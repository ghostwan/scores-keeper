package com.ghostwan.scoreskeeper.utils

import android.content.Context
import android.widget.Toast

object Debug {
    fun toast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}