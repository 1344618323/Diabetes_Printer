#include "draw_diabetes_task.h"
#include "cmsis_os.h"
#include "sys.h"
#include "stepper_con.h"
#include "string.h"
#include "bluetooth_comm_task.h"


uint16_t lineCntLimit = 200;
#define DRAWCNT_LIMIT 250
#define DIY_DIABETE_ID 100

Mechine sMechine;
Mechine sLastMechine;
Mechine sMechineUart, sLastMechineUart;

/*****************************↓糖人路径部分↓*********************************/
enum {eLine = 0, eCircle = 1, eNoDraw = 0, eDraw = 1};

/**
*  内置糖人造型1：神奇宝贝logo
*/
const int16_t pokemonLogo[][4] =
{
	//触碰开关定位零点->A1点
	{eLine << 8 | eNoDraw << 4 | eMotorNormalMove, 60, 120, 3},
	//A1->A2的圆弧
	{eCircle << 8 | eDraw << 4 | eMotorSlowMove, 60, 1, 1},
	{ -60, 0, -60, 0},
	//A2->A3的线段
	{eLine << 8 | eDraw << 4 | eMotorSlowMove, 30, 0, 1},
	//A3->A4的圆弧
	{eCircle << 8 | eDraw << 4 | eMotorSlowMove, 30, 1, 1},
	{ -30, 0, -30, 0},
	//A4->A5的线段
	{eLine << 8 | eDraw << 4 | eMotorSlowMove, 60, 0, 1},
	//A4->触碰开关定位零点
	{eLine << 8 | eDraw << 4 | eMotorSlowMove, 30, 0, 1},
};

/**
*  内置糖人造型2：坦克
*/
const int16_t tankDiabete[][4] =
{
	{eLine << 8 | eNoDraw << 4 | eMotorNormalMove, 110, 150, 3},
	{eLine << 8 | eDraw << 4 | eMotorNormalMove, 10, -90, 1},
	{eCircle << 8 | eDraw << 4 | eMotorSlowMove, 26, 1, 1},
	{ 24, -10, -26, 0},
	{eCircle << 8 | eDraw << 4 | eMotorSlowMove, 20, 1, 1},
	{ -20, 0, -20, 0},
	{eLine << 8 | eDraw << 4 | eMotorSlowMove, -5, 42, 1},
	{eCircle << 8 | eDraw << 4 | eMotorSlowMove, 20, 1, 1},
	{ -20, 0, -20, 0},
	{eLine << 8 | eDraw << 4 | eMotorSlowMove, 0, 42, 1},
	{eCircle << 8 | eDraw << 4 | eMotorSlowMove, 20, 1, 1},
	{ -20, 0, -20, 0},
	{eCircle << 8 | eDraw << 4 | eMotorSlowMove, 25, 1, 1},
	{ -25, 0, 25, 0},
	{eLine << 8 | eDraw << 4 | eMotorSlowMove, 14, -8, 1},
	{eLine << 8 | eDraw << 4 | eMotorSlowMove, 4, -15, 1},
	{eLine << 8 | eDraw << 4 | eMotorSlowMove, 25, 50, 1},
	{eLine << 8 | eDraw << 4 | eMotorSlowMove, 6, -3, 1},
	{eLine << 8 | eDraw << 4 | eMotorSlowMove, -25, -50, 1},
	{eCircle << 8 | eDraw << 4 | eMotorSlowMove, 26, 1, 1},
	{ 10, 24, 0, -26},
	{eLine << 8 | eDraw << 4 | eMotorSlowMove, -10, 50, 1},
	{eLine << 8 | eDraw << 4 | eMotorSlowMove, 8, 0, 1},
	{eLine << 8 | eDraw << 4 | eMotorNormalMove, 0, -58, 1},
	{eLine << 8 | eDraw << 4 | eMotorSlowMove, -7, -8, 1},
};


