package com.odontosystem.app

import android.app.Application
import com.odontosystem.app.data.local.SessionManager
import com.odontosystem.app.data.remote.RetrofitClient

class OdontoApplication : Application() {

    lateinit var sessionManager: SessionManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        sessionManager = SessionManager(this)
        RetrofitClient.init(sessionManager)
    }

    companion object {
        lateinit var instance: OdontoApplication
            private set
    }
}
