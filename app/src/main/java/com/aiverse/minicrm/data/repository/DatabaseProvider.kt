package com.aiverse.minicrm.data.repository

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseProvider {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    // Migration for Customers table
    val MIGRATION_1_2_CUSTOMERS = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create a new table with the exact schema Room expects
            database.execSQL("""
                CREATE TABLE customers_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    email TEXT NOT NULL,
                    phone TEXT,
                    company TEXT,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL DEFAULT 0,
                    isSynced INTEGER NOT NULL DEFAULT 0
                )
            """)

            // Copy existing data and set default values for new columns
            database.execSQL("""
                INSERT INTO customers_new (id, name, email, phone, company, createdAt, updatedAt, isSynced)
                SELECT id, name, email, phone, company, createdAt, 0, 0 FROM customers
            """)

            // Drop old table and rename
            database.execSQL("DROP TABLE customers")
            database.execSQL("ALTER TABLE customers_new RENAME TO customers")
        }
    }

    // Migration for Orders table
    val MIGRATION_1_2_ORDERS = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create new orders table with exact schema Room expects
            database.execSQL("""
                CREATE TABLE orders_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    customerId INTEGER NOT NULL,
                    orderDate INTEGER NOT NULL,
                    amount REAL NOT NULL DEFAULT 0,
                    isSynced INTEGER NOT NULL DEFAULT 0
                )
            """)

            // Copy existing data and set default values for new columns
            database.execSQL("""
                INSERT INTO orders_new (id, customerId, orderDate, amount, isSynced)
                SELECT id, customerId, orderDate, amount, 0 FROM orders
            """)

            // Drop old table and rename
            database.execSQL("DROP TABLE orders")
            database.execSQL("ALTER TABLE orders_new RENAME TO orders")
        }
    }

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "mini_crm_db"
            )
                .addMigrations(MIGRATION_1_2_CUSTOMERS, MIGRATION_1_2_ORDERS)
                .fallbackToDestructiveMigration() // Optional: ensures no crash if migration fails
                .build()
            INSTANCE = instance
            instance
        }
    }
}