/**
*  指向diy糖人造型路径的结构体
*/
typedef struct
{
	int16_t (*diyDiabete)[4];
	uint8_t totalLine;
	uint8_t isChange;
} DiyDiabeteStruct;

DiyDiabeteStruct sDiyDiabeteStr = {NULL, 0, 0};


/*****************************↓任务部分↓*********************************/
typedef enum {ePre = 0, ePreDone = 1,} eMotorPreSta;
typedef enum {eWait = 0, eWork = 1,} eMotorWorkSta;

/**
*  该任务用于绘制糖人
*/
void Draw_Diabetes_Task(void const * argument)
{
	Print_All_Init();

	osDelay(1000);
	while (1)
	{

		if (sMechineUart.isWork != sLastMechineUart.isWork ||
		        sMechineUart.diabeteId != sLastMechineUart.diabeteId) {
			sMechine.isWork = sMechineUart.isWork;
			sMechine.diabeteId = sMechineUart.diabeteId;
		}

		if (sPathAnalysis.isUse == 1) {
			sPathAnalysis.isUse = 0;
			if (sDiyDiabeteStr.diyDiabete != NULL) {
				vPortFree(sDiyDiabeteStr.diyDiabete);
			}
			sDiyDiabeteStr.diyDiabete = pvPortMalloc(sPathAnalysis.pointSum * 8);
			sDiyDiabeteStr.totalLine = sPathAnalysis.pointSum;
			sDiyDiabeteStr.isChange = 1;
			memcpy(sDiyDiabeteStr.diyDiabete, sPathAnalysis.diabetePath, sPathAnalysis.pointSum * 8);
			sMechineUart.isWork = sMechine.isWork = eWork;
			sMechineUart.diabeteId = sMechine.diabeteId = DIY_DIABETE_ID;
		}

		if (sMechine.isPre == ePre) {
			// 归零操作
			Zero_All_Init();
			if (Is_All_Init() == 1) {
				sMechine.isPre = ePreDone;
			}
		}
		else if (sMechine.isPre == ePreDone) {
			if (sMechine.isWork == eWait) {
				//收到蓝牙停止数据，如果上次循环是工作，就重新归零
				if (sLastMechine.isWork == eWork) {
					Analysis_End();
					DIABETE_CLOSE;//关闭舵机
					Print_All_Init();
					sMechine.isPre = ePre;
				}
			}
			else if (sMechine.isWork == eWork) {
				//如果画图完成或者糖画型号改变就停止作画重新归零
				if (Is_Analysis_End() ||
				        (sLastMechine.diabeteId != 0 && sLastMechine.diabeteId != sMechine.diabeteId)
				        || sDiyDiabeteStr.isChange) {
					if (sDiyDiabeteStr.isChange) {
						sDiyDiabeteStr.isChange = 0;
					}
					//如果是画完图，就改为eWait状态，啥都不做，
					//如果是改变糖人型号，就在归零后，继续eWork状态工作
					if (Is_Analysis_End()) {
						sMechine.isWork = eWait;
					}
					Analysis_End();
					DIABETE_CLOSE;//关闭舵机
					Print_All_Init();
					sMechine.isPre = ePre;
				}

				else {
					//画糖
					switch (sMechine.diabeteId) {
					case 1:
						lineCntLimit = 200;
						Analysis_Diabete_Code(pokemonLogo,
						                      (sizeof(pokemonLogo) / sizeof(pokemonLogo[0][0])) /
						                      (sizeof(pokemonLogo[0]) / sizeof(pokemonLogo[0][0])));
						break;
					case 2:
						lineCntLimit = 200;
						Analysis_Diabete_Code(tankDiabete,
						                      (sizeof(tankDiabete) / sizeof(tankDiabete[0][0])) /
						                      (sizeof(tankDiabete[0]) / sizeof(tankDiabete[0][0])));
						break;

					case DIY_DIABETE_ID:
						lineCntLimit = 50;
						Analysis_Diabete_Code(sDiyDiabeteStr.diyDiabete, sDiyDiabeteStr.totalLine);
						break;

					default:
						break;
					}
				}
			}
		}
		sLastMechine = sMechine;
		sLastMechineUart = sMechineUart;
		osDelay(5);
	}
}


