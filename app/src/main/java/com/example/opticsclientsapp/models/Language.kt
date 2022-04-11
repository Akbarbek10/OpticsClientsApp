package com.example.opticsclientsapp.models

data class Language(
    var id: Int,
    var image: Int,
    var language: String,
    var isChosen: Boolean = false
)