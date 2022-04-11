package com.example.opticsclientsapp.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.opticsclientsapp.models.Client


class DbHelper(context: Context) :
    SQLiteOpenHelper(context, Constants.DB_NAME, null, Constants.DB_VERSION), DbService {
    override fun onCreate(p0: SQLiteDatabase?) {
        val query =
            "create table ${Constants.TABLE_NAME} (" +
                    "${Constants.ID} integer primary key autoincrement, " +
                    "${Constants.FULL_NAME} text, " +
                    "${Constants.PHONE_NUMBER} text," +
                    "${Constants.DATE_OF_BIRTH} text," +
                    "${Constants.IMG_PROFILE} text," +
                    "${Constants.GENDER} integer," +
                    "${Constants.PRODUCT_NAME} text," +
                    "${Constants.DIOPTRE} text," +
                    "${Constants.WEARING_TIME} integer," +
                    "${Constants.DATE_OF_PURCHASE} text, " +
                    "${Constants.NOTES} text, " +
                    "${Constants.IS_NOTIFICATION_SEEN} integer" +
                    ")"
        p0?.execSQL(query)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {}

    override fun addClient(client: Client): Int {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(Constants.FULL_NAME, client.fullname)
        cv.put(Constants.PHONE_NUMBER, client.phoneNum)
        cv.put(Constants.DATE_OF_BIRTH, client.dateOfBirth)
        cv.put(Constants.IMG_PROFILE, client.imgProfile)
        cv.put(Constants.GENDER, client.gender)
        cv.put(Constants.PRODUCT_NAME, client.productName)
        cv.put(Constants.DIOPTRE, client.dioptre)
        cv.put(Constants.WEARING_TIME, client.wearingTime)
        cv.put(Constants.DATE_OF_PURCHASE, client.dateOfPurchase)
        cv.put(Constants.NOTES, client.notes)
        cv.put(Constants.IS_NOTIFICATION_SEEN, 0)
        val id = db.insert(Constants.TABLE_NAME, null, cv).toInt()
        db.close()

        return id
    }

    override fun updateClient(client: Client) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(Constants.FULL_NAME, client.fullname)
        cv.put(Constants.PHONE_NUMBER, client.phoneNum)
        cv.put(Constants.DATE_OF_BIRTH, client.dateOfBirth)
        cv.put(Constants.IMG_PROFILE, client.imgProfile)
        cv.put(Constants.GENDER, client.gender)
        cv.put(Constants.PRODUCT_NAME, client.productName)
        cv.put(Constants.DIOPTRE, client.dioptre)
        cv.put(Constants.WEARING_TIME, client.wearingTime)
        cv.put(Constants.DATE_OF_PURCHASE, client.dateOfPurchase)
        cv.put(Constants.NOTES, client.notes)
        cv.put(Constants.IS_NOTIFICATION_SEEN, 0)
        db.update(Constants.TABLE_NAME, cv, "id=" + client.id, null)
        db.close()
    }

    override fun deleteClientById(id: Int) {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM " + Constants.TABLE_NAME + " WHERE id = " + id)
        db.close()
    }

    override fun updateClientImgProfile(id: Int, imgProfile: String) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(Constants.IMG_PROFILE, imgProfile)
        db.update(Constants.TABLE_NAME, cv, "id=$id", null)
        db.close()
    }

    override fun makeNotificationChecked(id: Int) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(Constants.IS_NOTIFICATION_SEEN, 1)
        db.update(Constants.TABLE_NAME, cv, "id=$id", null)
        db.close()
    }


    override fun getAllClients(): ArrayList<Client> {
        val clients = ArrayList<Client>()
        val db = writableDatabase
        val query = "SELECT * FROM ${Constants.TABLE_NAME}"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val client = Client()
                client.id = cursor.getInt(0)
                client.fullname = cursor.getString(1)
                client.phoneNum = cursor.getString(2)
                client.dateOfBirth = cursor.getString(3)
                client.imgProfile = cursor.getString(4)
                client.gender = cursor.getInt(5)
                client.productName = cursor.getString(6)
                client.dioptre = cursor.getString(7)
                client.wearingTime = cursor.getInt(8)
                client.dateOfPurchase = cursor.getString(9)
                client.notes = cursor.getString(10)
                client.isNotificationSeen = cursor.getInt(11) == 1
                clients.add(client)
            } while (cursor.moveToNext())
        }

        return clients
    }


}