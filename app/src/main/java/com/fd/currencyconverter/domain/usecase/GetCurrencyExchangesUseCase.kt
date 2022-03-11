package com.fd.currencyconverter.domain.usecase


import com.fd.currencyconverter.domain.common.NetworkResult
import com.fd.currencyconverter.domain.model.ExchangeModelResponse
import com.fd.currencyconverter.domain.repository.CurrencyRepository
import kotlinx.coroutines.flow.Flow

import javax.inject.Inject


class GetCurrencyExchangesUseCase @Inject constructor(
    private val repository: CurrencyRepository
) {
    suspend operator fun invoke(): Flow<NetworkResult<ExchangeModelResponse>> {
        return repository.getCurrencies()
    }
}
