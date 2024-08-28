package com.example.myapplication.di

import com.example.myapplication.data.PlacesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent


@Module
@InstallIn(ViewModelComponent::class)
class RepositoryModule {

    @Provides
    fun providePlacesRepo() = PlacesRepository()
}