#include "stepper_con.h"
#include "tim.h"
#include "sys.h"
#include "string.h"
#include "cmsis_os.h"
/**
*  ���ڿ���ת�ٺͲ�������
*/
#define RADIO 80/3   //8ϸ��

SteppermotorCon sXstepper;
SteppermotorCon sYstepper;
MyPoint sCurPoint;//ȫ����mm��0��0������240,240��
MyPbpStr sPbpStu;


enum {	eNReach = 0, eReach = 1};
enum {	eNStop = 0,	eStop = 1};
enum {	eNpre = 0, ePreing = 1, ePre = 2};



/**
*  ����Զ����ٶȵ���(tarXmm,tarYmm)
*	 0-300
*  �����Ƿ񵽴�Ŀ������
*/
uint8_t Con_AllMotor_Move(int32_t tarXmm, int32_t tarYmm, int8_t speed) {
	uint8_t xreach, yreach;
	yreach = Con_Motor_Move(&sYstepper, tarYmm,  speed);
	xreach = Con_Motor_Move(&sXstepper, tarXmm,  speed);
	return xreach && yreach;
}

uint8_t Con_Motor_Move(SteppermotorCon *stepper, int32_t tarMM, int8_t speed) {
//�Զ����ٶȵ�(tarMM,tarMM)
	if (stepper->spd != speed) {
		//δͣ�¼�ʱ�����͸ı��ٶȣ���Ҫ�ȹؼ�ʱ�������ؿ�
		if (stepper->stop != eStop) {
			HAL_TIM_PWM_Stop_IT(&(stepper->tim), stepper->ch);
		}
		stepper->tim.Instance->CNT = 0;
		if (speed == eMotorSlowMove)
			stepper->tim.Instance->ARR = 399;
		else if (speed == eMotorNormalMove)
			stepper->tim.Instance->ARR = 199;
		else if (speed == eMotorHighMove)
			stepper->tim.Instance->ARR = 99;
		stepper->spd = speed;
		if (stepper->stop != eStop) {
			HAL_TIM_PWM_Start_IT(&(stepper->tim), stepper->ch);
		}
	}

	if (stepper->tarMM != tarMM) {
		stepper->tarMM = tarMM;
		stepper->addPulNum = tarMM * RADIO - stepper->pulGetNum;
		stepper->pulSetNum = stepper->pulGetNum + stepper->addPulNum;

		if (stepper->addPulNum != 0) {
			stepper->dir = stepper->addPulNum > 0 ? 1 : -1;
			HAL_GPIO_WritePin(stepper->dirMinGpio, stepper->dirMinPin, stepper->addPulNum > 0 ? GPIO_PIN_RESET : GPIO_PIN_SET);
			if (stepper->stop == eStop) {
				HAL_TIM_PWM_Start_IT(&(stepper->tim), stepper->ch);
			}
			stepper->stop = eNStop;
		}
		else {
			stepper->stop = eStop;
		}
	}
	return stepper->stop;
}

/**
*  �����ʼ��
*/
void Stepper_Init() {
	HAL_GPIO_WritePin(YPULPlu_GPIO, YPULPlu_PIN, GPIO_PIN_SET);//PUL+ ��3.3V
	HAL_GPIO_WritePin(YDIRPlu_GPIO, YDIRPlu_PIN, GPIO_PIN_SET);//DIR+ ��3.3V
	sYstepper.tim = YTIM;
	sYstepper.ch = YTIMCH;
	sYstepper.dirMinGpio = YDIRMin_GPIO;
	sYstepper.dirMinPin = YDIRMin_PIN;
	sYstepper.touGpio = YTOU_GPIO;
	sYstepper.touPin = YTOU_PIN;
	HAL_GPIO_WritePin(XPULPlu_GPIO, XPULPlu_PIN, GPIO_PIN_SET);//PUL+ ��3.3V
	HAL_GPIO_WritePin(XDIRPlu_GPIO, XDIRPlu_PIN, GPIO_PIN_SET);//DIR+ ��3.3V
	sXstepper.tim = XTIM;
	sXstepper.ch = XTIMCH;
	sXstepper.dirMinGpio = XDIRMin_GPIO;
	sXstepper.dirMinPin = XDIRMin_PIN;
	sXstepper.touGpio = XTOU_GPIO;
	sXstepper.touPin = XTOU_PIN;
}



