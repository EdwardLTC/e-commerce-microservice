package org.edward.app.presentations.screens

import org.edward.app.data.local.CartRepository
import org.edward.app.presentations.screens.auth.login.LoginScreenModel
import org.edward.app.presentations.screens.auth.register.RegisterScreenModel
import org.edward.app.presentations.screens.main.cart.CartScreenModel
import org.edward.app.presentations.screens.main.home.HomeScreenModel
import org.edward.app.presentations.screens.main.home.detail.ProductDetailScreenModel
import org.edward.app.presentations.screens.main.profile.ProfileScreenModel
import org.edward.app.presentations.screens.main.profile.settings.SettingsScreenModel
import org.koin.dsl.module

val screenModelModule = module {
    single { CartRepository() }
    factory { LoginScreenModel(get(), get()) }
    factory { RegisterScreenModel(get(), get()) }
    single { HomeScreenModel(get()) }
    factory { ProductDetailScreenModel(get(), get()) }
    factory { CartScreenModel(get()) }
    factory { ProfileScreenModel(get(), get()) }
    factory { SettingsScreenModel(get()) }
}
