package com.example.wheretopark.data.local.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.wheretopark.data.local.entity.Favorite
import io.reactivex.rxjava3.core.Flowable

@Dao
interface FavoriteDao {

    @Upsert
    suspend fun addFavorite(favorite: Favorite)

    @Delete
    suspend fun deleteFavorite(favorite: Favorite)

    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): Flowable<List<Favorite>>

    @Query("DELETE FROM favorites")
    fun deleteAllFavorites(): Flowable<List<Favorite>>

}