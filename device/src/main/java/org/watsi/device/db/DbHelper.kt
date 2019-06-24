package org.watsi.device.db

/**
 * This class contains db constants and other helper methods.
 */
object DbHelper {
    const val DB_NAME = "submission"

    // Maximum number of parameters allowed in a single SQLite statement (see: https://www.sqlite.org/limits.html)
    const val SQLITE_MAX_VARIABLE_NUMBER = 999
}
