package com.example.paymentdemo.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun QrScanner(onCode: (String) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var granted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    val delivered = remember { AtomicBoolean(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it; if (!it) error = "Camera permission is required to scan a QR code." }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selected -> runCatching {
            val image = InputImage.fromFilePath(context, selected)
            BarcodeScanning.getClient().process(image).addOnSuccessListener { codes ->
                val value = codes.firstOrNull()?.rawValue
                if (value != null && delivered.compareAndSet(false, true)) onCode(value) else if (value == null) error = "No QR code found in this image."
            }.addOnFailureListener { error = "Could not read this image." }
        }.onFailure { error = "Could not open the selected image." } }
    }
    LaunchedEffect(Unit) { if (!granted) launcher.launch(Manifest.permission.CAMERA) }
    Column(Modifier.fillMaxSize().background(Color(0xFF17121D))) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("‹  Back", color = Color.White) }
            Spacer(Modifier.weight(1f)); Text("Scan any QR", color = Color.White, style = MaterialTheme.typography.titleLarge)
        }
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (granted) {
                AndroidView(factory = { ctx ->
                    val previewView = PreviewView(ctx).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
                    val providerFuture = ProcessCameraProvider.getInstance(ctx)
                    providerFuture.addListener({
                        runCatching {
                            val provider = providerFuture.get()
                            val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                            val scanner = BarcodeScanning.getClient()
                            val analysis = ImageAnalysis.Builder().setTargetResolution(Size(1280, 720)).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
                            val executor = Executors.newSingleThreadExecutor()
                            analysis.setAnalyzer(executor) { proxy ->
                                val media = proxy.image
                                if (media == null) proxy.close() else {
                                    scanner.process(InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees))
                                        .addOnSuccessListener { codes -> codes.firstOrNull()?.rawValue?.let { value -> if (delivered.compareAndSet(false, true)) onCode(value) } }
                                        .addOnCompleteListener { proxy.close() }
                                }
                            }
                            provider.unbindAll(); provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
                        }.onFailure { error = "Unable to start camera. Check camera availability." }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                }, modifier = Modifier.fillMaxSize())
                Box(Modifier.size(260.dp).border(3.dp, Color.White, RoundedCornerShape(24.dp)))
            } else Text(error ?: "Waiting for camera permission", color = Color.White)
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.BottomCenter).padding(28.dp)) {
                Text("Align the demo QR inside the frame", color = Color.White)
                TextButton(onClick = { gallery.launch("image/*") }) { Text("Choose QR image from gallery", color = Color.White) }
                error?.let { Text(it, color = Color(0xFFFFB4AB), modifier = Modifier.padding(top = 8.dp)) }
            }
        }
    }
}
