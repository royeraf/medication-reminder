package com.medicationreminder.di

import com.medicationreminder.data.repository.DoseLogRepositoryImpl
import com.medicationreminder.data.repository.MedicationRepositoryImpl
import com.medicationreminder.data.repository.RxNormRepositoryImpl
import com.medicationreminder.domain.repository.DoseLogRepository
import com.medicationreminder.domain.repository.MedicationRepository
import com.medicationreminder.domain.repository.RxNormRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMedicationRepository(
        impl: MedicationRepositoryImpl
    ): MedicationRepository

    @Binds
    @Singleton
    abstract fun bindDoseLogRepository(
        impl: DoseLogRepositoryImpl
    ): DoseLogRepository

    @Binds
    @Singleton
    abstract fun bindRxNormRepository(
        impl: RxNormRepositoryImpl
    ): RxNormRepository
}
