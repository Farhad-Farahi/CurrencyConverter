package com.fd.currencyconverter.domain.usecase

import com.fd.currencyconverter.data.repository.FakeCurrencyRepositoryImplTest
import kotlinx.coroutines.flow.first
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.runBlocking
import com.google.common.truth.Truth.assertThat

class GetCurrencyExchangesUseCaseTest {

    private lateinit var getCurrencyExchangesUseCase: GetCurrencyExchangesUseCase
    private lateinit var fakeCurrencyRepositoryImplTest: FakeCurrencyRepositoryImplTest


    @Before
    fun setUp() {
        fakeCurrencyRepositoryImplTest = FakeCurrencyRepositoryImplTest()
        getCurrencyExchangesUseCase = GetCurrencyExchangesUseCase(fakeCurrencyRepositoryImplTest)
    }


    @Test
    fun dataTransferFromFakeRepositoryShouldBeEqual_returnsTrue(): Unit = runBlocking {
        val exchangeModelResponse = getCurrencyExchangesUseCase.invoke().first()
        exchangeModelResponse.data?.apply {
            assertThat(
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
    fun dataTransferFromFakeRepositoryShouldBeEqual_returnsFalse(): Unit = runBlocking {
        val exchangeModelResponse = getCurrencyExchangesUseCase.invoke().first()
        exchangeModelResponse.data?.apply {
            assertThat(base == "x").isFalse()
        }
    }
}