package org.edward.app.data.local

import org.edward.app.shared.createDataStore
import org.koin.dsl.module

fun dataStoreModule(context: Any?) = module {
    single<DataStoreRepository> {
        DataStoreRepositoryImpl(createDataStore(context))
    }
}