package com.fd.currencyconverter.domain.common

interface commissionInterface{
    val index : Int
}

enum class CommissionPlan : commissionInterface{
    First5 {
        override val index: Int
            get() = 0
    },
    Every5 {
        override val index: Int
            get() = 1
    }
}