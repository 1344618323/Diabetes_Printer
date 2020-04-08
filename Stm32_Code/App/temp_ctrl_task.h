#ifndef __TEMP_CTRL_TASK_H
#define __TEMP_CTRL_TASK_H

void Temp_Ctrl_Task(void const * argument);
void Get_NTC_Temp();

extern float curTemp;
#endif
