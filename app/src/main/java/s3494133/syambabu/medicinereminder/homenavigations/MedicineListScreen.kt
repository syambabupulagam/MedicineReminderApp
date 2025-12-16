package s3494133.syambabu.medicinereminder.homenavigations

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import s3494133.syambabu.medicinereminder.R
import s3494133.syambabu.medicinereminder.data.Medicine
import s3494133.syambabu.medicinereminder.data.MedicineViewModel
import s3494133.syambabu.medicinereminder.ui.theme.DarkGreen
import s3494133.syambabu.medicinereminder.utils.NotificationScheduler
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineListScreen(navController: NavController, viewModel: MedicineViewModel) {
    val medicines by viewModel.allMedicines.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Scheduled Medicines",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkGreen
                )
            )
        }
    ) { paddingValues ->
        if (medicines.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No medicines scheduled yet.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate("add_medicine") },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                ) {
                    Text("Add Your First Medicine")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(medicines) { medicine ->
                    ScheduleMedicineListItem(
                        medicine = medicine,
                        onDelete = {
                            viewModel.delete(it)
                            NotificationScheduler.cancelAllNotifications(
                                navController.context,
                                it.id
                            )
                        },
                        onMarkTaken = { updatedMedicine ->
                            viewModel.markMedicineTaken(updatedMedicine)

                            Toast.makeText(
                                navController.context,
                                "${updatedMedicine.name} marked as taken!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    )
                }
            }
        }
    }
}




@Composable
fun ScheduleMedicineListItem(
    medicine: Medicine,
    onDelete: (Medicine) -> Unit,
    onMarkTaken: (Medicine) -> Unit
) {

    val todayDate = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date())
    }

    // Reset taken times if day changed
    val takenToday =
        if (medicine.lastTakenDate == todayDate)
            medicine.takenTimesToday
        else
            emptyList()

    val nextDoseTime = getNextAvailableDose(
        medicine.times,
        takenToday
    )

    val isButtonEnabled = nextDoseTime != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            /* -------- HEADER -------- */

            Row(verticalAlignment = Alignment.Top) {

                val photoPainter =
                    if (medicine.photoUri != null)
                        rememberAsyncImagePainter(Uri.parse(medicine.photoUri))
                    else
                        rememberAsyncImagePainter(android.R.drawable.ic_menu_info_details)

                Image(
                    painter = photoPainter,
                    contentDescription = medicine.name,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            medicine.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Medication, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Dosage: ${medicine.dosage}")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Schedule, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(medicine.times.joinToString(", "))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Event, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(medicine.frequencyType)
                    }
                }

                IconButton(
                    onClick = { onDelete(medicine) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete Medicine",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            /* -------- MARK TAKEN BUTTON -------- */

            Button(
                onClick = {
                    nextDoseTime?.let { dose ->
                        onMarkTaken(
                            medicine.copy(
                                takenTimesToday = takenToday + dose,
                                lastTakenDate = todayDate
                            )
                        )
                    }
                },
                enabled = isButtonEnabled,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isButtonEnabled)
                        Color(0xFF31473A)
                    else
                        Color.Gray
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isButtonEnabled)
                        "Mark as Taken (${nextDoseTime})"
                    else
                        "Mark as Taken"
                )
            }
        }
    }
}


fun isDoseTimePassed(timeV: String): Boolean {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val now = Calendar.getInstance()

    val dose = formatter.parse(timeV) ?: return false
    val doseCal = Calendar.getInstance().apply {
        time = dose
        set(Calendar.YEAR, now.get(Calendar.YEAR))
        set(Calendar.MONTH, now.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))
    }

    return now.timeInMillis >= doseCal.timeInMillis
}

fun getNextAvailableDose(
    times: List<String>,
    takenTimes: List<String>
): String? {
    for (time in times) {
        if (isDoseTimePassed(time) && !takenTimes.contains(time)) {
            return time
        }
    }
    return null
}



@Composable
fun ScheduleMedicineListItemOld(
    medicine: Medicine,
    onDelete: (Medicine) -> Unit,
    onMarkTaken: (Medicine) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                val photoPainter = if (medicine.photoUri != null) {
                    rememberAsyncImagePainter(Uri.parse(medicine.photoUri))
                } else {
                    painterResource(id = R.drawable.iv_tablet)
                }
                Image(
                    painter = photoPainter,
                    contentDescription = medicine.name,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = medicine.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        IconButton(
                            onClick = { onDelete(medicine) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete Medicine",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Medication,
                            contentDescription = "Dosage",
                            tint = DarkGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Dosage: ${medicine.dosage}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))


                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = "Time",
                            tint = DarkGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = medicine.times.joinToString(", "),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Event,
                            contentDescription = "Frequency",
                            tint = DarkGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val frequencyText = medicine.frequencyType +
                                if (medicine.frequencyType == "Specific Days" && medicine.specificDays != null) {
                                    val dayNames = medicine.specificDays.map { dayInt ->
                                        when (dayInt) {
                                            Calendar.SUNDAY -> "Su"
                                            Calendar.MONDAY -> "Mo"
                                            Calendar.TUESDAY -> "Tu"
                                            Calendar.WEDNESDAY -> "We"
                                            Calendar.THURSDAY -> "Th"
                                            Calendar.FRIDAY -> "Fr"
                                            Calendar.SATURDAY -> "Sa"
                                            else -> ""
                                        }
                                    }
                                    " (${dayNames.joinToString(", ")})"
                                } else ""
                        Text(
                            text = frequencyText,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {


                medicine.currentQuantity?.let {
                    if (it <= (medicine.refillThreshold ?: 0)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Warning,
                                contentDescription = "Low Stock Warning",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Low on stock: $it doses left",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

            }


            Button(
                onClick = { onMarkTaken(medicine) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Mark Taken",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mark as Taken")
            }
        }
    }
}
