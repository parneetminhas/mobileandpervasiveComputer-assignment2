package com.smartcartbuddy

import android.content.Context
import android.content.SharedPreferences

class Common(private val context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_pref", Context.MODE_PRIVATE)

    val userId: String?
        get() = sharedPreferences.getString("user_id", null)
}