/**
*  pwm�жϺ���
*  �������ﵽĿ��ʱ������1������Ϊ0
*  �����и���ת����ת��
*/
void HAL_TIM_PWM_PulseFinishedCallback(TIM_HandleTypeDef *htim) {
	if (htim == &YTIM) {
		sYstepper.pulGetNum += sYstepper.dir;
		if (sYstepper.pulGetNum == sYstepper.pulSetNum) {
			HAL_TIM_PWM_Stop_IT(&(sYstepper.tim), sYstepper.ch);
			sYstepper.stop = eStop;
		}
	}
	else if (htim == &XTIM) {
		sXstepper.pulGetNum += sXstepper.dir;
		if (sXstepper.pulGetNum == sXstepper.pulSetNum) {
			HAL_TIM_PWM_Stop_IT(&(sXstepper.tim), sXstepper.ch);
			sXstepper.stop = eStop;
		}
	}
}

/**
*  ϵͳ��λ
*/
void Print_All_Init() {
	Stepper_Stop();
	memset(&sXstepper, 0, sizeof(sXstepper));
	memset(&sYstepper, 0, sizeof(sYstepper));
	Stepper_Init();
	memset(&sCurPoint, 0, sizeof(sCurPoint));
	memset(&sPbpStu, 0, sizeof(sPbpStu));
}


/**
*  �����λ����
*/
void Zero_All_Init() {
	Zero_Motor_Init(&sXstepper);
	Zero_Motor_Init(&sYstepper);
}

uint8_t Is_All_Init(){
	return (sXstepper.pre==2?1:0)&&(sYstepper.pre==2?1:0);
}

void Zero_Motor_Init(SteppermotorCon *stepper) {
	if (stepper->pre == eNpre) {
		stepper->pre = ePreing;
		stepper->tim.Instance->CNT = 0;
		stepper->tim.Instance->ARR = 199;
		stepper->spd = eMotorNormalMove;
		HAL_TIM_PWM_Start(&(stepper->tim), stepper->ch);
		HAL_GPIO_WritePin(stepper->dirMinGpio, stepper->dirMinPin, GPIO_PIN_SET);//DIR-
		stepper->stop = eNStop;
	}
	else if (stepper->pre == ePreing) {
		if (HAL_GPIO_ReadPin(stepper->touGpio, stepper->touPin)) {
			osDelay(5);
			if (HAL_GPIO_ReadPin(stepper->touGpio, stepper->touPin)) {
				stepper->pre = ePre;
				HAL_TIM_PWM_Stop(&(stepper->tim), stepper->ch);
				stepper->stop = eStop;
			}
		}
	}
	if (stepper->pre == ePre) {
		Con_Motor_Move(stepper, 0, eMotorNormalMove);
	}
}

/**
*  ���ֹͣ����
*/
void Stepper_Stop() {
	if (sYstepper.stop != eStop) {
		HAL_TIM_PWM_Stop_IT(&YTIM, YTIMCH);
		sYstepper.stop = eStop;
	}
	if (sXstepper.stop != eStop) {
		HAL_TIM_PWM_Stop_IT(&XTIM, XTIMCH);
		sXstepper.stop = eStop;
	}
}


/*****************************��BSP�㣬�����������ȽϷ�ʵ�ֻ�Բ������ֱ�ߵ��㷨��*********************************/
/**
*  ���ȽϷ����ж�����
*/
uint8_t Judge_Quadrant(int32_t x , int32_t y) {
	if (x >= 0 && y >= 0)
		return 1;
	else if (x >= 0 && y < 0)
		return 4;
	else if (x < 0 && y >= 0)
		return 2;
	else if (x < 0 && y < 0)
		return 3;
	return 0;
}

