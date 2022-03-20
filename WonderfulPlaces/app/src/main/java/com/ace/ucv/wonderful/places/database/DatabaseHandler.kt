package com.ace.ucv.wonderful.places.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.ace.ucv.wonderful.places.domain.WonderfulPlaceDO

class DatabaseHandler(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "wonderful_places_database"
        private const val TABLE_WONDERFUL_PLACE = "wonderful_places"
        private const val KEY_ID = "_id"
        private const val KEY_TITLE = "title"
        private const val KEY_IMAGE = "image"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_DATE = "date"
        private const val KEY_LOCATION = "location"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createWonderfulPlaceTable = ("CREATE TABLE " + TABLE_WONDERFUL_PLACE + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TITLE + " TEXT,"
                + KEY_IMAGE + " TEXT,"
                + KEY_DESCRIPTION + " TEXT,"
                + KEY_DATE + " TEXT,"
                + KEY_LOCATION + " TEXT,"
                + KEY_LATITUDE + " TEXT,"
                + KEY_LONGITUDE + " TEXT)")
        db?.execSQL(createWonderfulPlaceTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_WONDERFUL_PLACE")
        onCreate(db)
    }

    fun addWonderfulPlace(wonderfulPlace: WonderfulPlaceDO): Long {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(KEY_TITLE, wonderfulPlace.title)
        contentValues.put(KEY_IMAGE, wonderfulPlace.image)
        contentValues.put(KEY_DESCRIPTION, wonderfulPlace.description)
        contentValues.put(KEY_DATE, wonderfulPlace.date)
        contentValues.put(KEY_LOCATION, wonderfulPlace.location)
        contentValues.put(KEY_LATITUDE, wonderfulPlace.latitude)
        contentValues.put(KEY_LONGITUDE, wonderfulPlace.longitude)

        val result = db.insert(TABLE_WONDERFUL_PLACE, null, contentValues)

        db.close()
        return result
    }

    fun getWonderfulPlaces(): ArrayList<WonderfulPlaceDO> {
        val wonderfulPlaces: ArrayList<WonderfulPlaceDO> = ArrayList()
        val selectQuery = "SELECT  * FROM $TABLE_WONDERFUL_PLACE"
        val db = this.readableDatabase

        try {
            val cursor: Cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    val place = WonderfulPlaceDO(
                        cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                        cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                        cursor.getString(cursor.getColumnIndex(KEY_IMAGE)),
                        cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(KEY_DATE)),
                        cursor.getString(cursor.getColumnIndex(KEY_LOCATION)),
                        cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE))
                    )
                    wonderfulPlaces.add(place)

                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (sqLiteException: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }
        return wonderfulPlaces
    }

    fun updateWonderfulPlace(wonderfulPlace: WonderfulPlaceDO): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_TITLE, wonderfulPlace.title)
        contentValues.put(KEY_IMAGE, wonderfulPlace.image)
        contentValues.put(KEY_DESCRIPTION, wonderfulPlace.description)
        contentValues.put(KEY_DATE, wonderfulPlace.date)
        contentValues.put(KEY_LOCATION, wonderfulPlace.location)
        contentValues.put(KEY_LATITUDE, wonderfulPlace.latitude)
        contentValues.put(KEY_LONGITUDE, wonderfulPlace.longitude)

        val success = db.update(TABLE_WONDERFUL_PLACE, contentValues,
            KEY_ID + "=" + wonderfulPlace.id, null)

        db.close()
        return success
    }

    fun deleteWonderfulPlace(wonderfulPlace: WonderfulPlaceDO): Int {
        val db = this.writableDatabase
        val success = db.delete(TABLE_WONDERFUL_PLACE, KEY_ID + "=" + wonderfulPlace.id, null)
        db.close()
        return success
    }
}