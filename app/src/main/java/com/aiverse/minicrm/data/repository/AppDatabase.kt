package com.aiverse.minicrm.data.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aiverse.minicrm.data.model.CustomerEntity
import com.aiverse.minicrm.data.model.OrderEntity

@Database(
    entities = [CustomerEntity::class, OrderEntity::class],
    version = 2 // incremented from 1 to 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun orderDao(): OrderDao
}
