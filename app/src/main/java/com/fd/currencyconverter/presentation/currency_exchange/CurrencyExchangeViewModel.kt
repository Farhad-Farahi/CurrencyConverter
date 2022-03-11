package com.fd.currencyconverter.presentation.currency_exchange

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fd.currencyconverter.domain.common.CommissionPlan
import com.fd.currencyconverter.domain.common.NetworkResult
import com.fd.currencyconverter.domain.model.ExchangeModelResponse
import com.fd.currencyconverter.domain.model.RateItem
import com.fd.currencyconverter.domain.model.Rates
import com.fd.currencyconverter.domain.usecase.GetCurrencyExchangesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import kotlin.reflect.full.memberProperties

@HiltViewModel
class CurrencyExchangeViewModel @Inject constructor(
    private val getCurrencyExchangesUseCase: GetCurrencyExchangesUseCase
) : ViewModel() {

    // Exchange model response (this is whole response)
    private var _exchanges: MutableStateFlow<NetworkResult<ExchangeModelResponse>> =
        MutableStateFlow(NetworkResult.Loading())
    val exchanges: StateFlow<NetworkResult<ExchangeModelResponse>> = _exchanges

    // shouldSequentiallyRequest boolean value for permissions to network request loop
    private var shouldSequentiallyRequest = true

    // freeConvertLeft is the amount of convert that user has commission free
    // it should be sync from server.(i made dummy "5" time left here)
    private var _freeConvertLeft: MutableStateFlow<Int> = MutableStateFlow(5)
    val freeConvertLeft: StateFlow<Int> = _freeConvertLeft

    //currencyBalanceList is for list of our balances that we show in recyclerview
    var currencyBalanceList: MutableStateFlow<List<RateItem>> = MutableStateFlow(arrayListOf())

    //counting convert time for (every x convert free commission plan)
    private var _convertTimeCount : MutableStateFlow<Int> = MutableStateFlow(0)
    val convertTimeCount : StateFlow<Int> = _convertTimeCount

    private var _commissionPlan : MutableStateFlow<CommissionPlan> =  MutableStateFlow(CommissionPlan.First5)
    val commissionPlan : StateFlow<CommissionPlan> = _commissionPlan

    // hashMap is keeping our balance list with (key,value)
    // so we can put and retrieve and replace data so fast
    private val hashMap = HashMap<String, Double>()
    private val list = arrayListOf<RateItem>()

    private val currencyNames: List<String> = Rates::class.memberProperties.map {
        it.name
    }

    //requestJob will request for network call sequentially(every 5 second here)
    private var requestJob: Job? = null


    init {
        getCurrencyExchanges()
        requestJob = startRepeatingJob(50000L)

        createStarterDummyListForBalances()
    }

    fun decreaseFreeConvertLeft() {
        _freeConvertLeft.value -= 1
    }
    fun increaseConvertTimeCount(){
        _convertTimeCount.value +=1
    }
    fun refreshConvertItemCount(){
        _convertTimeCount.value=1
    }
    fun changeCommissionPlan(newPlan : CommissionPlan){
        _commissionPlan.value = newPlan
    }

    private fun createStarterDummyListForBalances() {
        for (currency in currencyNames) {
            hashMap[currency] = 0.0
        }
        hashMap["EUR"] = 1000.0
        val map = hashMap.toList()
            .sortedByDescending { (_, value) -> value }
            .toMap()

        //create list for passing to recyclerview

        for (item in map) {
            list.add(RateItem(currencyName = item.key, currencyValue = item.value))
        }
        currencyBalanceList.value = list
    }

    fun updateCurrencyBalanceList(list: List<RateItem>) {
        for (item in list) {
            hashMap[item.currencyName] = item.currencyValue
        }
        val map = hashMap.toList()
            .sortedByDescending { (_, value) -> value }
            .toMap()

        val newList = arrayListOf<RateItem>()
        for (item in map) {
            newList.add(RateItem(currencyName = item.key, currencyValue = item.value))
        }
        currencyBalanceList.value = newList
    }

    /**
     * collecting data from GetCurrencyExchangesUseCase.kt
     */
    private fun getCurrencyExchanges() = viewModelScope.launch {
        getCurrencyExchangesUseCase.invoke().collect {
            _exchanges.value = it
        }
    }


    private fun startRepeatingJob(timeInterval: Long): Job {

        return CoroutineScope(Dispatchers.IO).launch {
            while (shouldSequentiallyRequest) {
                Log.d("startRepeatingJob", "requesting...")
                getCurrencyExchanges()
                delay(timeInterval)
            }
        }
    }

    private fun stopNetworkCall() {
        shouldSequentiallyRequest = false
        requestJob?.cancel()
        requestJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopNetworkCall()
    }
}