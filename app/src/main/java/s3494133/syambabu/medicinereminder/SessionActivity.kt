package s3494133.syambabu.medicinereminder


import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.firebase.database.FirebaseDatabase
import s3494133.syambabu.medicinereminder.ui.theme.DarkGreen
import s3494133.syambabu.medicinereminder.utils.CryptoUtils
import s3494133.syambabu.medicinereminder.utils.NavigationScreens
import s3494133.syambabu.medicinereminder.utils.UserPrefs


@Composable
fun SessionActivityScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current.findActivity()

    val context1 = LocalContext.current

    Scaffold(
        topBar = {},
        content = { innerPadding ->


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(DarkGreen)
            ) {

                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    painter = painterResource(id = R.drawable.ic_medicine_reminder),
                    contentDescription = "Medicine Reminder",
                )


                // Bottom section with email, password fields, and sign-in button on a white background
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(16.dp), // Padding for the fields
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {


                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(listOf(Color.Gray, Color.Gray)),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Enter Your Email/PhoneNumber") }
                    )

                    Spacer(modifier = Modifier.height(6.dp)) // Space between fields

                    // Password Text Field
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(listOf(Color.Gray, Color.Gray)),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Enter Your Password") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible)
                                Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff

                            val description = if (passwordVisible) "Hide password" else "Show password"

                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, description)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp)) // Space between fields and button

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                    }
                    // Sign In Button
                    Button(
                        onClick = {
                            when {
                                email.isEmpty() -> {
                                    Toast.makeText(
                                        context,
                                        " Please Enter Mail/Phone Number",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                password.isEmpty() -> {
                                    Toast.makeText(
                                        context,
                                        " Please Enter Password",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                else -> {

                                    loginUser(email, password, context1, navController)


                                }

                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = DarkGreen
                        )
                    ) {
                        Text(text = "Sign In", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.weight(1f)) // Space between form section and sign-up text

                    // Sign Up text section
                    Row(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(text = "You are a new user ?", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Sign Up",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White, // Blue text color for "Sign Up"
                            modifier = Modifier.clickable {

                                navController.navigate(NavigationScreens.Register.route) {
                                    popUpTo(NavigationScreens.Login.route) {
                                        inclusive = true
                                    }
                                }

                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp)) // Space between fields and button


                }
            }

        }
    )
}

fun loginUser(loginInput: String, password: String, context: Context, navController: NavController) {

    val db = FirebaseDatabase.getInstance().getReference("PatientAccounts")

    // First: try login as Email
    if (loginInput.contains("@")) {

        val emailKey = loginInput.replace(".", ",")

        db.child(emailKey).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val savedPassword = snapshot.child("password").value.toString()
                val decryptedPassword = CryptoUtils.decrypt(savedPassword)

                if (decryptedPassword == password) {
                    Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()

                    UserPrefs.markLoginStatus(context, true)
                    UserPrefs.saveEmail(
                        context,
                        email = loginInput
                    )
                    UserPrefs.saveName(context, snapshot.child("name").value.toString())



                    navController.navigate(NavigationScreens.Home.route) {
                        popUpTo(NavigationScreens.Login.route) { inclusive = true }
                    }

                } else {
                    Toast.makeText(context, "Incorrect Password", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "No account with this email", Toast.LENGTH_SHORT).show()
            }
        }

    } else {
        // Second: login using Phone number
        db.get().addOnSuccessListener { snapshot ->
            var found = false

            for (child in snapshot.children) {
                val savedPhone = child.child("phone").value.toString()
                val savedPassword = child.child("password").value.toString()
                val decryptedPassword = CryptoUtils.decrypt(savedPassword)

                if (savedPhone == loginInput) {
                    found = true
                    if (decryptedPassword == password) {

                        UserPrefs.markLoginStatus(context, true)
                        UserPrefs.saveEmail(
                            context,
                            email = child.child("email").value.toString()
                        )
                        UserPrefs.saveName(context, child.child("name").value.toString())

                        Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()

                        navController.navigate(NavigationScreens.Home.route) {
                            popUpTo(NavigationScreens.Login.route) { inclusive = true }
                        }

                    } else {
                        Toast.makeText(context, "Incorrect Password", Toast.LENGTH_SHORT).show()
                    }
                    break
                }
            }

            if (!found) {
                Toast.makeText(context, "No account with this phone number", Toast.LENGTH_SHORT).show()
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun SessionActivityScreenPreview() {
    SessionActivityScreen(navController = NavHostController(LocalContext.current))
}
