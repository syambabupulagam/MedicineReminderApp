package tees.syambabu.medicinereminder

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import tees.syambabu.medicinereminder.ui.theme.Blue
import tees.syambabu.medicinereminder.ui.theme.LightLavender
import tees.syambabu.medicinereminder.ui.theme.LightPink
import tees.syambabu.medicinereminder.ui.theme.Orange
import tees.syambabu.medicinereminder.ui.theme.Pink
import tees.syambabu.medicinereminder.ui.theme.PurpleDeep


@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    DashboardScreen(navController = NavHostController(LocalContext.current))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Medicine Reminder App",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Orange,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = { // Add actions block for trailing icons
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onPrimary // Ensure the icon color matches title
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Hi , User",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Manage Your Medicines",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            DashboardCard(
                title = "Add New Medicine",
                description = "Set name, dosage, time, and frequency.",
                icon = R.drawable.iv_add_medicine,
                onClick = {
                    navController.navigate("add_medicine")
                },
                PurpleDeep
            )
            Spacer(modifier = Modifier.height(16.dp))

            DashboardCard(
                title = "Scheduled Medicines",
                description = "See all your upcoming medicine schedules.",
                icon = R.drawable.iv_schedule_medicine,
                onClick = { },
                LightPink
            )
            Spacer(modifier = Modifier.height(16.dp))

            DashboardCard(
                title = "Medicine History",
                description = "View past intake records for each medicine.",
                icon = R.drawable.iv_medicine_history,
                onClick = {
                },
                Blue
            )

            Spacer(modifier = Modifier.height(16.dp))

            DashboardCard(
                title = "About Us",
                description = "Learn more about the app and contact us.",
                icon = R.drawable.iv_aboutus, // Using Info icon
                onClick = {
                },
                LightLavender
            )
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    description: String,
    icon: Int,
    onClick: () -> Unit,
    cardColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor =
            cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {


            Icon(
                painter = painterResource(id = icon), // Pass your drawable resource here
                contentDescription = null, // Provide a meaningful description if it's not purely decorative
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}