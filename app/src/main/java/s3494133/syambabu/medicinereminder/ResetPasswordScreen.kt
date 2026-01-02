package s3494133.syambabu.medicinereminder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.database.FirebaseDatabase
import s3494133.syambabu.medicinereminder.ui.theme.DarkGreen
import s3494133.syambabu.medicinereminder.utils.CryptoUtils

@Composable
fun ForgotPasswordScreen(navController: NavController) {

    var email by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var step2 by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    val context = LocalContext.current

    Scaffold(
        topBar = {},
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkGreen)
                .padding(16.dp)
        ) {

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Forgot Password?",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Text(
                text = "Reset it now!",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(40.dp))


            if (!step2) {

                Text("Enter Email Address", color = Color.White)

                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Gray, RoundedCornerShape(16.dp)),
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Email") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Enter Date of Birth", color = Color.White)

                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Gray, RoundedCornerShape(16.dp)),
                    value = dob,
                    onValueChange = { dob = it },
                    placeholder = { Text("dd-mm-yyyy") }
                )

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = {
                        if (email.isEmpty() || dob.isEmpty()) {
                            errorMessage = "Please fill all fields"
                            return@Button
                        }

                        errorMessage = ""
                        loading = true

                        val key = email.replace(".", ",")

                        FirebaseDatabase.getInstance()
                            .getReference("PatientAccounts")
                            .child(key)
                            .get()
                            .addOnSuccessListener { snapshot ->
                                loading = false

                                if (!snapshot.exists()) {
                                    errorMessage = "Account not found"
                                    return@addOnSuccessListener
                                }

                                val dbEmail = snapshot.child("email").value.toString()
                                val dbDob = snapshot.child("dob").value.toString()

                                if (dbEmail == email && dbDob == dob) {
                                    step2 = true
                                } else {
                                    errorMessage = "Incorrect Email or DOB"
                                }
                            }
                            .addOnFailureListener {
                                loading = false
                                errorMessage = "Error: ${it.localizedMessage}"
                            }

                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = DarkGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Verify")
                }
            }


            if (step2) {

                Text("Enter New Password", color = Color.White)

                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Gray, RoundedCornerShape(16.dp)),
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    placeholder = { Text("New Password") },
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Confirm Password", color = Color.White)

                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Gray, RoundedCornerShape(16.dp)),
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = { Text("Confirm Password") },
                )

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = {
                        if (newPassword != confirmPassword) {
                            errorMessage = "Passwords do not match"
                            return@Button
                        }

                        loading = true
                        val key = email.replace(".", ",")

                        val encryptedPass = CryptoUtils.encrypt(newPassword)

                        FirebaseDatabase.getInstance()
                            .getReference("PatientAccounts")
                            .child(key)
                            .child("password")
                            .setValue(encryptedPass)
                            .addOnSuccessListener {
                                loading = false
                                successMessage = "Password updated successfully!"

                                navController.popBackStack()
                            }
                            .addOnFailureListener {
                                loading = false
                                errorMessage = "Failed to update password"
                            }

                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = DarkGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Update Password")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (loading)
                Text("Processing...", color = Color.White, modifier = Modifier.align(Alignment.CenterHorizontally))

            if (errorMessage.isNotEmpty())
                Text(errorMessage, color = Color.Red, modifier = Modifier.align(Alignment.CenterHorizontally))

            if (successMessage.isNotEmpty())
                Text(successMessage, color = Color.Green, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}
