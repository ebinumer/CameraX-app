package com.ebinumer.cameraxandcrop

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.ebinumer.cameraxandcrop.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraSelector: CameraSelector
    private var imageCapture: ImageCapture? = null
    private lateinit var imgCaptureExecutor: ExecutorService
    var flashOn = false
    var photo = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        imgCaptureExecutor = Executors.newSingleThreadExecutor()

        btnClick()
        getAllImages()
    }

    @SuppressLint("ResourceAsColor")
    fun btnClick() {
        val cameraProviderResult =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
                if (permissionGranted) {
                    // cut and paste the previous startCamera() call here.
                    startCamera()
                } else {
                    Snackbar.make(
                        binding.root,
                        "The camera permission is required",
                        Snackbar.LENGTH_INDEFINITE
                    ).show()
                }
            }

        cameraProviderResult.launch(android.Manifest.permission.CAMERA)
        binding.imgCaptureBtn.setOnClickListener {
            takePhoto()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                animateFlash()
            }
            if (flashOn) {

            }
        }
        binding.switchBtn.setOnClickListener {

            //change the cameraSelector
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            // restart the camera
            startCamera()
        }
        binding.flashBtn.setOnClickListener {
            if (flashOn) {
                flashOn = false
                Glide.with(this).load(R.drawable.ic_baseline_flash_off_24).into(binding.flashBtn)
            } else {
                flashOn = true
                Glide.with(this).load(R.drawable.ic_baseline_flash_on_24).into(binding.flashBtn)

            }
        }
        binding.cardImg.setOnClickListener {

            startActivity(Intent(this, GalleryActivity::class.java))
        }

        binding.txtPhoto.setOnClickListener {
            photo = true
            binding.txtPhoto.setTextColor(R.color.yellow)
            binding.txtVideo.setTextColor(R.color.white)
        }
        binding.txtVideo.setOnClickListener {
            photo = false
            binding.txtVideo.setTextColor(R.color.yellow)
            binding.txtPhoto.setTextColor(R.color.white)
        }
    }

    private fun startCamera() {
        // listening for data from the camera
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            // connecting a preview use case to the preview in the xml file.
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.preview.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()
            try {
                // clear all the previous use cases first.
                cameraProvider.unbindAll()
                // binding the lifecycle of the camera to the lifecycle of the application.
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Log.d("Main", "Use case binding failed")
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        imageCapture?.let {
            //Create a storage location whose fileName is timestamped in milliseconds.
            val fileName = "JPEG_${System.currentTimeMillis()}.jpeg"
            val file = File(externalMediaDirs[0], fileName)

            // Save the image in the above file
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()

            /* pass in the details of where and how the image is taken.(arguments 1 and 2 of takePicture)
            pass in the details of what to do after an image is taken.(argument 3 of takePicture) */

            it.takePicture(
                outputFileOptions,
                imgCaptureExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        Log.i("main", "The image has been saved in ${file.toUri()}")
                        runOnUiThread {
                            binding.cardImg.visibility = View.VISIBLE
                            Glide.with(this@MainActivity).load(file.toUri())
                                .into(binding.imgLastPic)
                        }

                    }

                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(
                            binding.root.context,
                            "Error taking photo",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.d("main", "Error taking photo:$exception")
                    }

                })
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun animateFlash() {
        binding.root.postDelayed({
            binding.root.foreground = ColorDrawable(Color.WHITE)
            binding.root.postDelayed({
                binding.root.foreground = null
            }, 50)
        }, 100)
    }

    fun getAllImages() {
        val directory = File(externalMediaDirs[0].absolutePath)
        val files = (directory.listFiles() as Array<File>).reversedArray()
        if (files.isNotEmpty()) {
            binding.cardImg.visibility = View.VISIBLE
            Glide.with(this@MainActivity).load(files[0])
                .into(binding.imgLastPic)
        }
    }
}