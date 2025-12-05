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
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import s3494133.syambabu.medicinereminder.R
import s3494133.syambabu.medicinereminder.data.Medicine
import s3494133.syambabu.medicinereminder.data.MedicineHistory
import s3494133.syambabu.medicinereminder.data.MedicineViewModel
import s3494133.syambabu.medicinereminder.ui.theme.DarkGreen
import s3494133.syambabu.medicinereminder.utils.NotificationScheduler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryMedicineListItem(
    medicine: Medicine,
    onDelete: (Medicine) -> Unit,
    onViewHistory: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val photoPainter = if (medicine.photoUri != null) {
                rememberAsyncImagePainter(Uri.parse(medicine.photoUri))
            } else {
                painterResource(id = R.drawable.iv_tablet)
            }
            Image(
                painter = photoPainter,
                contentDescription = "Medicine Photo",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medicine.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Dosage: ${medicine.dosage}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = { onDelete(medicine) },
                modifier = Modifier.align(Alignment.Top)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Medicine",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        Button(
            onClick = { onViewHistory(medicine.id) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("View Full History")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalMedicineHistoryListScreen(navController: NavController, viewModel: MedicineViewModel) {
    val medicines by viewModel.allMedicines.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Medicines History",
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
                        )                     }
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
                    text = "No medicines added yet to view history.",
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
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(medicines) { medicine ->
                    HistoryMedicineListItem( // Using the new HistoryMedicineListItem
                        medicine = medicine,
                        onDelete = {
                            viewModel.delete(it)
                            NotificationScheduler.cancelAllNotifications(
                                navController.context,
                                it.id
                            )
                            Toast.makeText(
                                navController.context,
                                "${it.name} deleted.",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onViewHistory = { medId ->
                            navController.navigate("medicine_history/$medId")
                        }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineHistoryScreen(navController: NavController, viewModel: MedicineViewModel, medicineId: Int) {
    val medicineHistory by viewModel.getMedicineHistoryForMedicine(medicineId).collectAsState(initial = emptyList())
    val medicine by viewModel.getMedicineById(medicineId).collectAsState(initial = null)
    val context = navController.context

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${medicine?.name ?: "Medicine"} History", style = MaterialTheme.typography.headlineSmall, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, // Changed to AutoMirrored
                            contentDescription = "Back",
                            tint = Color.White // Set icon color
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkGreen
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (medicineHistory.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f), // Take available space
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No history recorded for this medicine yet.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f) // Take available space
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp), // Increased horizontal padding
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp) // More space between history items
                ) {
                    items(medicineHistory) { historyEntry ->
                        MedicineHistoryItem(historyEntry = historyEntry) // Using the dedicated item composable
                    }
                }
            }

            // "Delete All History" Button at the bottom
            if (medicineHistory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp)) // Space above the button
                Button(
                    onClick = {
                        viewModel.deleteHistoryForMedicine(medicineId)
                        Toast.makeText(context, "History deleted for ${medicine?.name ?: "this medicine"}", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp) // Horizontal padding for the button
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp) // Rounded button
                ) {
                    Text("Delete All History")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun MedicineHistoryItem(historyEntry: MedicineHistory) {
    val formattedDate = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()).format(Date(historyEntry.takenTimestamp))
    val formattedTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(historyEntry.takenTimestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "at $formattedTime",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = historyEntry.dosageTaken,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (historyEntry.dosageTaken == "Taken") DarkGreen else MaterialTheme.colorScheme.error
            )
        }
    }
}
