package s3494133.syambabu.medicinereminder.homenavigations

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import s3494133.syambabu.medicinereminder.data.MedicineHistory
import s3494133.syambabu.medicinereminder.data.MedicineViewModel
import s3494133.syambabu.medicinereminder.ui.theme.DarkGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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