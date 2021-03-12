package com.example.cameraproject

//watcher alkuperänen

//package com.example.cameraproject
//
//import android.app.Activity.RESULT_OK
//import android.content.ActivityNotFoundException
//import android.content.Intent
//import android.graphics.Bitmap
//import android.provider.MediaStore
//import androidx.core.app.ActivityCompat.startActivityForResult
//
//val REQUEST_IMAGE_CAPTURE = 1
//
//private fun dispatchTakePictureIntent() {
//    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//    try {
//        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
//    } catch (e: ActivityNotFoundException) {
//        // display error state to the user
//    }
//}
//
//override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//        val imageBitmap = data.extras.get("data") as Bitmap
////        imageView.setImageBitmap(imageBitmap)
//    }
//}