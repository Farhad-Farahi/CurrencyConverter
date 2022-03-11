package com.fd.currencyconverter.data.repository

import com.fd.currencyconverter.data.remote.CurrencyApi
import javax.inject.Inject


class RemoteDataSource @Inject constructor(private val currencyApi: CurrencyApi) {
    suspend fun getFeature() = currencyApi.getCurrencies()
}