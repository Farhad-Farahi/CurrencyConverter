package com.fd.currencyconverter.domain.common

import java.math.BigDecimal


fun Double.toBigDecimalWithRound(scale : Int) : BigDecimal {
    val bigDecimal = BigDecimal(this)
    return bigDecimal.setScale(scale, BigDecimal.ROUND_HALF_UP)
}

