
package com.adrian.listacompra.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.TextButton
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
@Composable
fun RegisterScreen(onLoginClick: () -> Unit) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Crear cuenta",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Repetir contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {

                when {
                    email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                        message = "Rellena todos los campos"
                    }

                    password != confirmPassword -> {
                        message = "Las contraseñas no coinciden"
                    }

                    password.length < 6 -> {
                        message = "La contraseña debe tener al menos 6 caracteres"
                    }

                    else -> {
                        isLoading = true
                        message = ""

                        auth.createUserWithEmailAndPassword(
                            email.trim(),
                            password
                        ).addOnCompleteListener { task ->

                            isLoading = false

                            if (task.isSuccessful) {

                                val user = auth.currentUser

                                if (user != null) {
                                    val userData = hashMapOf(
                                        "uid" to user.uid,
                                        "email" to user.email
                                    )

                                    FirebaseFirestore.getInstance()
                                        .collection("users")
                                        .document(user.uid)
                                        .set(userData)
                                        .addOnSuccessListener {
                                            Log.d("SHARE_DEBUG", "Usuario guardado con UID: ${user.uid}")

                                            FirebaseFirestore.getInstance()
                                                .collection("users")
                                                .document(user.uid)
                                                .get()
                                                .addOnSuccessListener { document ->
                                                    Log.d("SHARE_DEBUG", "¿Documento existe?: ${document.exists()}")
                                                    Log.d("SHARE_DEBUG", "Datos guardados: ${document.data}")
                                                }
                                                .addOnFailureListener { exception ->
                                                    Log.e("SHARE_DEBUG", "Error leyendo el usuario guardado", exception)
                                                }

                                            message = "¡Cuenta creada correctamente!"
                                        }
                                        .addOnFailureListener { exception ->
                                            message = "Error al guardar el usuario: ${exception.localizedMessage}"
                                        }
                                }

                            } else {
                                message = task.exception?.localizedMessage
                                    ?: "Error al crear la cuenta"
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Registrarme")
        }
        TextButton(
            onClick = onLoginClick
        ) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        }

        if (message.isNotEmpty()) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
