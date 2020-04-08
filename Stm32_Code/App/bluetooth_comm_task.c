#include "bluetooth_comm_task.h"
#include "cmsis_os.h"
#include "sys.h"
#include "bsp_uart.h"
#include "temp_ctrl_task.h"
#include "string.h"

/**
*  该任务用于蓝牙发送数据
*/
char positionStr[10];
char tempStr[8];

void Bluetooth_Comm_Task(void const * argument)
{
	osDelay(1000);
	while (1)
	{
		static uint32_t second;
		second++;
		if (second % 5 == 0) {
			float tempCopy = curTemp;
			VAL_LIMIT(tempCopy, -999, 999);
			sprintf(tempStr + 1, "%.1f", tempCopy);
			tempStr[0] = 'T';
			tempStr[7] = 'Z';
			HAL_UART_Transmit_DMA(&BLUETOOTH_HUART, tempStr, 8);
		}

		//解析路径
		if (second % 5 == 0) {
			RecivePath_Analysis();
		}

		osDelay(100);
	}
}




PathAnalysis sPathAnalysis = {NULL, NULL, 0, 0, 0};

typedef struct
{
	uint8_t path[500];
	uint8_t isStart;//是否开始接收
	uint8_t isEnd;//是否结束接收
	uint8_t isChange;//路径是否更改
	uint8_t scnt;//每接收到一个发送的包+1
	uint8_t slen;//分开发送的包的总数
	uint16_t pathlen;//字符串总个数
	uint8_t pointSum;//点的总数
} RecivePath;

RecivePath sRecivePath;

void RecivePath_Handler(uint8_t*buff, uint32_t len) {
	if (strncmp("path", buff, 4) == 0) {
		memset(&sRecivePath.path, 0, sizeof(sRecivePath.path));
		if (buff[5] == 'x')
			sRecivePath.pointSum = buff[4] - '0' ;
		else if (buff[6] == 'x')
			sRecivePath.pointSum = (buff[4] - '0') * 10 + buff[5] - '0';
		else
			return;

		if (strncmp("end", buff + len - 3, 3) == 0) {
			sRecivePath.isEnd = 1;
			sRecivePath.isStart = 0;
			memcpy(sRecivePath.path, buff, len);
			sRecivePath.pathlen = len;
		}
		else if (strncmp("lp", buff + len - 2, 2) == 0) {
			if (strncmp("ep", buff + len - 6, 2) == 0) {
				sRecivePath.slen = (buff[len - 4] - '0') * 10 + (buff[len - 3] - '0');
				memcpy(sRecivePath.path, buff, len - 6);
			}
			else if (strncmp("ep", buff + len - 5, 2) == 0) {
				sRecivePath.slen = buff[len - 3] - '0';
				memcpy(sRecivePath.path, buff, len - 5);
			}
			sRecivePath.isEnd = 0;
			sRecivePath.scnt = 1;
			sRecivePath.isStart = 1;
		}
		sRecivePath.isChange = 1;
	}
	else if (strncmp("tp", buff, 2) == 0 && sRecivePath.isEnd == 0 && sRecivePath.isStart == 1) {
		if (strncmp("end", buff + len - 3, 3) == 0) {
			if (sRecivePath.slen - 1 == sRecivePath.scnt) {
				sRecivePath.isEnd = 1;
				sRecivePath.pathlen = sRecivePath.scnt * 100 + len - 2;
			}
			sRecivePath.isStart = 0;
			memcpy(sRecivePath.path + sRecivePath.scnt * 100, buff + 2, len - 2);
		}
		else if (strncmp("ep", buff + len - 2, 2) == 0) {
			memcpy(sRecivePath.path + sRecivePath.scnt * 100, buff + 2, len - 4);
			sRecivePath.scnt++;
		}
	}
}



//int16_t array[30][4];


