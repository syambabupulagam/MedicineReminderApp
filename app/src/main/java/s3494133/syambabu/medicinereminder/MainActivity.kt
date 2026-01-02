package s3494133.syambabu.medicinereminder


import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import s3494133.syambabu.medicinereminder.homenavigations.AddMedicineScreen
import s3494133.syambabu.medicinereminder.ui.theme.MedicineReminderTheme
import s3494133.syambabu.medicinereminder.data.MedicineViewModel
import s3494133.syambabu.medicinereminder.homenavigations.AboutUsScreen
import s3494133.syambabu.medicinereminder.homenavigations.GlobalMedicineHistoryListScreen
import s3494133.syambabu.medicinereminder.homenavigations.MedicineHistoryScreen
import s3494133.syambabu.medicinereminder.homenavigations.MedicineListScreen
import s3494133.syambabu.medicinereminder.ui.theme.DarkGreen
import s3494133.syambabu.medicinereminder.utils.NavigationScreens
import s3494133.syambabu.medicinereminder.utils.NotificationScheduler
import s3494133.syambabu.medicinereminder.utils.UserLocalData

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationScheduler.createNotificationChannels(this)

        setContent {
            MedicineReminderTheme {
                MedicineReminderApp()
            }
        }
    }

}

@Composable
fun MedicineReminderApp() {
    val navController = rememberNavController()

    val context = LocalContext.current
    val viewModel: MedicineViewModel = viewModel(
        factory = MedicineViewModel.MedicineViewModelFactory(context.applicationContext as Application)
    )

    NavHost(navController = navController, startDestination = NavigationScreens.Splash.route) {

        composable(NavigationScreens.Splash.route) {
            SplashScreen(navController = navController)
        }

        composable(NavigationScreens.Login.route) {
            SessionActivityScreen(navController = navController)
        }

        composable(NavigationScreens.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController)
        }

        composable(NavigationScreens.Register.route) {
            SignUpScreen(navController = navController)
        }
        composable(NavigationScreens.Home.route) {
            DashboardScreen(navController = navController)
        }

        composable(NavigationScreens.Profile.route) {
            ProfileScreen(navController = navController)
        }

        composable(NavigationScreens.AboutUs.route) {
            AboutUsScreen(navController = navController)
        }

        composable(NavigationScreens.AddMedicine.route) {
            AddMedicineScreen(navController = navController, viewModel = viewModel)
        }

        composable(NavigationScreens.ViewMedicine.route) {
            MedicineListScreen(navController = navController, viewModel = viewModel)
        }
        composable(NavigationScreens.GlobalMedicineHistoryList.route) {
            GlobalMedicineHistoryListScreen(navController = navController, viewModel = viewModel)
        }

        composable("medicine_history/{medicineId}") { backStackEntry ->
            val medicineId = backStackEntry.arguments?.getString("medicineId")?.toIntOrNull()
            if (medicineId != null) {
                MedicineHistoryScreen(navController = navController, viewModel = viewModel, medicineId = medicineId)
            } else {
                Toast.makeText(LocalContext.current, "Medicine ID not found for history.", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
        }


    }
}

@Composable
fun SplashScreen(navController: NavController) {

    val context = LocalContext.current as Activity


    LaunchedEffect(Unit) {
        delay(3000)

        val patientStatus = UserLocalData.checkLoginStatus(context)
        if (patientStatus) {
            navController.navigate(NavigationScreens.Home.route) {
                popUpTo(NavigationScreens.Splash.route) {
                    inclusive = true
                }
            }
        } else {
            navController.navigate(NavigationScreens.Login.route) {
                popUpTo(NavigationScreens.Splash.route) {
                    inclusive = true
                }
            }
        }
    }

    MedicineReminderSplashScreenDesign()
}

@Composable
fun MedicineReminderSplashScreenDesign() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGreen),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Spacer(modifier = Modifier.height(94.dp))

            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = "Medicine Reminder",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )


            Card(
                modifier = Modifier
                    .padding(16.dp)
            )
            {

                Column(
                    modifier = Modifier
                        .width(300.dp)
                        .background(color = Color.Transparent),
                )
                {
                    Image(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        painter = painterResource(id = R.drawable.ic_medicine_reminder),
                        contentDescription = "Medicine Reminder",
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "By",
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.black), // Green color similar to the design
                        fontSize = 26.sp,
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Syam Babu",
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen, // Green color similar to the design
                        fontSize = 26.sp,
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(6.dp))


                }
            }


        }
    }

}


@Preview(showBackground = true)
@Composable
fun SplashScrOnBoardingScreenDPreview() {
    MedicineReminderSplashScreenDesign()
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}