package org.edward.app.di

import org.edward.app.data.local.dataStoreModule
import org.edward.app.data.network.networkModule
import org.edward.app.data.remote.remoteModule
import org.edward.app.presentations.screens.screenModelModule

fun appModule(context: Any?) = listOf(
    dataStoreModule(context), screenModelModule, remoteModule, networkModule
)