/**
*  ���ܣ����ȽϷ�ֱ�߲岹
*  ������double step					����		mm
*		 double XEnd, double YEnd	�岹�յ�	mm
*  ���ػ�ֱ�ߵ�״̬������0ʱ��Ϊ���ֱ��
*/
uint8_t My_PBP_Line(int32_t step, int32_t xEnd, int32_t yEnd, int8_t speed) {

	if (sPbpStu.sta == ePbpStop) {
		sPbpStu.nDir = Judge_Quadrant(xEnd , yEnd);
		sPbpStu.stepCount = 0;
		sPbpStu.xLastVal = sPbpStu.yLastVal = sPbpStu.xCurVal = sPbpStu.yCurVal = 0;
		sPbpStu.step = step;
		sPbpStu.xEnd = MyAbs(xEnd);
		sPbpStu.yEnd = MyAbs(yEnd);
		sPbpStu.stepMount = (int32_t) (MyAbs(xEnd) + MyAbs(yEnd)) / step;
		sPbpStu.lDevVal = sPbpStu.yCurVal * sPbpStu.xEnd - sPbpStu.xCurVal * sPbpStu.yEnd;
		sPbpStu.sta = ePbpCal;
	}

	if (sPbpStu.sta == ePbpCal) {
		if (sPbpStu.stepCount < sPbpStu.stepMount) {
			if (sPbpStu.lDevVal >= 0)
			{
				if (sPbpStu.nDir == 1 || sPbpStu.nDir == 4) {
					sPbpStu.xCurVal += sPbpStu.step;
				}
				else if (sPbpStu.nDir == 2 || sPbpStu.nDir == 3) {
					sPbpStu.xCurVal -= sPbpStu.step;
				}

				sPbpStu.lDevVal -= sPbpStu.yEnd;
			}
			else {
				if (sPbpStu.nDir == 1 || sPbpStu.nDir == 2) {
					sPbpStu.yCurVal += sPbpStu.step;
				}
				else if (sPbpStu.nDir == 3 || sPbpStu.nDir == 4) {
					sPbpStu.yCurVal -= sPbpStu.step;
				}
				sPbpStu.lDevVal += sPbpStu.xEnd;
			}

			sPbpStu.stepCount++;
			sPbpStu.sta = ePbpWork;
		}
		else {
			sPbpStu.sta = ePbpStop;
		}
	}

	if (sPbpStu.sta == ePbpWork) {
		if (Con_AllMotor_Move(sCurPoint.x + sPbpStu.xCurVal - sPbpStu.xLastVal, sCurPoint.y +
		                      sPbpStu.yCurVal - sPbpStu.yLastVal, speed)) {
			sPbpStu.sta = ePbpCal;
			sCurPoint.x += sPbpStu.xCurVal - sPbpStu.xLastVal;
			sCurPoint.y += sPbpStu.yCurVal - sPbpStu.yLastVal;
			sPbpStu.xLastVal = sPbpStu.xCurVal;
			sPbpStu.yLastVal = sPbpStu.yCurVal;
		}
	}

	return sPbpStu.sta;
}



