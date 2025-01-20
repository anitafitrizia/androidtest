package com.jetpackanita

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jetpackanita.ui.theme.JetpackAnitaTheme
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JetpackAnitaTheme {
                var isLoggedIn by remember { mutableStateOf(false) }

                if (isLoggedIn) {
                    HomePageScreen(onLogout = { isLoggedIn = false })
                } else {
                    LoginScreen(onLoginSuccess = { isLoggedIn = true })
                }
            }
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val coroutineScope = rememberCoroutineScope() // coroutine scope

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var alertMessage by remember { mutableStateOf("") }
    var showAlert by remember { mutableStateOf(false) }

    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            title = { Text(text = "Message") },
            text = { Text(text = alertMessage) },
            confirmButton = {
                Button(onClick = { showAlert = false }) {
                    Text("OK")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (username.isEmpty() || password.isEmpty()) {
                    alertMessage = "Please enter both username and password."
                    showAlert = true
                } else {
                    coroutineScope.launch {
                        handleLogin(username, password, onLoginSuccess, {
                            alertMessage = it
                            showAlert = true
                        })
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
    }
}


suspend fun handleLogin(
    username: String,
    password: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    withContext(Dispatchers.IO) {
        val loginDetails = JSONObject().apply {
            put("email", username)
            put("password", password)
        }

        val url = URL("https://reqres.in/api/login")
        val connection = url.openConnection() as HttpsURLConnection

        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        connection.outputStream.use { it.write(loginDetails.toString().toByteArray()) }

        val responseCode = connection.responseCode
        withContext(Dispatchers.Main) {
            if (responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                if (response.contains("token")) {
                    onSuccess()
                } else {
                    onError("Invalid username or password.")
                }
            } else {
                onError("Request failed with code $responseCode")
            }
        }
    }
}

@Composable
fun HomePageScreen(onLogout: () -> Unit) {
    var singleUser by remember { mutableStateOf<User?>(null) }
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Welcome to the Home Page!", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    singleUser = fetchSingleUser()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(Color.Green)
        ) {
            Text("Get Single User")
        }

        singleUser?.let { user ->
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                AsyncImage(
                    model = user.avatar,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("${user.firstName} ${user.lastName}", style = MaterialTheme.typography.bodyLarge)
                    Text(user.email, style = MaterialTheme.typography.bodySmall)
                }
            }
            // Button to clear the single user
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { singleUser = null },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(Color.Red)
            ) {
                Text("Clear Single User")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    users = fetchUsersList()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(Color.Blue)
        ) {
            Text("List Users")
        }

        users.forEach { user ->
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                AsyncImage(
                    model = user.avatar,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("${user.firstName} ${user.lastName}", style = MaterialTheme.typography.bodyLarge)
                    Text(user.email, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Clear Users List Button (outside the forEach loop)
        if (users.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { users = emptyList() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(Color.Red)
            ) {
                Text("Clear Users List")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(Color.Red)
        ) {
            Text("Logout", color = Color.White)
        }
    }
}

suspend fun fetchSingleUser(): User? {
    val url = URL("https://reqres.in/api/users/2")

    return try {
        val response = withContext(Dispatchers.IO) {
            url.readText()
        }

        val jsonResponse = JSONObject(response).getJSONObject("data")
        User(
            id = jsonResponse.getInt("id"),
            email = jsonResponse.getString("email"),
            firstName = jsonResponse.getString("first_name"),
            lastName = jsonResponse.getString("last_name"),
            avatar = jsonResponse.getString("avatar")
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun fetchUsersList(): List<User> {
    val url = URL("https://reqres.in/api/users?page=1")

    return try {
        val response = withContext(Dispatchers.IO) {
            url.readText()
        }

        val jsonArray = JSONObject(response).getJSONArray("data")
        List(jsonArray.length()) { index ->
            val jsonUser = jsonArray.getJSONObject(index)
            User(
                id = jsonUser.getInt("id"),
                email = jsonUser.getString("email"),
                firstName = jsonUser.getString("first_name"),
                lastName = jsonUser.getString("last_name"),
                avatar = jsonUser.getString("avatar")
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

data class User(
    val id: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val avatar: String
)