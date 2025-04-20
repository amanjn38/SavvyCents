package com.finance.savvycents.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.finance.savvycents.SavvyCentsDatabase
import com.finance.savvycents.contact.ContactUseCase
import com.finance.savvycents.contact.ContactUseCaseImpl
import com.finance.savvycents.contact.LocalContactDataSource
import com.finance.savvycents.contact.RemoteContactDataSource
import com.finance.savvycents.dao.CategoryDao
import com.finance.savvycents.dao.ContactDao
import com.finance.savvycents.dao.FriendDao
import com.finance.savvycents.repository.AuthRepository
import com.finance.savvycents.repository.AuthRepositoryImpl
import com.finance.savvycents.repository.CategoryRepository
import com.finance.savvycents.repository.CategoryRepositoryImpl
import com.finance.savvycents.repository.ContactRepository
import com.finance.savvycents.repository.ContactRepositoryImpl
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

    @Provides
    @Singleton
    fun provideContactRepository(
        remoteDataSource: RemoteContactDataSource,
        localDataSource: LocalContactDataSource
    ): ContactRepository {
        return ContactRepositoryImpl(remoteDataSource, localDataSource)
    }

    @Provides
    @Singleton
    fun provideLocalContactDataSource(contactDao: ContactDao): LocalContactDataSource {
        return LocalContactDataSource(contactDao)
    }

    @Provides
    @Singleton
    fun provideContactUseCase(repository: ContactRepository): ContactUseCase {
        return ContactUseCaseImpl(repository)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): SavvyCentsDatabase {
        return Room.databaseBuilder(
            context,
            SavvyCentsDatabase::class.java,
            "contacts"
        ).build()
    }

    @Provides
    @Singleton
    fun provideContactDao(appDatabase: SavvyCentsDatabase): ContactDao {
        return appDatabase.contactDao()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(appDatabase: SavvyCentsDatabase): CategoryDao {
        return appDatabase.categoryDao()
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(
        categoryDao: CategoryDao
    ): CategoryRepository {
        return CategoryRepositoryImpl(categoryDao)
    }

    @Provides
    @Singleton
    fun provideCategoryViewModel(repository: CategoryRepository): CategoryViewModel {
        return CategoryViewModel(repository)
    }
    @Provides
    @Singleton
    fun provideFriendDao(appDatabase: SavvyCentsDatabase): FriendDao {
        return appDatabase.friendDao()
    }
}