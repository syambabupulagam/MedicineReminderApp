package s3494133.syambabu.medicinereminder.utils

sealed class NavigationScreens(val route: String) {
    object Splash : NavigationScreens("splash_route")
    object Home : NavigationScreens("dashboard")
    object Login : NavigationScreens("login")
    object Profile : NavigationScreens("profile")
    object AboutUs : NavigationScreens("aboutus")
    object ForgotPassword : NavigationScreens("forgot_password")
    object Register : NavigationScreens("register")
    object AddMedicine : NavigationScreens("add_medicine")
    object ViewMedicine : NavigationScreens("view_medicines")
    object GlobalMedicineHistoryList : NavigationScreens("global_medicine_history_list")

}