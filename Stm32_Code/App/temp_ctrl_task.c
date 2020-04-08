#include "arm_math.h"
#include "temp_ctrl_task.h"
#include "cmsis_os.h"
#include "sys.h"
#include "adc.h"
#include "pid.h"
#include "tim.h"
/**
*  该任务用于控制温度
*/
int16_t cxnTemp[5];
int16_t cxnTempSum;
float cxnTempAvg;
float curTemp;//融糖器当前温度
pid_t pid_temp  = { 0 };
int32_t pidOut;

void Temp_Ctrl_Task(void const * argument)
{
  PID_struct_init(&pid_temp, POSITION_PID, 20000, 14000, 180.0f, 160.0f, 20.0f);
	osDelay(1000);
  HAL_TIM_PWM_Start(&TEMP_TIM, TEMP_CH);
	//TEMP_OUT = 10000;
  while (1)
  {
    uint8_t i;
    for (i = 0; i < 16; i++) {
      osDelay(100);
      Get_NTC_Temp();
			
			pidOut = (int32_t)(pid_calc(&pid_temp, curTemp, DIABETE_TEMP));
    }
		
    VAL_LIMIT(pidOut, 0, 20000);

    if (pidOut <= 2000) {
      pidOut = 2000;
			if(curTemp<DIABETE_TEMP-5)
				 pidOut = 6000;
    }
    else if (pidOut >= 18000) {
      pidOut = pidOut >= 19000 ? 20000 : 18000;
    }
		
		if(curTemp>DIABETE_TEMP+1){
			pidOut = 0;
		}
		
    TEMP_OUT = (uint32_t)pidOut;
  }
}

/**
*  获取温度
*/
void Get_NTC_Temp() {
  uint8_t i;
  for (i = 0; i < 5; i++) {
    HAL_ADC_Start(&hadc1);
    HAL_ADC_PollForConversion(&hadc1, 10);
    cxnTemp[i] = HAL_ADC_GetValue(&hadc1);
    cxnTempSum += cxnTemp[i];
    if (i == 4) {
      cxnTempAvg = 2.085 * 5 * 4096 / 1.0 / cxnTempSum - 2.085;
      curTemp = 1177692.5 / (298.15 * log(cxnTempAvg / 10) + 3950) - 273.15;
      cxnTempSum = 0;
    }
    osDelay(5);
  }
}