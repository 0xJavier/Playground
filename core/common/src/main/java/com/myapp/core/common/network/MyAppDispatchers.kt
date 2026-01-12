package com.myapp.core.common.network

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val myAppDispatcher: MyAppDispatchers)

enum class MyAppDispatchers {
    Default,
    IO,
}
