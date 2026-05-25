package org.edward.app.di

import org.edward.app.data.local.dataStoreModule

fun appModule(context: Any?) = listOf(
    dataStoreModule(context),
)