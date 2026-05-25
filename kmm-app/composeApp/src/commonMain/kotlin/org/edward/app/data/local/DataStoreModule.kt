package org.edward.app.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.edward.app.shared.createDataStore
import org.koin.dsl.module

fun dataStoreModule(context: Any?) = module {
    single<DataStore<Preferences>> { createDataStore(context) }
}