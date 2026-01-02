package s3494133.syambabu.medicinereminder

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@Composable
fun SignUpScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }

    var email by remember { mutableStateOf("") }
    var phonenumber by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val context1 = LocalContext.current

    var dobDate by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    fun openDatePicker(onSelect: (String) -> Unit, minDate: Long? = null) {
        val dp = DatePickerDialog(
            context1,
            { _, year, month, day ->
                val c = Calendar.getInstance().apply {
                    set(year, month, day)
                }

                val selectedDob = dateFormat.format(c.time)
                onSelect(selectedDob)

                val today = Calendar.getInstance()
                var calculatedAge = today.get(Calendar.YEAR) - year

                if (
                    today.get(Calendar.MONTH) < month ||
                    (today.get(Calendar.MONTH) == month && today.get(Calendar.DAY_OF_MONTH) < day)
                ) {
                    calculatedAge--
                }

                age = calculatedAge.toString()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        dp.datePicker.calendarViewShown = true
        dp.datePicker.spinnersShown = true

        if (minDate != null) dp.datePicker.minDate = minDate

        dp.datePicker.maxDate = System.currentTimeMillis()

        dp.show()
    }


    val context = LocalContext.current.findActivity()

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


                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(listOf(Color.Gray, Color.Gray)),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Enter Your Name") }
                    )

                    Spacer(modifier = Modifier.height(6.dp))


                    DOBDateField(
                        label = "Date of Birth",
                        value = dobDate,
                        onClick = {
                            val today = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.timeInMillis

                            openDatePicker({ dobDate = it }, 1900)
                        }
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(listOf(Color.Gray, Color.Gray)),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        value = age,
                        onValueChange = { },
                        label = { Text("Age") },
                        readOnly = true
                    )


                    Spacer(modifier = Modifier.height(6.dp))


                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(listOf(Color.Gray, Color.Gray)),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Enter Your Email") }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(listOf(Color.Gray, Color.Gray)),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        value = phonenumber,
                        onValueChange = { input ->

                            val digitsOnly = input.filter { it.isDigit() }

                            phonenumber = digitsOnly.take(10)
                        },
                        label = { Text("Enter Your Phone Number") },
                        leadingIcon = {
                            Text(
                                text = "+44",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )



                    Spacer(modifier = Modifier.height(6.dp))

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

                            val description =
                                if (passwordVisible) "Hide password" else "Show password"

                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, description)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                    }

                    Button(
                        onClick = {
                            when {
                                name.isEmpty() -> {
                                    Toast.makeText(
                                        context,
                                        " Please Enter Name",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                age.isEmpty() -> {
                                    Toast.makeText(context, " Please Enter Age", Toast.LENGTH_SHORT)
                                        .show()
                                }

                                email.isEmpty() -> {
                                    Toast.makeText(
                                        context,
                                        " Please Enter Mail",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                phonenumber.isEmpty() -> {
                                    Toast.makeText(
                                        context,
                                        " Please Enter Phone Number",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }

                                password.isEmpty() -> {
                                    Toast.makeText(
                                        context,
                                        " Please Enter Password",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }

                                else -> {

                                    val encryptedPassword = CryptoUtils.encrypt(password)

                                    val ref = FirebaseDatabase.getInstance()
                                        .getReference("PatientAccounts")

                                    val emailKey = email.replace(".", ",")

                                    ref.child(emailKey).get()
                                        .addOnSuccessListener { emailSnapshot ->

                                            if (emailSnapshot.exists()) {
                                                Toast.makeText(
                                                    context,
                                                    "Email already registered!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                return@addOnSuccessListener
                                            }

                                            ref.get().addOnSuccessListener { allUsersSnapshot ->

                                                var phoneExists = false

                                                for (child in allUsersSnapshot.children) {
                                                    val savedPhone =
                                                        child.child("phone").value?.toString() ?: ""
                                                    if (savedPhone == phonenumber) {
                                                        phoneExists = true
                                                        break
                                                    }
                                                }

                                                if (phoneExists) {
                                                    Toast.makeText(
                                                        context,
                                                        "Phone number already registered!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    return@addOnSuccessListener
                                                }

                                                val userData = PatientData(
                                                    name = name,
                                                    dob = dobDate,
                                                    email = email,
                                                    age = age,
                                                    phone = phonenumber,
                                                    password = encryptedPassword
                                                )

                                                ref.child(emailKey).setValue(userData)
                                                    .addOnCompleteListener { task ->
                                                        if (task.isSuccessful) {
                                                            Toast.makeText(
                                                                context,
                                                                "Registration Successful",
                                                                Toast.LENGTH_SHORT
                                                            ).show()

                                                            navController.navigate(NavigationScreens.Login.route) {
                                                                popUpTo(NavigationScreens.Register.route) {
                                                                    inclusive = true
                                                                }
                                                            }
                                                        } else {
                                                            Toast.makeText(
                                                                context,
                                                                "Registration Failed: ${task.exception?.message}",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }
                                                    .addOnFailureListener { exception ->
                                                        Toast.makeText(
                                                            context,
                                                            "Registration Failed: ${exception.message}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }

                                            }

                                        }

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
                        Text(text = "Sign Up", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(text = "You are a old user ?", color = Color.White, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Sign In",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.clickable {

                                navController.navigate(NavigationScreens.Login.route) {
                                    popUpTo(NavigationScreens.Register.route) {
                                        inclusive = true
                                    }
                                }

                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))


                }
            }
        }
    )
}

data class PatientData
    (
    var name: String = "",
    var dob: String = "",
    var age: String = "",
    var email: String = "",
    var phone: String = "",
    var password: String = "",
)


@Composable
fun DOBDateField(label: String, value: String, onClick: () -> Unit) {
    Column {

        Box {
            TextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Select DOB") },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(listOf(Color.Gray, Color.Gray)),
                        shape = RoundedCornerShape(16.dp)
                    )
            )
            Box(
                Modifier
                    .matchParentSize()
                    .clickable { onClick() }
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    SignUpScreen(navController = NavHostController(LocalContext.current))
}
