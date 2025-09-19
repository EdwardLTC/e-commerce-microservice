package org.edward.app.shared

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.edward.app.data.local.AppSetting
import org.edward.app.data.local.dataStoreFileName

actual fun createDataStore(context: Any?): DataStore<Preferences> {
    require(
        value = context is Context,
        lazyMessage = { "Context must be of type android.content.Context" }
    )

    return AppSetting.getDataStore(producerPath = {
        context.filesDir.resolve(dataStoreFileName).absolutePath
    })
}