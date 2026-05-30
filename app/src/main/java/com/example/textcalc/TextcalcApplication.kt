package com.example.textcalc

import android.app.Application
import com.example.textcalc.data.TextcalcDatabase

class TextcalcApplication : Application() {
    val database: TextcalcDatabase by lazy { TextcalcDatabase.getDatabase(this) }
}
