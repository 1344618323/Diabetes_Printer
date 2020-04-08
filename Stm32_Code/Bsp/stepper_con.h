#ifndef __STEPPER_CON_H
#define __STEPPER_CON_H
#include "sys.h"

typedef struct {
	int32_t tarMM;
	int32_t addPulNum;
	int32_t pulGetNum;
	int32_t pulSetNum;
	int8_t dir;//+1 -1
	uint8_t stop; //eNStop = 0,	eStop = 1
	uint8_t pre;  //eNpre = 0, ePreing = 1, ePre = 2
	int8_t spd;  //eMotorSlowMove = 0,	eMotorNormalMove = 1,	eMotorHighMove = 2
	TIM_HandleTypeDef tim;
	uint32_t ch;
	GPIO_TypeDef *dirMinGpio;
	uint16_t dirMinPin;
	GPIO_TypeDef *touGpio;
	uint16_t touPin;
} SteppermotorCon;

typedef struct
{
	int32_t x;
	int32_t y;
} MyPoint;

typedef struct
{
	uint8_t sta;//ePbpStop=0,ePbpWork=1
	int32_t lDevVal;//当前偏差值
	int32_t xCurVal;
	int32_t	yCurVal; //当前点的实际位置
	int32_t stepMount;//插补综述
	int32_t stepCount; //插补计数器
	uint8_t nDir;//所在象限
	int32_t xEnd;
	int32_t yEnd;
	int32_t step;

	int32_t radius;
	int32_t goallDevVal;//与终点的偏差
	uint8_t bIsCW;
	int32_t xLastVal;
	int32_t	yLastVal; //当前点的实际位置
} MyPbpStr;




typedef enum {eMotorSlowMove = 0, eMotorNormalMove = 1, eMotorHighMove = 2} eMotorSpd;
enum {ePbpStop = 0, ePbpCal = 1, ePbpWork = 2};

void Print_All_Init();
void Stepper_Init();
void Zero_All_Init();
void Zero_Motor_Init(SteppermotorCon *stepper);
void Stepper_Stop();
uint8_t Con_AllMotor_Move(int32_t tarXmm, int32_t tarYmm, int8_t speed);
uint8_t Con_Motor_Move(SteppermotorCon *stepper, int32_t tarMM, int8_t speed);
uint8_t My_PBP_Line(int32_t step, int32_t xEnd, int32_t yEnd, int8_t speed);
uint8_t My_PBP_Circle(int32_t step, int32_t xStart, int32_t yStart,
                      int32_t xEnd, int32_t yEnd, int32_t radius, uint8_t bIsCW, int8_t speed);
uint8_t Judge_Quadrant(int32_t x , int32_t y);
uint8_t Is_All_Init();
#endif
