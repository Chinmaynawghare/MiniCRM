package com.aiverse.minicrm.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int,
    val orderTitle: String,
    val orderAmount: Double,
    val orderDate: Long,
    val isSynced: Int = 0,           // Use Int (0/1) for Room migrations
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
