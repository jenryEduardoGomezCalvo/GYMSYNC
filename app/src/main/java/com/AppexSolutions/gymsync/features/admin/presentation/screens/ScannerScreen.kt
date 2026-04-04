package com.AppexSolutions.gymsync.features.admin.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.AppexSolutions.gymsync.features.admin.presentation.viewmodels.ScannerUiState
import com.AppexSolutions.gymsync.features.admin.presentation.viewmodels.ScannerViewModel
import com.AppexSolutions.gymsync.ui.theme.*
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

@Composable
fun ScannerScreen(
    onNavigateBack: () -> Unit = {},
    onScanSuccess: () -> Unit = {},
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Mostrar resultado 2 segundos y navegar al dashboard si fue éxito
    LaunchedEffect(uiState) {
        if (uiState is ScannerUiState.Success ||
            uiState is ScannerUiState.AlreadyScanned ||
            uiState is ScannerUiState.Error
        ) {
            // Capturar si fue éxito ANTES del delay para no perder el estado tras resetState()
            val wasSuccess = uiState is ScannerUiState.Success
            delay(2000)
            viewModel.resetState()
            if (wasSuccess) {
                onScanSuccess()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkNavy)) {
        if (hasCameraPermission) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                onQrDetected = viewModel::onQrDetected
            )
            ScannerOverlay()
        } else {
            NoCameraPermissionContent(
                onRequestPermission = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            )
        }

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = TextPrimary
                )
            }
            Text(
                text = "Escanear QR",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // Feedback de resultado en la parte inferior
        ResultFeedbackCard(
            uiState = uiState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 32.dp)
        )
    }
}

// ── Preview de cámara ──────────────────────────────────────────────────────────
@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    onQrDetected: (String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    // rememberUpdatedState garantiza que el listener del future siempre use el
    // lifecycle owner más reciente, incluso si el composable recompuso entre
    // el momento en que factory corrió y cuando el callback se dispara.
    val lifecycleOwnerRef = androidx.compose.runtime.rememberUpdatedState(lifecycleOwner)
    val executor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember { BarcodeScanning.getClient() }
    val onQrDetectedRef = androidx.compose.runtime.rememberUpdatedState(onQrDetected)

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
            barcodeScanner.close()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                try {
                    val owner = lifecycleOwnerRef.value

                    // Guard: si el lifecycle ya no está activo (p.ej. Activity recreada
                    // tras conceder permiso en Android 11+), se cancela el bind
                    // para evitar IllegalStateException: Cannot bind to a destroyed lifecycle.
                    if (!owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return@addListener

                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(executor) { imageProxy ->
                                processImageProxy(imageProxy, barcodeScanner) { raw ->
                                    onQrDetectedRef.value(raw)
                                }
                            }
                        }

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        owner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}

@OptIn(ExperimentalGetImage::class)
internal fun processImageProxy(
    imageProxy: androidx.camera.core.ImageProxy,
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    onQrDetected: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return
    }
    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    scanner.process(image)
        .addOnSuccessListener { barcodes ->
            barcodes.firstOrNull()?.rawValue?.let { onQrDetected(it) }
        }
        .addOnCompleteListener { imageProxy.close() }
}

// ── Overlay oscuro con recuadro transparente ───────────────────────────────────
@Composable
private fun ScannerOverlay() {
    val scanBoxSize = 260.dp
    val cornerRadius = 16.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()

                // Capa oscura sobre toda la pantalla
                drawRect(color = Color(0xBB000000))

                // Recorte transparente centrado
                val boxPx = scanBoxSize.toPx()
                val cornerPx = cornerRadius.toPx()
                val topLeft = Offset(
                    x = (size.width - boxPx) / 2f,
                    y = (size.height - boxPx) / 2f
                )
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = topLeft,
                    size = Size(boxPx, boxPx),
                    cornerRadius = CornerRadius(cornerPx),
                    blendMode = BlendMode.Clear
                )
            }
    )

    // Borde luminoso sobre el recuadro (encima del overlay)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(scanBoxSize)
                .drawWithContent {
                    drawContent()
                    drawRoundRect(
                        color = Color(0xFF60A5FA),
                        size = Size(size.width, size.height),
                        cornerRadius = CornerRadius(cornerRadius.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Apunta al código QR del cliente",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = (scanBoxSize.value / 2 - 120).dp.coerceAtLeast(8.dp))
        )
    }
}

// ── Card de feedback ───────────────────────────────────────────────────────────
@Composable
private fun ResultFeedbackCard(
    uiState: ScannerUiState,
    modifier: Modifier = Modifier
) {
    val visible = uiState is ScannerUiState.Success ||
            uiState is ScannerUiState.AlreadyScanned ||
            uiState is ScannerUiState.Error

    if (!visible) return

    val (backgroundColor, icon, title, subtitle) = when (uiState) {
        is ScannerUiState.Success -> FeedbackData(
            background = SuccessGreen.copy(alpha = 0.95f),
            icon = Icons.Default.CheckCircle,
            title = "Asistencia registrada",
            subtitle = uiState.nombreCliente
        )
        is ScannerUiState.AlreadyScanned -> FeedbackData(
            background = WarningAmber.copy(alpha = 0.95f),
            icon = Icons.Default.Warning,
            title = "Ya registrado",
            subtitle = "${uiState.nombreCliente} ya ingresó recientemente"
        )
        is ScannerUiState.Error -> FeedbackData(
            background = ErrorRed.copy(alpha = 0.95f),
            icon = Icons.Default.Error,
            title = "QR no válido",
            subtitle = uiState.mensaje
        )
        else -> return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

// ── Pantalla sin permiso ───────────────────────────────────────────────────────
@Composable
private fun NoCameraPermissionContent(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Permiso de cámara requerido",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Necesitamos acceso a la cámara para escanear los códigos QR de los clientes.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Conceder permiso", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ── Data class auxiliar para el feedback ──────────────────────────────────────
private data class FeedbackData(
    val background: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val subtitle: String
)
