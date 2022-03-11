package com.fd.currencyconverter.domain.repository

import com.fd.currencyconverter.domain.common.NetworkResult
import com.fd.currencyconverter.domain.model.ExchangeModelResponse
import kotlinx.coroutines.flow.Flow


interface CurrencyRepository {
    suspend fun getCurrencies(): Flow<NetworkResult<ExchangeModelResponse>>
}