#include "bsp_uart.h"
#include "sys.h"
#include "usart.h"
#include "string.h"
#include "draw_diabetes_task.h"

uint8_t   bluetooth_buff[BLUETOOTH_RX_MAX_BUFLEN];

/**
  * @brief   enable global uart it and do not use DMA transfer done it
  * @param   uart IRQHandler id, receive buff, buff size
  * @retval  set success or fail
  */
static int UART_Receive_DMA_No_IT(UART_HandleTypeDef* huart, uint8_t* pData, uint32_t Size)
{
	uint32_t tmp1 = 0;

	tmp1 = huart->RxState;
	if (tmp1 == HAL_UART_STATE_READY)
	{
		if ((pData == NULL) || (Size == 0))
		{
			return HAL_ERROR;
		}

		/* Process Locked */
		__HAL_LOCK(huart);

		huart->pRxBuffPtr = pData;
		huart->RxXferSize = Size;
		huart->ErrorCode  = HAL_UART_ERROR_NONE;

		/* Enable the DMA Stream */
		HAL_DMA_Start(huart->hdmarx, (uint32_t)&huart->Instance->DR,
		              (uint32_t)pData, Size);

		/* Enable the DMA transfer for the receiver request by setting the DMAR bit
		 in the UART CR3 register */
		huart->Instance->CR3 |= USART_CR3_DMAR;

		/* Process Unlocked */
		__HAL_UNLOCK(huart);

		return HAL_OK;
	}
	else
	{
		return HAL_BUSY;
	}
}

/**
  * @brief   initialize uart device
  * @usage   after MX_USARTx_UART_Init() use these function
  */
//void bluetooth_init()
//{
//	__HAL_UART_CLEAR_IDLEFLAG(&BLUETOOTH_HUART);
//	__HAL_UART_ENABLE_IT(&BLUETOOTH_HUART, UART_IT_IDLE);
//	UART_Receive_DMA_No_IT(&BLUETOOTH_HUART, bluetooth_buff, BLUETOOTH_RX_MAX_BUFLEN);
//}

/**
  * @brief   clear idle it flag after uart receive a frame data
  * @param   uart IRQHandler id
  * @retval  none
  * @usage   call in MyUartFrameIRQHandler() function
  */
void uart_reset_idle_rx_callback(UART_HandleTypeDef* huart)
{
	if (__HAL_UART_GET_FLAG(huart, UART_FLAG_IDLE))
	{
		__HAL_UART_CLEAR_IDLEFLAG(huart);
		// clear idle it flag
		uint32_t DMA_FLAGS = __HAL_DMA_GET_TC_FLAG_INDEX(huart->hdmarx);
		//according uart clear corresponding DMA flag

		__HAL_DMA_DISABLE(huart->hdmarx);
		__HAL_DMA_CLEAR_FLAG(huart->hdmarx, DMA_FLAGS);
		__HAL_DMA_SET_COUNTER(huart->hdmarx, BLUETOOTH_RX_MAX_BUFLEN);
		__HAL_DMA_ENABLE(huart->hdmarx);
	}
}

/**
  * @brief   callback this function when uart interrupt
  * @param   uart IRQHandler id
  * @retval  none
  * @usage   call in uart handler function USARTx_IRQHandler()
  */

void MyUartFrameIRQHandler(UART_HandleTypeDef* huart)
{
	if (huart == &BLUETOOTH_HUART)
	{
		//Uart_Callback_BLT_Handle(bluetooth_buff, len);
	}
	uart_reset_idle_rx_callback(huart);
}

uint32_t rx_len;

void MyUsartReceive_IDLE_Handler(UART_HandleTypeDef *huart)
{
	uint32_t temp;

	if ((__HAL_UART_GET_FLAG(huart, UART_FLAG_IDLE) != RESET))
	{
		__HAL_UART_CLEAR_IDLEFLAG(huart);
		HAL_UART_DMAStop(huart);
		__HAL_UART_DISABLE_IT(huart, UART_IT_IDLE);
		temp = huart->hdmarx->Instance->NDTR;
		if (huart == &BLUETOOTH_HUART) {
			rx_len =  BLUETOOTH_RX_MAX_BUFLEN - temp;
			Uart_Callback_BLT_Handle(bluetooth_buff, rx_len);
			HAL_UART_Receive_DMA(huart, bluetooth_buff, BLUETOOTH_RX_MAX_BUFLEN);
		}
		__HAL_UART_ENABLE_IT(huart, UART_IT_IDLE);
	}
}

void bluetooth_init()
{
	HAL_UART_Receive_DMA(&BLUETOOTH_HUART, bluetooth_buff, BLUETOOTH_RX_MAX_BUFLEN);
	__HAL_UART_ENABLE_IT(&BLUETOOTH_HUART, UART_IT_IDLE);
}