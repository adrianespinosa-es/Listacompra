
package com.adrian.listacompra.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.adrian.listacompra.ui.screens.HomeScreen

@Composable
fun AuthScreen() {

    val auth = remember { FirebaseAuth.getInstance() }

    var currentUser by remember {
        mutableStateOf(auth.currentUser)
    }

    var showRegister by remember {
        mutableStateOf(false)
    }

    DisposableEffect(auth) {

        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }

        auth.addAuthStateListener(listener)

        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser != null) {

        HomeScreen()

    } else {

        if (showRegister) {

            RegisterScreen(
                onLoginClick = {
                    showRegister = false
                }
            )

        } else {

            LoginScreen(
                onRegisterClick = {
                    showRegister = true
                }
            )
        }
    }
}
