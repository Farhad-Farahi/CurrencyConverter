package com.fd.currencyconverter.presentation.currency_exchange

import com.fd.currencyconverter.data.repository.FakeCurrencyRepositoryImplTest
import com.fd.currencyconverter.domain.common.CommissionPlan
import com.fd.currencyconverter.domain.model.RateItem
import com.fd.currencyconverter.domain.usecase.GetCurrencyExchangesUseCase
import com.google.common.truth.Truth
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After


import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class CurrencyExchangeViewModelTest {

    private lateinit var fakeCurrencyRepositoryImplTest: FakeCurrencyRepositoryImplTest
    private lateinit var getCurrencyExchangesUseCase: GetCurrencyExchangesUseCase
    private lateinit var currencyExchangeViewModel: CurrencyExchangeViewModel


    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)

        fakeCurrencyRepositoryImplTest = FakeCurrencyRepositoryImplTest()
        getCurrencyExchangesUseCase = GetCurrencyExchangesUseCase(fakeCurrencyRepositoryImplTest)
        currencyExchangeViewModel = CurrencyExchangeViewModel(getCurrencyExchangesUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset the main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }


    /**
     * testing transfer data between 3 layers
     * repository - domain - ViewModel
     */
    @Test
    fun dataTransferFromFakeRepositoryShouldBeEqual_returnsTrue(): Unit = runBlocking {
        val exchangeModelResponse = currencyExchangeViewModel.exchanges.first()

        exchangeModelResponse.data?.apply {
            Truth.assertThat(
                base == FakeCurrencyRepositoryImplTest.exchangeModelResponse.base
                        && date == FakeCurrencyRepositoryImplTest.exchangeModelResponse.date
                        && success == FakeCurrencyRepositoryImplTest.exchangeModelResponse.success
                        && timestamp == FakeCurrencyRepositoryImplTest.exchangeModelResponse.timestamp
                        && rates.AED == FakeCurrencyRepositoryImplTest.exchangeModelResponse.rates.AED
                        && rates.AWG == FakeCurrencyRepositoryImplTest.exchangeModelResponse.rates.AWG
            ).isTrue()
        }
    }


    @Test
    fun updateCurrencyBalanceList_returnTrue() {

        val arrayList = ArrayList<RateItem>()
        val rateItem1 = RateItem("EUR", 999.0)
        val rateItem2 = RateItem("USD", 1.0)
        val rateItem3 = RateItem("AED", 5.0)
        arrayList.add(rateItem1)
        arrayList.add(rateItem2)
        arrayList.add(rateItem3)

        currencyExchangeViewModel.updateCurrencyBalanceList(arrayList)

        runBlocking {
            val balanceList = currencyExchangeViewModel.currencyBalanceList.first()
            Truth.assertThat(
                balanceList.contains(rateItem1) &&
                        balanceList.contains(rateItem2) &&
                        balanceList.contains(rateItem3)
            ).isTrue()
        }
    }


    @Test
    fun updateCurrencyBalanceList_returnFalse() {

        val arrayList = ArrayList<RateItem>()
        val rateItem2 = RateItem("USD", 1.0)
//        arrayList.add(rateItem2)

        currencyExchangeViewModel.updateCurrencyBalanceList(arrayList)

        runBlocking {
            val balanceList = currencyExchangeViewModel.currencyBalanceList.first()
            Truth.assertThat(
                balanceList.contains(rateItem2)
            ).isFalse()
        }
    }

    /**
     * we now currencyExchangeViewModel initial value is 5
     */
    @Test
    fun decreaseFreeConvertLeft_returnTrue() {
        currencyExchangeViewModel.decreaseFreeConvertLeft()

        runBlocking {
            val commissionFreeLeft = currencyExchangeViewModel.freeConvertLeft.first()
            Truth.assertThat(commissionFreeLeft==4).isTrue()
        }
    }
    @Test
    fun decreaseFreeConvertLeft_returnFalse() {
        currencyExchangeViewModel.decreaseFreeConvertLeft()
        currencyExchangeViewModel.decreaseFreeConvertLeft()

        runBlocking {
            val commissionFreeLeft = currencyExchangeViewModel.freeConvertLeft.first()
            Truth.assertThat(commissionFreeLeft==4).isFalse()
        }
    }

    /**
     * we now currencyExchangeViewModel initial value is 0
     */
    @Test
    fun increaseConvertTimeCount_returnTrue(){
        currencyExchangeViewModel.increaseConvertTimeCount()
        currencyExchangeViewModel.increaseConvertTimeCount()
        currencyExchangeViewModel.increaseConvertTimeCount()

        runBlocking {
            val convertTime = currencyExchangeViewModel.convertTimeCount.first()
            Truth.assertThat(convertTime==3).isTrue()
        }
    }

    @Test
    fun increaseConvertTimeCount_returnFalse(){
        currencyExchangeViewModel.increaseConvertTimeCount()
        currencyExchangeViewModel.increaseConvertTimeCount()
        currencyExchangeViewModel.increaseConvertTimeCount()

        runBlocking {
            val convertTime = currencyExchangeViewModel.convertTimeCount.first()
            Truth.assertThat(convertTime < 3).isFalse()
        }
    }

    @Test
    fun changeCommissionPlan_returnTrue(){
        currencyExchangeViewModel.changeCommissionPlan(CommissionPlan.Every5)

        runBlocking {
            val plan = currencyExchangeViewModel.commissionPlan.first()
            Truth.assertThat(plan == CommissionPlan.Every5).isTrue()
        }

    }
    @Test
    fun changeCommissionPlan_returnFalse(){
        currencyExchangeViewModel.changeCommissionPlan(CommissionPlan.First5)

        runBlocking {
            val plan = currencyExchangeViewModel.commissionPlan.first()
            Truth.assertThat(plan == CommissionPlan.Every5).isFalse()
        }

    }
    @Test
    fun refreshConvertItemCount_returnTrue(){
        currencyExchangeViewModel.increaseConvertTimeCount()
        currencyExchangeViewModel.increaseConvertTimeCount()
        currencyExchangeViewModel.increaseConvertTimeCount()

        currencyExchangeViewModel.refreshConvertItemCount()
        runBlocking {
            val convertTime = currencyExchangeViewModel.convertTimeCount.first()
            Truth.assertThat(convertTime==1).isTrue()
        }
    }

    @Test
    fun refreshConvertItemCount_returnFalse(){
        currencyExchangeViewModel.increaseConvertTimeCount()
        currencyExchangeViewModel.increaseConvertTimeCount()
        currencyExchangeViewModel.increaseConvertTimeCount()

        currencyExchangeViewModel.refreshConvertItemCount()

        currencyExchangeViewModel.increaseConvertTimeCount()

        runBlocking {
            val convertTime = currencyExchangeViewModel.convertTimeCount.first()
            Truth.assertThat(convertTime==1).isFalse()
        }
    }
}