package com.brocoders.taaza_khabar.di

import android.content.Context
import androidx.room.Room
import com.brocoders.taaza_khabar.data.database.BookmarkDao
import com.brocoders.taaza_khabar.data.database.NewsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideNewsDatabase(@ApplicationContext context: Context): NewsDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            NewsDatabase::class.java,
            "news_database"
        ).build()
    }

    @Provides
    fun provideBookmarkDao(database: NewsDatabase): BookmarkDao {
        return database.bookmarkDao()
    }
} 