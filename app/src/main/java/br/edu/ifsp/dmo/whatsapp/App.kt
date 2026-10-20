package br.edu.ifsp.dmo.whatsapp

import android.app.Application
import br.edu.ifsp.dmo.whatsapp.data.remote.ApiClient
import br.edu.ifsp.dmo.whatsapp.data.remote.SessionStore

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        session = SessionStore(this)
        api = ApiClient(session)
    }

    companion object {
        lateinit var instance: App
            private set
        lateinit var session: SessionStore
            private set
        lateinit var api: ApiClient
            private set
    }
}
