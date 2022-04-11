package com.example.opticsclientsapp.models

import java.io.Serializable

data class Client(
    var id: Int = 0,
    var fullname: String = "",
    var phoneNum: String = "",
    var dateOfBirth: String = "",
    var imgProfile: String = "",
    var gender: Int = 0,
    var productName: String = "",
    var dioptre: String = "",
    var dateOfPurchase: String = "",
    var wearingTime: Int = 0,
    var notes: String = "",
    var isExpanded: Boolean = false,
    var isNotificationSeen: Boolean = false,
    var daysLeft: Long = 0

) : Serializable

