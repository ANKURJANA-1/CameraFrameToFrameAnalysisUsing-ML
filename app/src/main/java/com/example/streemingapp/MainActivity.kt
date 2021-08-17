package com.example.streemingapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;


class MainActivity : AppCompatActivity() {

    // welcome ..
    lateinit var cameraView: SurfaceView
    lateinit var textView: TextView
    lateinit var  cameraSource: CameraSource

    val RequestCameraPermission = 1001
    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cameraView = findViewById(R.id.surface_view)
        textView = findViewById(R.id.text_view)


        // 1
        val textRecognizer: TextRecognizer = TextRecognizer.Builder(applicationContext).build()
        if (!textRecognizer.isOperational()) {
            Log.w("MainActivity", "Detected dependence are not found ")
        } else {
            cameraSource = CameraSource.Builder(applicationContext, textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1280, 1024)
                .setRequestedFps(2.0f)
                .setAutoFocusEnabled(true)
                .build()

            //2
            cameraView.getHolder().addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    try {
                        if (ActivityCompat.checkSelfPermission(
                                applicationContext,
                                Manifest.permission.CAMERA
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                this@MainActivity, arrayOf(Manifest.permission.CAMERA),
                                RequestCameraPermission
                            )
                        }
                        cameraSource!!.start(cameraView.getHolder())
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }

                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int
                ) {
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    cameraSource!!.stop()
                }
            })


            // 4
            textRecognizer.setProcessor(object : Detector.Processor<TextBlock?> {
                override fun release() {}
                override fun receiveDetections(detections: Detector.Detections<TextBlock?>) {
                    val items: SparseArray<TextBlock?>? = detections.getDetectedItems()
                    if (items != null) {
                        if (items.size() != 0) {
                            textView.post(Runnable {
                                val stringBuilder = StringBuilder()
                                if (items != null) {
                                    for (i in 0 until items.size()) {
                                        val item = items.valueAt(i)
                                        if (item != null) {
                                            stringBuilder.append(item.getValue())
                                        }
                                        stringBuilder.append("\n")
                                    }
                                }
                                textView.setText(stringBuilder.toString())
                                Log.d("Text", stringBuilder.toString())
                            })
                        }
                    }
                }
            })
        }
    }

    // 3
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RequestCameraPermission -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    try {
                        cameraSource.start(cameraView!!.holder)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    /*  private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                val bindToLifecycle = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview
                )

            } catch(exc: Exception) {
                Log.d("TAG", "Use case binding failed")
            }

        }, ContextCompat.getMainExecutor(this))

    }*/
}

