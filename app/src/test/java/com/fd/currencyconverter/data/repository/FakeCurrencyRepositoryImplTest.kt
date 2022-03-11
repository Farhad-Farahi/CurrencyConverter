package com.fd.currencyconverter.data.repository

import com.fd.currencyconverter.domain.common.NetworkResult
import com.fd.currencyconverter.domain.model.ExchangeModelResponse
import com.fd.currencyconverter.domain.model.Rates
import com.fd.currencyconverter.domain.repository.CurrencyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.junit.Assert.*

class FakeCurrencyRepositoryImplTest : CurrencyRepository {

    companion object {
        private fun getData(exchangeModelResponse: ExchangeModelResponse): NetworkResult.Success<ExchangeModelResponse> {
            return NetworkResult.Success(data = exchangeModelResponse)
        }

        private val rates = Rates()
        val exchangeModelResponse = ExchangeModelResponse(
            base = "baseTest",
            date = "dateTest",
            rates = rates,
            success = true,
            timestamp = 10
        )
        val networkResult: NetworkResult<ExchangeModelResponse> = getData(exchangeModelResponse)
    }

    override suspend fun getCurrencies(): Flow<NetworkResult<ExchangeModelResponse>> {
        return flow {
            emit(networkResult)
        }
    }
}