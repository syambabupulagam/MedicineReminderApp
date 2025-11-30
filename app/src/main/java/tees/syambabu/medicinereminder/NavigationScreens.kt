package tees.syambabu.medicinereminder

sealed class NavigationScreens(val route: String) {
    object Splash : NavigationScreens("splash_route")
    object Home : NavigationScreens("dashboard")
    object Login : NavigationScreens("login")
    object Register : NavigationScreens("register")
    object AddMedicine : NavigationScreens("add_medicine")
}