/*****************************↓解析命令部分↓*********************************/
typedef struct
{
	uint8_t isDraw;//eNoDraw eDraw
	uint8_t lastDraw;//eNoDraw eDraw
	uint8_t isLine;//eLine eCircle
	uint8_t codeLine;//解析到第几行
	uint8_t lastCodeLine;
	uint8_t codeTotal;//共多少行

	uint16_t drawCnt;//画与否状态改变，延时3s
	uint16_t LineCnt;//线条状态改变，延时3s
	uint8_t sta;

	int16_t x;
	int16_t y;
	int16_t x2;
	int16_t y2;
	uint8_t bIsCW;
	int16_t r;
	int16_t step;
	int8_t speed;
} AnalysisCode;

AnalysisCode sAnaCode;

enum {eStartAna = 0, eWorkAna = 1, eEndAna = 2};

/**
*   命令行解析器
*   命令行格式如下所示
*   line     eLine<<8 |(draw or nodraw)<<4|speed     x   y     step
*   circle   eCircle<<8|(draw or nodraw)<<4|speed    r   corw  step
*                                              x1    y1  x2    y2
*   参数：diaArray命令行数组，totalLine命令行行数
*/
void Analysis_Diabete_Code(const int16_t diaArray[][4], uint8_t totolLine) {
	if (sAnaCode.sta == eStartAna) {
		//开始先获得共多少条命令
		sAnaCode.codeTotal = totolLine;
		sAnaCode.sta = eWorkAna;
	}
	if (sAnaCode.sta == eWorkAna) {
		if (sAnaCode.codeLine == 0 || sAnaCode.codeLine != sAnaCode.lastCodeLine) {
			if (sAnaCode.codeLine == 0) {
				sAnaCode.LineCnt = lineCntLimit;
			}
			else {
				sAnaCode.LineCnt = 0;
			}

			//解析完一条命令后解析下一条，获取是直线还是圆弧，以及是否出糖
			sAnaCode.isLine = diaArray[sAnaCode.codeLine][0] >> 8;
			sAnaCode.isDraw = (diaArray[sAnaCode.codeLine][0] & 0xf0) >> 4;
			sAnaCode.speed = diaArray[sAnaCode.codeLine][0] & 0x0f;
			sAnaCode.lastCodeLine = sAnaCode.codeLine;

			//如果出糖状态有变，延时3s
			if (sAnaCode.isDraw != sAnaCode.lastDraw) {
				if (sAnaCode.isDraw == eDraw) {
					DIABETE_OPEN;
				}
				else if (sAnaCode.isDraw == eNoDraw) {
					DIABETE_CLOSE;
				}
				sAnaCode.drawCnt = 0;
				sAnaCode.LineCnt = lineCntLimit;
			}
			else if (sAnaCode.isDraw == sAnaCode.lastDraw) {
				sAnaCode.drawCnt = DRAWCNT_LIMIT;
			}
			sAnaCode.lastDraw = sAnaCode.isDraw;

			//对应命令，画直线或圆弧
			if (sAnaCode.isLine == eLine) {
				sAnaCode.x = diaArray[sAnaCode.codeLine][1];
				sAnaCode.y = diaArray[sAnaCode.codeLine][2];
				sAnaCode.step = diaArray[sAnaCode.codeLine][3];
			}
			else if (sAnaCode.isLine == eCircle) {
				sAnaCode.x = diaArray[sAnaCode.codeLine + 1][0];
				sAnaCode.y = diaArray[sAnaCode.codeLine + 1][1];
				sAnaCode.x2 = diaArray[sAnaCode.codeLine + 1][2];
				sAnaCode.y2 = diaArray[sAnaCode.codeLine + 1][3];
				sAnaCode.step = diaArray[sAnaCode.codeLine][3];
				sAnaCode.r = diaArray[sAnaCode.codeLine][1];
				sAnaCode.bIsCW = diaArray[sAnaCode.codeLine][2];
			}
		}

		if (sAnaCode.drawCnt < DRAWCNT_LIMIT)
			sAnaCode.drawCnt++;
		else if (sAnaCode.LineCnt < lineCntLimit)
			sAnaCode.LineCnt++;
		else {
			if (sAnaCode.isLine == eLine) {
				if (!My_PBP_Line(sAnaCode.step, sAnaCode.x, sAnaCode.y, sAnaCode.speed)) {
					sAnaCode.codeLine++;
				}
			}
			else if (sAnaCode.isLine == eCircle) {
				//void PBP_Circle(double step, double XStart, double YStart, double XEnd, double YEnd, double radius, int bIsCW)
				if (!My_PBP_Circle(sAnaCode.step, sAnaCode.x, sAnaCode.y,
				                   sAnaCode.x2, sAnaCode.y2, sAnaCode.r, sAnaCode.bIsCW, sAnaCode.speed)) {
					sAnaCode.codeLine = sAnaCode.codeLine + 2;
				}
			}
			if ((sAnaCode.codeLine == sAnaCode.codeTotal && sAnaCode.isLine == eLine) ||
			        (sAnaCode.codeLine - 1 == sAnaCode.codeTotal && sAnaCode.isLine == eCircle)) {
				sAnaCode.sta = eEndAna;
			}
		}
	}
}

