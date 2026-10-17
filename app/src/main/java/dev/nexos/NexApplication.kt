package dev.nexos

import android.app.Application
import dev.nexos.data.preferences.UserPreferencesRepository
import dev.nexos.data.repository.FileRepository
import dev.nexos.runtime.RuntimeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NexApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    lateinit var preferencesRepository: UserPreferencesRepository
        private set

    lateinit var fileRepository: FileRepository
        private set

    lateinit var runtimeProvider: RuntimeProvider
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        preferencesRepository = UserPreferencesRepository(this)
        fileRepository = FileRepository(this)
        runtimeProvider = RuntimeProvider(this)

        applicationScope.launch {
            val initialPrefs = preferencesRepository.userPreferencesFlow.first()
            runtimeProvider.setRuntimeType(initialPrefs.activeRuntimeType)
        }
    }

    companion object {
        lateinit var instance: NexApplication
            private set
    }
}
