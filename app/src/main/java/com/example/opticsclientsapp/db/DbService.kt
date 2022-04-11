package com.example.opticsclientsapp.db

import com.example.opticsclientsapp.models.Client

interface DbService {
    fun addClient(client: Client): Int

    fun updateClient(client: Client)

    fun makeNotificationChecked(id: Int)

    fun deleteClientById(id:Int)

    fun updateClientImgProfile(id:Int, imgProfile:String)

    fun getAllClients(): ArrayList<Client>

}
