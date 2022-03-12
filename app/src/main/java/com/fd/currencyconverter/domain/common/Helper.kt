package com.fd.currencyconverter.domain.common

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog


object Helper {
    fun createDialog(
        context: Context, title: String, message: String,
        positiveName: String
    ): AlertDialog {
        return AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveName) { _, _ -> }.create()
    }
}