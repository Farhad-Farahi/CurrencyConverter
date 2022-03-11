package com.fd.currencyconverter.domain.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


data class ExchangeModelResponse(
    @SerializedName("base")
    @Expose
    var base: String,
    @SerializedName("date")
    @Expose
    val date: String,
    @SerializedName("rates")
    @Expose
    val rates: Rates,
    @SerializedName("success")
    @Expose
    val success: Boolean,
    @SerializedName("timestamp")
    @Expose
    val timestamp: Int
)