package com.aiverse.minicrm.data.repository
import androidx.room.*
import com.aiverse.minicrm.data.model.CustomerEntity

@Dao
interface CustomerDao {

    @Insert
    suspend fun insert(customer: CustomerEntity): Long

    @Update
    suspend fun update(customer: CustomerEntity)

    @Delete
    suspend fun delete(customer: CustomerEntity)

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getById(id: Int): CustomerEntity?

    @Query("SELECT * FROM customers")
    suspend fun getAll(): List<CustomerEntity>  // ✅ add this

    @Query("SELECT * FROM customers WHERE isSynced = 0")
    suspend fun getUnsyncedCustomers(): List<CustomerEntity>

    @Query("UPDATE customers SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Int)

    @Query("UPDATE customers SET isSynced = 1 WHERE id = :customerId")
    suspend fun markCustomerAsSynced(customerId: Int)

}
