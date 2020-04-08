#ifndef __BSP_UART_H__
#define __BSP_UART_H__

#include "usart.h"

void bluetooth_init();
void MyUartFrameIRQHandler(UART_HandleTypeDef *huart);
void MyUsartReceive_IDLE_Handler(UART_HandleTypeDef *huart);
#endif
