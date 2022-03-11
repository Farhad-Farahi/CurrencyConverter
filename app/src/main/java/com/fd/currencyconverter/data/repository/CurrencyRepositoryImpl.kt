package com.fd.currencyconverter.data.repository

import com.fd.currencyconverter.data.remote.BaseApiResponse
import com.fd.currencyconverter.domain.common.NetworkResult
import com.fd.currencyconverter.domain.model.ExchangeModelResponse
import com.fd.currencyconverter.domain.repository.CurrencyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject


class CurrencyRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) : CurrencyRepository, BaseApiResponse() {

    override suspend fun getCurrencies(): Flow<NetworkResult<ExchangeModelResponse>> {
        return flow {
            emit(safeApiCall {
                remoteDataSource.getFeature()
            })
        }.flowOn(Dispatchers.IO)
    }
}