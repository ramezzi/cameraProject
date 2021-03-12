package com.example.cameraproject

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat.startActivityForResult
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.scheduleAtFixedRate

val REQUEST_IMAGE_CAPTURE = 1

//class Watcher() {
//
//    fun repeatPics() {
//        // create a daemon thread
//        val timer = Timer("schedule", true)
//
//        // schedule a single event
//        timer.schedule(1000) {
//            println("hello world!")
//        }
//    // schedule at a fixed rate
//        timer.scheduleAtFixedRate(1000, 1000) {
//            println("hello world, repeatedly...")
//        }
//    }
//
//    fun takePicsForComparing() {
//
//    }
//
//}