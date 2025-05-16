package com.example.gc

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.widget.Button
import android.widget.ImageView
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.camera.core.AspectRatio

class CaptureActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var imageCaptured: ImageView
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var backButton: ImageButton
    private lateinit var captureButton: Button

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private var preview: Preview? = null // ประกาศ preview เป็น Property ของ Class
    private lateinit var cameraSelector: CameraSelector

    companion object {
        private const val TAG = "CaptureActivity"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 123
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)

        previewView = findViewById(R.id.previewView)
        imageCaptured = findViewById(R.id.imageCaptured)
        captureButton = findViewById(R.id.captureButton)
        backButton = findViewById(R.id.backButton)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        cameraExecutor = Executors.newSingleThreadExecutor()

        requestPermissions()

        captureButton.setOnClickListener {
            takePhoto()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun requestPermissions() {
        if (allPermissionsGranted()) {
            startCamera()
            getCurrentLocation()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                startCamera()
                getCurrentLocation()
            } else {
                // แจ้งเตือนผู้ใช้ว่าไม่ได้รับ Permission
                finish() // หรือจัดการตามความเหมาะสม
            }
        }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        currentLatitude = it.latitude
                        currentLongitude = it.longitude
                        Log.d("Location", "Latitude: $currentLatitude, Longitude: $currentLongitude")
                        // ตอนนี้เรามีตำแหน่งแล้ว คุณสามารถนำไปใช้เมื่อถ่ายรูป
                    } ?: run {
                        Log.d("Location", "Last known location is null")
                        // อาจจะต้อง requestLocationUpdates หากต้องการตำแหน่งล่าสุดเสมอ
                    }
                }
        } else {
            // Permission ไม่ได้รับ
        }
    }

    private fun takePhoto() {
        val photoFile = File(
            externalMediaDirs.first(),
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )

        // กำหนด Target Resolution ให้เป็นขนาดเดียวกัน (เช่น 640x640) เพื่อให้ได้อัตราส่วน 1:1
        val targetResolution = android.util.Size(640, 640) // คุณสามารถปรับขนาดได้ตามต้องการ

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
            .build() // เพิ่ม .build() ที่นี่

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(e: ImageCaptureException) {
                    Log.e("CameraX", "Photo capture failed: ${e.message}", e)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val intent = Intent(this@CaptureActivity, DisplayActivity::class.java)
                    intent.putExtra("imageUri", output.savedUri.toString())
                    intent.putExtra("latitude", currentLatitude)
                    intent.putExtra("longitude", currentLongitude)
                    startActivity(intent)
                }
            }
        )
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview, // ใช้ตัวแปร preview ที่ประกาศด้านนอก
                    imageCapture
                )

                // ไม่จำเป็นต้องกำหนด SurfaceProvider ซ้ำอีก
                // preview?.setSurfaceProvider(previewView.surfaceProvider)

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
