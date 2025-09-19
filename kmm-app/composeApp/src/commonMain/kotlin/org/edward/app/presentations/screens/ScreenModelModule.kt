package org.edward.app.presentations.screens

import org.edward.app.presentations.screens.auth.login.LoginScreenModel
import org.edward.app.presentations.screens.main.home.HomeScreenModel
import org.edward.app.presentations.screens.main.profile.ProfileScreenModel
import org.edward.app.presentations.screens.main.profile.settings.SettingsScreenModel
import org.koin.dsl.module

val screenModelModule = module {
    factory { LoginScreenModel(get(), get()) }
    factory { HomeScreenModel(get()) }
    factory { ProfileScreenModel(get()) }
    factory { SettingsScreenModel(get()) }
}