void RecivePath_Analysis() {
	uint32_t i;
	uint8_t j, k, m, n;

	if (sRecivePath.isEnd == 1 && sRecivePath.isChange == 1) {
		sRecivePath.isChange = 0;


		if (sPathAnalysis.pathArr != NULL) {
			vPortFree(sPathAnalysis.pathArr);
		}
		sPathAnalysis.pathArr = pvPortMalloc(sRecivePath.pointSum * 2);
		memset(sPathAnalysis.pathArr,0, sRecivePath.pointSum * 2);

		if (sPathAnalysis.diabetePath != NULL) {
			vPortFree(sPathAnalysis.diabetePath);
		}
		/**
		pvPortMalloc分配空间的单位是字节
		sRecivePath.pointSum为手机采集的坐标总数，即二维数组总行数
		表4-4是将每个数据以short（两个字节）数据类型存储，数组每行存储4个数据
		所以分配空间为sRecivePath.pointSum*8
		**/
		sPathAnalysis.diabetePath = pvPortMalloc(sRecivePath.pointSum * 8);

		sPathAnalysis.line = 0;

		for (i = 0; i < sRecivePath.pathlen; i++) {
			if (sRecivePath.path[i] == 'x') {
				for (j = 2; j < 5; j++) {
					if (sRecivePath.path[i + j] == 'y') {
						for (k = 1; k < j; k++) {
							if (sRecivePath.path[i + k] < '0' || sRecivePath.path[i + k] > '9') {
								j = 5;
								break;//
							}
							sPathAnalysis.pathArr[sPathAnalysis.line][0] = sPathAnalysis.pathArr[sPathAnalysis.line][0] * 10 + (sRecivePath.path[i + k] - '0');
							if (k == j - 1) {
								for (m = 2; m < 5; m++) {
									if (sRecivePath.path[i + j + m] == 'x'
									        || (strncmp(sRecivePath.path + i + j + m, "end", 3) == 0)) {
										for (n = 1; n < m; n++) {
											if (sRecivePath.path[i + j + n] < '0' || sRecivePath.path[i + j + n] > '9') {
												m = 5;
												break;
											}
											sPathAnalysis.pathArr[sPathAnalysis.line][1] = sPathAnalysis.pathArr[sPathAnalysis.line][1] * 10 + (sRecivePath.path[i + j + n] - '0');

											sPathAnalysis.diabetePath[sPathAnalysis.line][3] = sPathAnalysis.line == 0 ? 3 : 1;
											sPathAnalysis.diabetePath[sPathAnalysis.line][0] = sPathAnalysis.line == 0 ? (1 | (0 << 4)) : (0 | (1 << 4));
											sPathAnalysis.diabetePath[sPathAnalysis.line][1] = sPathAnalysis.line == 0 ? ((int16_t)(sPathAnalysis.pathArr[sPathAnalysis.line][0]) + 45) : (int16_t)sPathAnalysis.pathArr[sPathAnalysis.line][0] - (int16_t)sPathAnalysis.pathArr[sPathAnalysis.line - 1][0];
											sPathAnalysis.diabetePath[sPathAnalysis.line][2] = sPathAnalysis.line == 0 ? ((int16_t)(sPathAnalysis.pathArr[sPathAnalysis.line][1]) + 45) : (int16_t)sPathAnalysis.pathArr[sPathAnalysis.line][1] - (int16_t)sPathAnalysis.pathArr[sPathAnalysis.line - 1][1];

											//array[sPathAnalysis.line][0]=sPathAnalysis.diabetePath[sPathAnalysis.line][0];
											//array[sPathAnalysis.line][1]=sPathAnalysis.diabetePath[sPathAnalysis.line][1];
											//array[sPathAnalysis.line][2]=sPathAnalysis.diabetePath[sPathAnalysis.line][2];
											//array[sPathAnalysis.line][3]=sPathAnalysis.diabetePath[sPathAnalysis.line][3];

											if (n == m - 1) {
												sPathAnalysis.line++;
												i = i + j + m - 1;
											}
										}
										break;
									}
								}
								if (m == 5) {
									j = 5;
									break;
								}
							}
						}
						break;
					}
				}
				if (j == 5) {
					break;
				}
			}
		}
		if (i >= sRecivePath.pathlen) {
			if (sPathAnalysis.line == sRecivePath.pointSum) {
				sPathAnalysis.isUse = 1;
				sPathAnalysis.pointSum = sRecivePath.pointSum;
			}
			else {
				sPathAnalysis.isUse = 0;
			}
		}
		else {
			sPathAnalysis.isUse = 0;
		}
	}
}