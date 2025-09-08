package com.example.online_chat_hde.ui

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import androidx.compose.material3.ExperimentalMaterial3Api


import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.online_chat_hde.R
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilePickerBottom(
    expanded: MutableState<Boolean>,
    uiConfig: ChatUIConfig,
    onFileLoaded: (Uri, Long) -> Unit = {_, _ ->},
) {

    val ctx = LocalContext.current

    // Храним ТОЛЬКО пути, а не File-объекты
    var currentPhotoPath by rememberSaveable { mutableStateOf<String?>(null) }
    var currentVideoPath by rememberSaveable { mutableStateOf<String?>(null) }

    // Текущие Uri держим в обычном состоянии
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var currentVideoUri by remember { mutableStateOf<Uri?>(null) }

    // Фабрики файлов/Uri — создаём в ПОДПАПКЕ "pics" (как в file_paths.xml)
    fun newPhotoTarget(): Pair<File, Uri> {
        val dir = File(ctx.cacheDir, "pics").apply { mkdirs() }      // <-- важно: cacheDir/pics
        val file = File.createTempFile("IMG_", ".jpg", dir)
        val uri  = FileProvider.getUriForFile(ctx, "${ctx.packageName}.online_chat_fileprovider", file)
        return file to uri
    }

    fun newVideoTarget(): Pair<File, Uri> {
        val dir = File(ctx.cacheDir, "pics").apply { mkdirs() }      // <-- важно: cacheDir/pics
        val file = File.createTempFile("VID_", ".mp4", dir)
        val uri  = FileProvider.getUriForFile(ctx, "${ctx.packageName}.online_chat_fileprovider", file)
        return file to uri
    }


    val pickPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success -> // Фото
            val file = currentPhotoPath?.let(::File)
            if (success && file != null && file.exists() && file.length() > 0) {
                println("[photo] >>> ${currentPhotoUri}")
                onFileLoaded(currentPhotoUri!!, ctx.getFileSize(currentPhotoUri!!))
            }
        }
    )

    val pickVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo(),
        onResult = { success -> // Видео
            val file = currentVideoPath?.let(::File)
            if (success && file != null && file.exists() && file.length() > 0L) {
                // видео готово
                onFileLoaded(currentVideoUri!!, ctx.getFileSize(currentVideoUri!!))
            }
        }
    )

    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> // Файл
            println("[FILE uri] >>> $uri")
            expanded.value = false
            uri?.let {
                onFileLoaded(it, ctx.getFileSize(it))
            }
        }
    )


    // Переменная для хранения действия после получения разрешений
    val pendingCameraAction = remember { mutableStateOf<(() -> Unit)?>(null) }

    // Универсальный лаунчер для множественных разрешений
    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            // Выполняем отложенное действие после получения всех разрешений
            pendingCameraAction.value?.invoke()
        }
        pendingCameraAction.value = null
    }

    // Функция для запуска камеры с проверкой разрешений
    fun launchWithPermissions(action: () -> Unit) {
        val requiredPermissions = arrayOf(Manifest.permission.CAMERA)

        if (requiredPermissions.all {
                ContextCompat.checkSelfPermission(ctx, it) == PackageManager.PERMISSION_GRANTED
            }) {
            // Разрешения уже есть - сразу запускаем
            action()
        } else {
            // Сохраняем действие и запрашиваем разрешения
            pendingCameraAction.value = action
            permissionsLauncher.launch(requiredPermissions)
        }
    }




    val filePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pickFileLauncher.launch("*/*") // Запускаем выбор файла после получения разрешения
        }
    }

    // Функция для выбора файла с проверкой разрешений
    fun selectFile() {
        when {
            // Android 10+ (API 29+) - разрешение не требуется для системного пикера
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                pickFileLauncher.launch("*/*")
            }
            // Проверяем разрешение для старых версий
            ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                pickFileLauncher.launch("*/*")
            }
            // Запрашиваем разрешение если нет
            else -> {
                filePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }


    // стартовые функции (создают цель + запускают лаунчер)
    fun launchTakePhoto() {
        val (file, uri) = newPhotoTarget()
        currentPhotoPath = file.absolutePath
        currentPhotoUri = uri
        pickPhotoLauncher.launch(uri)
    }

    fun launchCaptureVideo() {
        val (file, uri) = newVideoTarget()
        currentVideoPath = file.absolutePath
        currentVideoUri = uri
        pickVideoLauncher.launch(uri)
    }




    if (expanded.value) {

//        val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
//        val bottom = rememberNavBarBottomInsetNoE2E()

        ModalBottomSheet(
            onDismissRequest = {expanded.value = false},
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = uiConfig.colors.background
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(16.dp, 16.dp)),
                horizontalArrangement = Arrangement.SpaceAround
            ) {


                FilePickerMenuItem(R.drawable.photo, "Фото", uiConfig) {
                    launchWithPermissions {
                        launchTakePhoto()
                    }
                    expanded.value = false
                }

                FilePickerMenuItem(R.drawable.camera, "Видео", uiConfig) {
                    launchWithPermissions {
                        launchCaptureVideo()
                    }
                    expanded.value = false
                }

                FilePickerMenuItem(R.drawable.folder, "Файлы", uiConfig) {
                    selectFile()
                }

            }
        }
    }


}




@Composable
private fun FilePickerMenuItem(
    icon: Int,
    text: String,
    uiConfig: ChatUIConfig,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(
                    color = uiConfig.colors.userRipple,
                ),
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            ImageVector.vectorResource(icon),
            contentDescription = null,
            colorFilter = ColorFilter.tint(uiConfig.colors.userMessageBackground),
            modifier = Modifier.size(uiConfig.dimensions.bottomSheetIconSize)
        )
        Text(
            text = text,
            fontSize = uiConfig.dimensions.timeFontSize,
            color = uiConfig.colors.userMessageBackground
        )
    }
}


fun Context.getFileSize(uri: Uri): Long {
    return when (uri.scheme) {
        ContentResolver.SCHEME_CONTENT -> {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1 && cursor.moveToFirst()) {
                    cursor.getLong(sizeIndex)
                } else {
                    -1L
                }
            } ?: -1L
        }
        ContentResolver.SCHEME_FILE -> {
            File(uri.path ?: return -1).length()
        }
        else -> -1L
    }
}
