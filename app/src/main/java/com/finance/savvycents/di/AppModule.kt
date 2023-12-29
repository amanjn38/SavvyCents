package com.finance.savvycents.di

import android.content.Context
import android.content.SharedPreferences
import com.finance.savvycents.repository.AuthRepository
import com.finance.savvycents.repository.AuthRepositoryImpl
import com.finance.savvycents.repository.TransactionRepository
import com.finance.savvycents.repository.TransactionRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext appContext: Context): Context {
        return appContext
    }

    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    fun providesAuthRepository(impl: AuthRepositoryImpl): AuthRepository = impl

    @Provides
    @Singleton
    fun providesSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("UserLoggedInStatus", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    fun provideTransactionRepository(firestore: FirebaseFirestore): TransactionRepository {
        return TransactionRepositoryImpl(firestore)
    }
}