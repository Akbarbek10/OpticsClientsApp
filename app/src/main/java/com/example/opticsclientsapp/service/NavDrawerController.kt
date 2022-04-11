package com.example.opticsclientsapp.service

import androidx.appcompat.widget.Toolbar

interface NavDrawerController {
    fun initDrawer(toolbar: Toolbar)
    fun lockDrawer()
    fun unlockDrawer()
}