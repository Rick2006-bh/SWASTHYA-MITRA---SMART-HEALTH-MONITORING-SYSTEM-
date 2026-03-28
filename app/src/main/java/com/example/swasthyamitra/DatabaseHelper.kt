package com.example.swasthyamitra

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "SwasthyaMitra.db"
        private const val DATABASE_VERSION = 3 // Incremented version to add more columns
        
        const val TABLE_USERS = "users"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_GENDER = "gender"
        const val COLUMN_DOB = "dob"

        const val TABLE_HISTORY = "health_history"
        const val COLUMN_HIST_ID = "id"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_DATE = "date"
        const val COLUMN_HEART_RATE = "heart_rate"
        const val COLUMN_SPO2 = "spo2"
        const val COLUMN_TEMP = "temp"
        const val COLUMN_BMI = "bmi"
        const val COLUMN_ECG = "ecg"
        const val COLUMN_STRESS = "stress"
        const val COLUMN_RESPIRATORY = "respiratory"
        const val COLUMN_HEIGHT = "height"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = ("CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_EMAIL + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_GENDER + " TEXT,"
                + COLUMN_DOB + " TEXT" + ")")
        db.execSQL(createUsersTable)

        val createHistoryTable = ("CREATE TABLE " + TABLE_HISTORY + "("
                + COLUMN_HIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID + " TEXT,"
                + COLUMN_DATE + " TEXT,"
                + COLUMN_HEART_RATE + " TEXT,"
                + COLUMN_SPO2 + " TEXT,"
                + COLUMN_TEMP + " TEXT,"
                + COLUMN_BMI + " TEXT,"
                + COLUMN_ECG + " TEXT,"
                + COLUMN_STRESS + " TEXT,"
                + COLUMN_RESPIRATORY + " TEXT,"
                + COLUMN_HEIGHT + " TEXT" + ")")
        db.execSQL(createHistoryTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_HISTORY ($COLUMN_HIST_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_USER_ID TEXT, $COLUMN_DATE TEXT, $COLUMN_HEART_RATE TEXT, $COLUMN_SPO2 TEXT, $COLUMN_TEMP TEXT, $COLUMN_BMI TEXT)")
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE $TABLE_HISTORY ADD COLUMN $COLUMN_ECG TEXT")
            db.execSQL("ALTER TABLE $TABLE_HISTORY ADD COLUMN $COLUMN_STRESS TEXT")
            db.execSQL("ALTER TABLE $TABLE_HISTORY ADD COLUMN $COLUMN_RESPIRATORY TEXT")
            db.execSQL("ALTER TABLE $TABLE_HISTORY ADD COLUMN $COLUMN_HEIGHT TEXT")
        }
    }

    fun addUser(name: String, email: String, pass: String, gender: String, dob: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NAME, name)
        values.put(COLUMN_EMAIL, email)
        values.put(COLUMN_PASSWORD, pass)
        values.put(COLUMN_GENDER, gender)
        values.put(COLUMN_DOB, dob)
        val success = db.insert(TABLE_USERS, null, values)
        db.close()
        return success
    }

    fun checkUser(email: String, pass: String): Boolean {
        val db = this.readableDatabase
        val columns = arrayOf(COLUMN_ID)
        val selection = "$COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?"
        val selectionArgs = arrayOf(email, pass)
        val cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null)
        val count = cursor.count
        cursor.close()
        db.close()
        return count > 0
    }

    fun addHealthRecord(userId: String, date: String, hr: String, spo2: String, temp: String, bmi: String, ecg: String, stress: String, respiratory: String, height: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_USER_ID, userId)
        values.put(COLUMN_DATE, date)
        values.put(COLUMN_HEART_RATE, hr)
        values.put(COLUMN_SPO2, spo2)
        values.put(COLUMN_TEMP, temp)
        values.put(COLUMN_BMI, bmi)
        values.put(COLUMN_ECG, ecg)
        values.put(COLUMN_STRESS, stress)
        values.put(COLUMN_RESPIRATORY, respiratory)
        values.put(COLUMN_HEIGHT, height)
        val success = db.insert(TABLE_HISTORY, null, values)
        db.close()
        return success
    }

    fun getHealthHistory(email: String): List<Map<String, String>> {
        val list = mutableListOf<Map<String, String>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_HISTORY WHERE $COLUMN_USER_ID = ? ORDER BY $COLUMN_HIST_ID DESC", arrayOf(email))
        
        if (cursor.moveToFirst()) {
            do {
                val map = mutableMapOf<String, String>()
                map["date"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
                map["hr"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HEART_RATE))
                map["spo2"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SPO2))
                map["temp"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEMP))
                map["bmi"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BMI))
                map["ecg"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ECG)) ?: "N/A"
                map["stress"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STRESS)) ?: "N/A"
                map["respiratory"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RESPIRATORY)) ?: "N/A"
                map["height"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HEIGHT)) ?: "N/A"
                list.add(map)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }
}
