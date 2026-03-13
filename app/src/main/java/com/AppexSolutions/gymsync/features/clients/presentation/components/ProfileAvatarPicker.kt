package com.AppexSolutions.gymsync.features.clients.presentation.components

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.AppexSolutions.gymsync.core.datastore.camera.CameraDataSource

/**
 * Componente reutilizable de avatar con selector de imagen.
 *
 * Muestra un avatar circular con un botón de edición superpuesto.
 * Al tocar, permite seleccionar una imagen de la galería o tomar una foto.
 *
 * @param imageUri URI de la imagen actual (null muestra avatar por defecto).
 * @param onImageSelected Callback cuando el usuario selecciona/captura una imagen.
 */
@Composable
fun ProfileAvatarPicker(
    imageUri: Uri?,
    onImageSelected: (Uri) -> Unit
) {
    val context = LocalContext.current
    val cameraDataSource = remember { CameraDataSource(context) }

    // URI temporal para la foto de cámara
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Mostrar bottom sheet de selección
    var showImageSourceDialog by remember { mutableStateOf(false) }

    // Launcher para seleccionar imagen de galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    // Launcher para capturar foto con cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            tempCameraUri?.let { onImageSelected(it) }
        }
    }

    // Launcher para solicitar permiso de cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val uri = cameraDataSource.createTempPhotoUri()
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Avatar con botón de edición
        Box(contentAlignment = Alignment.BottomEnd) {
            // Círculo del avatar
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E40AF))
                    .clickable { showImageSourceDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            // Botón circular azul con icono de editar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .offset(x = (-4).dp, y = (-4).dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3B82F6))
                    .clickable { showImageSourceDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Cambiar foto",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Toca para cambiar avatar",
            color = Color(0xFF9CA3AF),
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal
        )
    }

    // Diálogo de selección de fuente de imagen
    if (showImageSourceDialog) {
        ImageSourceDialog(
            onGallerySelected = {
                showImageSourceDialog = false
                galleryLauncher.launch("image/*")
            },
            onCameraSelected = {
                showImageSourceDialog = false
                val hasCameraPermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED

                if (hasCameraPermission) {
                    val uri = cameraDataSource.createTempPhotoUri()
                    tempCameraUri = uri
                    cameraLauncher.launch(uri)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onDismiss = { showImageSourceDialog = false }
        )
    }
}
