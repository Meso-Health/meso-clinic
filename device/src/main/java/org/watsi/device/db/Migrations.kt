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

        @JvmField
        val MIGRATION_16_17: Migration = object : Migration(16, 17) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // This will help with many queries in encounterDao that has orderBy occurredAt
                database.execSQL("CREATE INDEX `index_encounters_occurredAt` ON `encounters` (`occurredAt`)")

                // This will help with loading checked-in member screen and member detail screen, which rely on
                // needing to join encounters with members with identification events to know whether a member is
                // checked in or not.
                // Specifically, these indices will help with memberDao.isMemberCheckedIn and memberDao.checkedInMembers
                database.execSQL("CREATE INDEX `index_encounters_identificationEventId` on `encounters` (`identificationEventId`)")
                database.execSQL("CREATE INDEX `index_identification_events_occurredAt` ON `identification_events` (`occurredAt`)")
            }
        }

        @JvmField
        val MIGRATION_17_18: Migration = object : Migration(17, 18) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // This index will help with memberDao.findMemberRelationsByNames
                database.execSQL("CREATE INDEX `index_members_name` ON `members` (`name`)")
            }
        }

        @JvmField
        val MIGRATION_18_19: Migration = object : Migration(18, 19) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Android room uses integers to store booleans. 1 means true.
                database.execSQL("ALTER TABLE diagnoses " + " ADD COLUMN active INTEGER NOT NULL DEFAULT 1 ")
            }
        }

        @JvmField
        val MIGRATION_24_25: Migration = object : Migration(24, 25) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Android room uses integers to store booleans. 1 means true.
                database.execSQL("ALTER TABLE encounter_items " + " ADD COLUMN surgicalScore INTEGER DEFAULT NULL ")
            }
        }

    }
}
