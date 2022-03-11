package com.fd.currencyconverter.data.remote

import com.fd.currencyconverter.domain.common.Constants
import com.fd.currencyconverter.domain.model.ExchangeModelResponse
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Query


interface CurrencyApi {

    @POST(EndPoints.Currencies)
    suspend fun getCurrencies(
        @Query("access_key") apiKey: String = Constants.API_KEY
    ): Response<ExchangeModelResponse>
}