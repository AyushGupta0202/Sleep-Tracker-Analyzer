package com.example.android.trackmysleepquality.database

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface SleepDatabaseDao {

    @Insert
    suspend fun insert(night: SleepNight)

    @Query("SELECT * FROM daily_sleep_quality_table ORDER BY nightId DESC")
    fun getAllNights() : LiveData<List<SleepNight>>

    @Query("SELECT * FROM daily_sleep_quality_table WHERE nightId=:key")
    suspend fun get(key: Long): SleepNight?

    @Query("DELETE FROM daily_sleep_quality_table")
    suspend fun clear()

    @Update
    suspend fun update(night: SleepNight)

    @Query("SELECT * FROM daily_sleep_quality_table ORDER BY nightId DESC LIMIT 1")
    suspend fun getTonight(): SleepNight?

    /**
     * Selects and returns the night with given nightId.
     */
    @Query("SELECT * from daily_sleep_quality_table WHERE nightId = :key")
    fun getNightWithId(key: Long): LiveData<SleepNight>
}
