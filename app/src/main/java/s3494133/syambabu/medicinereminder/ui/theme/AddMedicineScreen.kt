package s3494133.syambabu.medicinereminder.ui.theme


import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import s3494133.syambabu.medicinereminder.R
import s3494133.syambabu.medicinereminder.utils.Medicine
import s3494133.syambabu.medicinereminder.utils.MedicineViewModel
import s3494133.syambabu.medicinereminder.utils.NotificationScheduler
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddMedicineScreen(navController: NavController, viewModel: MedicineViewModel) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var times by remember { mutableStateOf(mutableStateListOf("08:00")) }
    var frequencyType by remember { mutableStateOf("Daily") }
    var specificDays by remember { mutableStateOf(mutableStateListOf<Int>()) }
    var totalQuantity by remember { mutableStateOf("") }
    var currentQuantity by remember { mutableStateOf("") }
    var refillThreshold by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var sideEffects by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val showTimePicker = remember { mutableStateOf(false) }
    val timePickerIndex = remember { mutableStateOf(0) }

    val tempPhotoUri = remember { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            photoUri = tempPhotoUri.value
        } else {
            Toast.makeText(context, "Photo capture failed", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val file = createImageFile(context)
            val uri =
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            tempPhotoUri.value = uri
            takePictureLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Re-attempt save and schedule after permission is granted
            Toast.makeText(context, "Notification permission granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                context,
                "Notification permission denied. Reminders may not show.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val exactAlarmPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (NotificationScheduler.canScheduleExactAlarms(context)) {
            Toast.makeText(context, "Alarms & reminders permission granted!", Toast.LENGTH_SHORT).show()

        } else {
            Toast.makeText(context, "Alarms & reminders permission denied. Exact reminders may not work.", Toast.LENGTH_LONG).show()
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add New Medicine",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.surface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Orange,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Medicine Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage (e.g., 1 tablet, 5ml)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Multiple Doses per Day
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Times:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    times.forEachIndexed { index, time ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedTextField(
                                value = time.ifEmpty { "Select Time" },
                                onValueChange = { /* Read-only */ },
                                label = { Text("Time ${index + 1}") },
                                readOnly = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        timePickerIndex.value = index
                                        showTimePicker.value = true
                                    },
                                trailingIcon = {
                                    Icon(
                                        painter = painterResource(id = android.R.drawable.ic_input_add),
                                        contentDescription = "Select Time",
                                        modifier = Modifier.clickable {
                                            timePickerIndex.value = index
                                            showTimePicker.value = true
                                        }
                                    )
                                }
                            )
                            if (times.size > 1) {
                                IconButton(onClick = { times.removeAt(index) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Remove Time",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Button(onClick = { times.add("") }, modifier = Modifier.fillMaxWidth()) {
                        Text("Add Another Time")
                    }
                }
            }

            // Flexible Scheduling - Frequency Type
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Frequency Type:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    FlowRow( // Corrected FlowRow usage
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = frequencyType == "Daily",
                            onClick = { frequencyType = "Daily"; specificDays.clear() },
                            label = { Text("Daily") }
                        )
                        FilterChip(
                            selected = frequencyType == "Alternate Day",
                            onClick = { frequencyType = "Alternate Day"; specificDays.clear() },
                            label = { Text("Alternate Day") }
                        )
                        FilterChip(
                            selected = frequencyType == "Specific Days",
                            onClick = { frequencyType = "Specific Days" },
                            label = { Text("Specific Days") }
                        )
                    }
                }
            }

            // Specific Days Selection (only if "Specific Days" is selected)
            if (frequencyType == "Specific Days") {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Select Days:",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        val daysOfWeek = listOf(
                            Calendar.SUNDAY to "Sun", Calendar.MONDAY to "Mon",
                            Calendar.TUESDAY to "Tue", Calendar.WEDNESDAY to "Wed",
                            Calendar.THURSDAY to "Thu", Calendar.FRIDAY to "Fri",
                            Calendar.SATURDAY to "Sat"
                        )
                        FlowRow( // Corrected FlowRow usage
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            daysOfWeek.forEach { (dayInt, dayName) ->
                                FilterChip(
                                    selected = specificDays.contains(dayInt),
                                    onClick = {
                                        if (specificDays.contains(dayInt)) {
                                            specificDays.remove(dayInt)
                                        } else {
                                            specificDays.add(dayInt)
                                        }
                                    },
                                    label = { Text(dayName) }
                                )
                            }
                        }
                        if (specificDays.isEmpty()) {
                            Text(
                                "Please select at least one day.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Refill Reminders
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Refill Information:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = totalQuantity,
                        onValueChange = { totalQuantity = it },
                        label = { Text("Total Quantity (e.g., 30 pills)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = currentQuantity,
                        onValueChange = { currentQuantity = it },
                        label = { Text("Current Quantity (leave empty to default to total)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = refillThreshold,
                        onValueChange = { refillThreshold = it },
                        label = { Text("Refill Threshold (e.g., remind when 7 doses left)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Detailed Medicine Information
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Additional Notes:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (e.g., take with food)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = sideEffects,
                        onValueChange = { sideEffects = it },
                        label = { Text("Side Effects") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = purpose,
                        onValueChange = { purpose = it },
                        label = { Text("Purpose of Medicine") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }


            // Photo Placeholder and Capture Button
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (photoUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(photoUri),
                            contentDescription = "Captured Medicine Photo",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.iv_tablet),
                            contentDescription = "Medicine Photo Placeholder",
                            modifier = Modifier
                                .size(120.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))


                    val takePicturePreview = rememberLauncherForActivityResult(
                        ActivityResultContracts.TakePicturePreview()
                    ) { bitmap ->
                        if (bitmap != null) {
                            val file = saveBitmapToCache(context, bitmap)
//                            uploadImage(file)
                        }
                    }

                    Button(onClick = {
                        takePicturePreview.launch()
                    }) {
                        Text("Take Photo")
                    }


//                    Button(
//                        onClick = {
//                            when (PackageManager.PERMISSION_GRANTED) {
//                                ContextCompat.checkSelfPermission(
//                                    context,
//                                    Manifest.permission.CAMERA
//                                ) -> {
//                                    val file = createImageFile(context)
//                                    val uri = FileProvider.getUriForFile(
//                                        context,
//                                        "${context.packageName}.fileprovider",
//                                        file
//                                    )
//                                    tempPhotoUri.value = uri
//                                    takePictureLauncher.launch(uri)
//                                }
//
//                                else -> {
//                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
//                                }
//                            }
//                        },
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        Text("Add Photo")
//                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Button(
                    onClick = {
                        val parsedTotalQuantity = totalQuantity.toIntOrNull()
                        val parsedCurrentQuantity = currentQuantity.toIntOrNull() ?: parsedTotalQuantity
                        val parsedRefillThreshold = refillThreshold.toIntOrNull()

                        if (name.isNotBlank() && dosage.isNotBlank() && times.any { it.isNotBlank() } &&
                            (frequencyType != "Specific Days" || specificDays.isNotEmpty())) {

                            val newMedicine = Medicine(
                                name = name,
                                photoUri = photoUri?.toString(),
                                dosage = dosage,
                                times = times.filter { it.isNotBlank() },
                                frequencyType = frequencyType,
                                specificDays = if (frequencyType == "Specific Days") specificDays.toList() else null,
                                totalQuantity = parsedTotalQuantity,
                                currentQuantity = parsedCurrentQuantity,
                                refillThreshold = parsedRefillThreshold,
                                notes = notes.ifBlank { null },
                                sideEffects = sideEffects.ifBlank { null },
                                purpose = purpose.ifBlank { null }
                            )

                            // Check for POST_NOTIFICATIONS permission on Android 13+
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    // Check for SCHEDULE_EXACT_ALARM permission on Android 12+
                                    if (NotificationScheduler.canScheduleExactAlarms(context)) {
                                        viewModel.viewModelScope.launch {
                                            val medicineId = viewModel.insert(newMedicine).toInt()
                                            NotificationScheduler.scheduleAllNotifications(context, newMedicine.copy(id = medicineId))
                                            navController.popBackStack()
                                        }
                                    } else {
                                        // Request SCHEDULE_EXACT_ALARM permission
                                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                            data = Uri.fromParts("package", context.packageName, null)
                                        }
                                        exactAlarmPermissionLauncher.launch(intent)
                                        Toast.makeText(context, "Please grant 'Alarms & reminders' permission in settings.", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    // Request POST_NOTIFICATIONS permission
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    Toast.makeText(context, "Please grant notification permission to set reminders.", Toast.LENGTH_LONG).show()
                                }
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 (API 31) to Android 12L (API 32)
                                // Only SCHEDULE_EXACT_ALARM is required here, POST_NOTIFICATIONS is runtime for 33+
                                if (NotificationScheduler.canScheduleExactAlarms(context)) {
                                    viewModel.viewModelScope.launch {
                                        val medicineId = viewModel.insert(newMedicine).toInt()
                                        NotificationScheduler.scheduleAllNotifications(context, newMedicine.copy(id = medicineId))
                                        navController.popBackStack()
                                    }
                                } else {
                                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                    exactAlarmPermissionLauncher.launch(intent)
                                    Toast.makeText(context, "Please grant 'Alarms & reminders' permission in settings.", Toast.LENGTH_LONG).show()
                                }
                            }
                            else {
                                // For older Android versions, permissions are granted at install time
                                viewModel.viewModelScope.launch {
                                    val medicineId = viewModel.insert(newMedicine).toInt()
                                    NotificationScheduler.scheduleAllNotifications(context, newMedicine.copy(id = medicineId))
                                    navController.popBackStack()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Please fill all required fields (Name, Dosage, Times, Frequency, and Specific Days if selected).", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Save Medicine", fontSize = 18.sp)
                }
            }
        }

        // Time Picker Dialog
        if (showTimePicker.value) {
            val calendar = Calendar.getInstance()
            // Try to parse existing time to pre-fill picker, otherwise use current time
            val existingTime = times.getOrNull(timePickerIndex.value)
            val initialHour = existingTime?.split(":")?.getOrNull(0)?.toIntOrNull() ?: calendar.get(
                Calendar.HOUR_OF_DAY
            )
            val initialMinute =
                existingTime?.split(":")?.getOrNull(1)?.toIntOrNull() ?: calendar.get(
                    Calendar.MINUTE
                )

            TimePickerDialog(
                context,
                { _, selectedHour: Int, selectedMinute: Int ->
                    val newTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                    times[timePickerIndex.value] = newTime // Update the specific time in the list
                    showTimePicker.value = false
                },
                initialHour,
                initialMinute,
                true // 24 hour format for internal storage, can format for display
            ).apply {
                setOnDismissListener { showTimePicker.value = false }
            }.show()
        }
    }
}

fun saveBitmapToCache(context: Context, bitmap: Bitmap): File {
    val file = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
    file.outputStream().use {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
    }
    return file
}


fun createImageFile(context: Context): File {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir: File? = context.getExternalFilesDir(null)
    return File.createTempFile(
        "JPEG_${timeStamp}_",
        ".jpg",
        storageDir
    )
}
