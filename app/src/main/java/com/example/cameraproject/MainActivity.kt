package com.example.cameraproject

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Camera
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService
import kotlin.concurrent.schedule
import kotlin.concurrent.scheduleAtFixedRate
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import com.google.common.util.concurrent.ListenableFuture
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.concurrent.Executors

typealias LumaListener = (luma: Double) -> Unit


private const val FILE_NAME = "photo.jpg"
private const val REQUEST_CODE = 42
private lateinit var photoFile: File

private var preview: Preview? = null
//private var imageCapture: ImageCapture? = null
private var imageAnalyzer: ImageAnalysis? = null
private var camera: Camera? = null
//private var lastLuma: Double? = null
private var allower: Boolean = false



class MainActivity : AppCompatActivity() {
    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>
    private var imageCapture: ImageCapture? = null

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private var toggleOn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

     if (allPermissionsGranted()) {
        allower = true
     } else {
         ActivityCompat.requestPermissions(
             this, REQUIRED_PERMISSION, REQUEST_CODE_PERMISSION
         )
     }

        outputDirectory = getOutputDirectory()
        startDetectButton.setOnClickListener { buttonHandling() }
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if  (requestCode == REQUEST_CODE_PERMISSION) {
            if (allPermissionsGranted()) {
                //startCam()
            } else {
                Toast.makeText(this, "moi error", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }



    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSION.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED

    }

    companion object {
        private const val TAG = "CameraXExample"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SS"
        private const val REQUEST_CODE_PERMISSION = 10
        private val REQUIRED_PERMISSION = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun startDetecting() {


        if (allower) {
           startCam()

        } else {
            Toast.makeText(this, "allower is false", Toast.LENGTH_SHORT).show()
        }

    }

    private fun buttonHandling() {
//        toggleOn = !toggleOn
        if (toggleOn) {
            toggleOn = false
            finishAffinity()
        } else {
            toggleOn = true
            startDetectButton.setText("Stop detecting")
            startDetecting()
        }
    }


    private fun startCam() {
        var lastLuma: Double? = null
                cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            preview = Preview.Builder()
                .build()
                    .also {
                        it.setSurfaceProvider(viewFinder.surfaceProvider)
                    }

             imageAnalyzer = ImageAnalysis.Builder()
                    .build()
                    //tähän settingssii
                    .also {
                        it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                            Log.d(TAG, "Average luminosity: $luma")

                            if (lastLuma == null) {
                                lastLuma = luma
                            }

                            lastLuma?.let { comparePics(it, luma) }
                            lastLuma = luma
                        })
                    }

            imageCapture = ImageCapture.Builder()
                    .build()

            val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

            try {
                    cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture, imageAnalyzer)   //"camera =" poistettu

//                  random, selvitä
//                preview?.setSurfaceProvider(viewFinder.createSurfaceProvider(camera?.cameraInfo))
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }



    private fun takePic() {
// Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
                outputDirectory,
                SimpleDateFormat(FILENAME_FORMAT, Locale.US
                ).format(System.currentTimeMillis()) + ".jpg")

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken

        imageCapture.takePicture(
                outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                val msg = "Photo capture succeeded: $savedUri"
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                Log.d(TAG, msg)
            }
        })

//        imageCapture.takePicture(
//                outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
//            override fun onError(exc: ImageCaptureException) {
//                Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
//            }
//
//            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
//                val savedUri = Uri.fromFile(photoFile)
//                val msg = "Photo capture succeeded: $savedUri"
//                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
//                Log.d(TAG, msg)
//            }
//        })
    }

    private fun comparePics(lastLuma: Double, luma: Double) {
//        com.example.cameraproject.lastLuma
        val lumaAsInt = luma.toInt()
        val lastLumaAsInt = lastLuma.toInt()

        if (lastLuma != null && luma != null) {
            var result = 0
            result = (((lastLumaAsInt - lumaAsInt) * 100) / lumaAsInt)

            Log.d(TAG, "Difference in average luminosity: $result")
            if (result > 25 || result < -25) {
//                val timer = Timer("schedule", true)
//
//                timer.schedule(1000) {
                    takePic()
//                }
                Log.d(TAG, "Movement detected!")
            }
        }
    }

//    private fun startComparison() {
//         //create a daemon thread
//        val timer = Timer("schedule", true)
//
//        // schedule a single event
//        timer.schedule(1000) {
//            comparePics()
//        }
//    }


    private class LuminosityAnalyzer(private val listener: LumaListener) : ImageAnalysis.Analyzer {

        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        override fun analyze(image: ImageProxy) {

            val buffer = image.planes[0].buffer
            val data = buffer.toByteArray()
            val pixels = data.map { it.toInt() and 0xFF }
            val luma = pixels.average()

            listener(luma)

            image.close()
        }
    }







    private fun getPhotoFile(fileName: String): File {
        getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {

//            val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
//            imageView.setImageBitmap(takenImage)

            val takenImage = data?.extras?.get("data") as Bitmap
            //temporary
//            imageView.setImageBitmap(takenImage)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }

    }
}