/**
*   强制命令行解析器结束解析矩阵，标志位都置0
*/
void Analysis_End() {
	memset(&sAnaCode, 0, sizeof(sAnaCode));
}

/**
*   判断命令行解析器是否将整个矩阵解析完毕
*/
uint8_t Is_Analysis_End() {
	return sAnaCode.sta == eEndAna ? 1 : 0;
}

/*****************************↓蓝牙通讯部分↓*********************************/
/**
*   对包头包尾进行检测
*/
void Uart_Callback_BLT_Handle(uint8_t* buff, uint32_t len) {
	if (buff[0] == 'A') {
		if (buff[2] == 'Z') {
			sMechineUart.diabeteId = buff[1] - '0';
			sMechineUart.isWork = eWork;
		}
		else if (buff[3] == 'Z') {
			sMechineUart.diabeteId = (buff[1] - '0') * 10 + (buff[2] - '0');
			sMechineUart.isWork = eWork;
		}
	}
	if (strncmp("stop", buff, 4) == 0) {
		sMechineUart.isWork = eWait;
	}

	/************diy路径部分*************/
	if (strncmp("path", buff, 4) == 0 || strncmp("tp", buff, 2) == 0) {
		RecivePath_Handler(buff, len);
	}
}


// if (msg.length() <= 100) {
// 	try {
// 		byte[] bytes = msg.getBytes();
// 		outputStream.write(bytes);
// 	} catch (IOException e) {
// 		e.printStackTrace();
// 	}
// } else {
// 	int scnt = (int) Math.ceil((double) (msg.length()) / 100);
// 	for (int cnt = 0; cnt < scnt; cnt++) {
// 		String str;
// 		if (cnt == scnt - 1) {
// 			String str2 = msg.substring(cnt * 100, msg.length());
// 			str = "tp" + str2;
// 		} else if (cnt == 0) {
// 			str = msg.substring(cnt * 100, cnt * 100 + 99);
// 			str = str + "ep" + scnt + "lp";
// 		} else {
// 			String str2 = msg.substring(cnt * 100, cnt * 100 + 99);
// 			str = "tp" + str2;
// 			str = str + "ep";
// 		}
// 		try {
// 			byte[] bytes = str.getBytes();
// 			outputStream.write(bytes);
// 			sleep(100);
// 		} catch (IOException e) {
// 			e.printStackTrace();
// 		} catch (InterruptedException e) {
// 			e.printStackTrace();
// 		}
// 	}
// }