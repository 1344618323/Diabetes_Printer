#ifndef __BLUETOOTH_COMM_TASK_H
#define __BLUETOOTH_COMM_TASK_H
#include "sys.h"
void Bluetooth_Comm_Task(void const * argument);
void RecivePath_Handler(uint8_t*buff, uint32_t len);
void RecivePath_Analysis();


typedef struct
{
	uint8_t (*pathArr)[2];
	int16_t (*diabetePath)[4];
	uint8_t line;
	uint8_t isUse;
	uint8_t pointSum;//点的总数
} PathAnalysis;


extern PathAnalysis sPathAnalysis;
#endif