/********************************************************************/
/*  ������: PBP_Circle      										*/
/*  ��  �ܣ����ȽϷ�Բ���岹										*/
/*  ��  ����double step						����		mm			*/
/*			double XStart,	double YStart	�岹���	mm			*/
/*			double XEnd,	double YEnd		�岹�յ�	mm			*/
/*			double radius	Բ���뾶					mm			*/
/*          bool bIsCW		Բ���岹����	0��1				    */
/********************************************************************/
uint8_t My_PBP_Circle(int32_t step, int32_t xStart, int32_t yStart,
                      int32_t xEnd, int32_t yEnd, int32_t radius, uint8_t bIsCW, int8_t speed)
{

	if (sPbpStu.sta == ePbpStop) {
		sPbpStu.xLastVal = sPbpStu.xCurVal = xStart;
		sPbpStu.yLastVal = sPbpStu.yCurVal = yStart;
		sPbpStu.radius = radius;
		sPbpStu.xEnd = xEnd;
		sPbpStu.yEnd = yEnd;
		sPbpStu.step = step;
		sPbpStu.bIsCW = bIsCW;
		sPbpStu.lDevVal = sPbpStu.xCurVal * sPbpStu.xCurVal + sPbpStu.yCurVal * sPbpStu.yCurVal - sPbpStu.radius * sPbpStu. radius;
		sPbpStu.goallDevVal = (sPbpStu.xCurVal - sPbpStu.xEnd) * (sPbpStu.xCurVal - sPbpStu.xEnd) +
		                      (sPbpStu.yCurVal - sPbpStu.yEnd) * (sPbpStu.yCurVal - sPbpStu.yEnd);
		sPbpStu.stepCount = 0;
		sPbpStu.sta = ePbpCal;
	}

	if (sPbpStu.sta == ePbpCal) {
		if (sPbpStu.goallDevVal >= sPbpStu.step * sPbpStu.step || sPbpStu.stepCount == 0) {
			sPbpStu.nDir = Judge_Quadrant(sPbpStu.xCurVal, sPbpStu.yCurVal);
			if (sPbpStu.lDevVal >= 0) {
				//�����ڵ���0
				if (sPbpStu.bIsCW == 0) {
					//��Բ�岹
					switch (sPbpStu.nDir)
					{
					case 1:
						sPbpStu.xCurVal -= sPbpStu.step;

						break;
					case 2:
						sPbpStu.yCurVal -= sPbpStu.step;
						break;
					case 3:
						sPbpStu.xCurVal += sPbpStu.step;
						break;
					case 4:
						sPbpStu.yCurVal += sPbpStu.step;
						break;
					default: break;
					}

				}
				else//˳Բ�岹
				{
					switch (sPbpStu.nDir)
					{
					case 1:
						sPbpStu.yCurVal -= sPbpStu.step;
						break;
					case 2:
						sPbpStu.xCurVal += sPbpStu.step;
						break;
					case 3:
						sPbpStu.yCurVal += sPbpStu.step;
						break;
					case 4:
						sPbpStu.xCurVal -= sPbpStu.step;

						break;
					default: break;
					}
				}
			}
			else//���С����
			{
				if (sPbpStu.bIsCW == 0) //��Բ�岹
				{
					switch (sPbpStu.nDir)
					{
					case 1:
						sPbpStu.yCurVal += sPbpStu.step;
						break;
					case 2:
						sPbpStu.xCurVal -= sPbpStu.step;

						break;
					case 3:
						sPbpStu.yCurVal -= sPbpStu.step;
						break;
					case 4:
						sPbpStu.xCurVal += sPbpStu.step;
						break;
					default: break;
					}
				}
				else//˳Բ�岹
				{
					switch (sPbpStu.nDir)
					{
					case 1:
						sPbpStu.xCurVal += sPbpStu.step;
						break;
					case 2:
						sPbpStu.yCurVal += sPbpStu.step;
						break;
					case 3:
						sPbpStu.xCurVal -= sPbpStu.step;

						break;
					case 4:
						sPbpStu.yCurVal -= sPbpStu.step;
						break;
					default: break;
					}
				}
			}
			sPbpStu.lDevVal = sPbpStu.xCurVal * sPbpStu.xCurVal + sPbpStu.yCurVal * sPbpStu.yCurVal - sPbpStu.radius * sPbpStu.radius;
			sPbpStu.goallDevVal = (sPbpStu.xCurVal - sPbpStu.xEnd) * (sPbpStu.xCurVal - sPbpStu.xEnd) +
			                      (sPbpStu.yCurVal - sPbpStu.yEnd) * (sPbpStu.yCurVal - sPbpStu.yEnd);
			sPbpStu.stepCount++;
			sPbpStu.sta = ePbpWork;
		} else {
			sPbpStu.sta = ePbpStop;
		}
	}

	if (sPbpStu.sta == ePbpWork) {
		if (Con_AllMotor_Move(sCurPoint.x + sPbpStu.xCurVal - sPbpStu.xLastVal,
		                      sCurPoint.y + sPbpStu.yCurVal - sPbpStu.yLastVal, speed)) {
			sPbpStu.sta = ePbpCal;
			sCurPoint.x += sPbpStu.xCurVal - sPbpStu.xLastVal;
			sCurPoint.y += sPbpStu.yCurVal - sPbpStu.yLastVal;
			sPbpStu.xLastVal = sPbpStu.xCurVal;
			sPbpStu.yLastVal = sPbpStu.yCurVal;
		}
	}

	return sPbpStu.sta;
}
