#ifndef __DRAW_DIABETES_TASK_H
#define __DRAW_DIABETES_TASK_H
#include "sys.h"
void Draw_Diabetes_Task(void const * argument);
void Analysis_Diabete_Code(const int16_t diaArray[][4], uint8_t totolLine);
void Uart_Callback_BLT_Handle(uint8_t* buff, uint32_t len);
void Path_Generate(uint8_t* buff);
void Analysis_End();
uint8_t Is_Analysis_End();
typedef struct {
	uint8_t diabeteId;//打印第几个糖人NoId：0-99,100为diy
	uint8_t isWork;//1打印 0停止
	uint8_t isPre;
} Mechine;
#endif