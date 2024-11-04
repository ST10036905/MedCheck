package com.example.medcheck.Database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast


private const val DATABASE_NAME = "MedCheck.db"
private const val DATABASE_VERSION = 1
private const val TABLE_MEDICINE = "medicine"
private const val COLUMN_ID = "id"
private const val COLUMN_MEDICINE_NAME = "medicineName"
private const val COLUMN_STRENGTH = "strength"
private const val COLUMN_FREQUENCY = "frequency"
private const val createTableQuery =
	"CREATE TABLE $TABLE_MEDICINE (" +
			"$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
			"$COLUMN_MEDICINE_NAME TEXT, " +
			"$COLUMN_STRENGTH TEXT, " +
			"$COLUMN_FREQUENCY TEXT)"

class DatabaseHandler(context: Context?) :
		SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
	private val appContext = context?.applicationContext
		/**
		 *  DB Helper class helps with creating and
		 * accessing a database, it also makes it easy to have a different
		 * version of the database. The onUpgrade method will get called
		 * if the version of the database that is stored is not the same as
		 * the version that is required by the code
		 *
		 *  using the onCreate method to  creating a database if it does
		 * not exist yet.
		 */
		override fun onCreate(db: SQLiteDatabase?) {
			try {
				db?.execSQL(createTableQuery)
				Toast.makeText(appContext, "Table created", Toast.LENGTH_SHORT).show()
			} catch (e: Exception) {
				Toast.makeText(appContext, e.message, Toast.LENGTH_SHORT).show()
			}
		}
		
		override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
			// Here you can handle database schema changes when the version number changes
			db?.execSQL("DROP TABLE IF EXISTS $TABLE_MEDICINE")
			onCreate(db)
		}
		
		fun addMedicine(medicineName: String, strength: String, frequency: String): Long {
			val db = this.writableDatabase
			val contentValues = ContentValues()
			contentValues.put(COLUMN_MEDICINE_NAME, medicineName)
			contentValues.put(COLUMN_STRENGTH, strength)
			contentValues.put(COLUMN_FREQUENCY, frequency)
			val result = db.insert(TABLE_MEDICINE, null, contentValues)
			db.close()
			return result
		}
		
		fun getAllMedicines(): Cursor {
			val db = this.readableDatabase
			return db.rawQuery("SELECT * FROM $TABLE_MEDICINE", null)
		}
	}