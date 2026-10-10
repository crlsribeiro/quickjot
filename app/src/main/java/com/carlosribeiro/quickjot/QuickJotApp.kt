package com.carlosribeiro.quickjot

import android.app.Application
import com.carlosribeiro.quickjot.data.QuickJotDatabase
import com.carlosribeiro.quickjot.data.QuickJotRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class QuickJotApp : Application() {
    val repository: QuickJotRepository by lazy { QuickJotRepository(QuickJotDatabase.create(this)) }

    /** Outlives screens, so a save started while leaving the editor always completes. */
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
}
