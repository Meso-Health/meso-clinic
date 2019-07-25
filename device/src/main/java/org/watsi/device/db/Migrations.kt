package org.watsi.device.db

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration

class Migrations {
    companion object {
        @JvmField
        val MIGRATION_15_16: Migration = object : Migration(15, 16) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Android room uses integers to store booleans. 1 means true.
                database.execSQL("ALTER TABLE billables " + " ADD COLUMN active INTEGER NOT NULL DEFAULT 1 ")
            }
        }
    }
}
