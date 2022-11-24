package com.kuluruvineeth.di

import com.kuluruvineeth.repository.user.FakeUserRepository
import org.koin.dsl.module

internal val testModule = module {
    single{
        FakeUserRepository()
    }
}