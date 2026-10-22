package com.jorge.cervelog

import android.app.Application
import com.jorge.cervelog.di.AppContainer

class CerveLogApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
