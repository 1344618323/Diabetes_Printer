#ifndef __SYS_H
#define __SYS_H
#include "stm32f4xx_hal.h"


#define VAL_LIMIT(val, min, max)\
if(val<=min)\
{\
	val = min;\
}\
else if(val>=max)\
{\
	val = max;\
}\


#define MyAbs(x) ((x > 0) ? (x) : (-x))


#define XTIM             htim3
#define XTIMCH           TIM_CHANNEL_1
#define XPULPlu_GPIO     GPIOA
#define XPULPlu_PIN      GPIO_PIN_7
#define XDIRPlu_GPIO     GPIOC
#define XDIRPlu_PIN      GPIO_PIN_4
#define XDIRMin_GPIO     GPIOA
#define XDIRMin_PIN      GPIO_PIN_5

#define YTIM             htim2
#define YTIMCH           TIM_CHANNEL_4
#define YPULPlu_GPIO     GPIOA
#define YPULPlu_PIN      GPIO_PIN_1
#define YDIRPlu_GPIO     GPIOA
#define YDIRPlu_PIN      GPIO_PIN_2
#define YDIRMin_GPIO     GPIOA
#define YDIRMin_PIN      GPIO_PIN_4

#define YTOU_GPIO        GPIOC
#define YTOU_PIN         GPIO_PIN_1
#define XTOU_GPIO        GPIOC
#define XTOU_PIN         GPIO_PIN_2


#define DIABETE_TEMP 96
//100
#define TEMP_TIM          htim4
#define TEMP_CH           TIM_CHANNEL_1
#define TEMP_OUT          TIM4->CCR1

#define SUGER_GPIO        GPIOD
#define SUGER_PIN         GPIO_PIN_13

#define DIABETE_OPEN      TIM1->CCR4=85;
#define DIABETE_CLOSE      TIM1->CCR4=182;

#define BLUETOOTH_HUART  huart1
#define BLUETOOTH_RX_MAX_BUFLEN  150
#endif
