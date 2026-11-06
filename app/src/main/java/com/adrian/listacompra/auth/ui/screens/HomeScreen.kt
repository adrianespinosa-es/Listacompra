package com.adrian.listacompra.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adrian.listacompra.data.ShoppingListRepository
import com.google.firebase.auth.FirebaseAuth
import com.adrian.listacompra.data.ShoppingList
@Composable
fun HomeScreen() {

    val repository = remember { ShoppingListRepository() }
    val user = FirebaseAuth.getInstance().currentUser

    var lists by remember {
        mutableStateOf<List<ShoppingList>>(emptyList())
    }
    var selectedList by remember {
        mutableStateOf<ShoppingList?>(null)
    }

    var loading by remember {
        mutableStateOf(true)
    }
    var newListName by remember {
        mutableStateOf("")
    }

    var message by remember {
        mutableStateOf("")
    }

    LaunchedEffect(user?.uid) {

        if (user != null) {

            repository.getAllMyLists(user.uid) { result ->
                lists = result
                loading = false
            }

        } else {
            loading = false
        }
    }
    if (selectedList != null) {
        ShoppingListScreen(
            listId = selectedList!!.id,
            listName = selectedList!!.name,
            ownerId = selectedList!!.ownerId,
            onBack = {
                selectedList = null
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Mis listas de compra",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
            }
        ) {
            Text("Cerrar sesión")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = newListName,
            onValueChange = { newListName = it },
            label = { Text("Nombre de la lista") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = message)

        Button(
            onClick = {
                val name = newListName.trim()

                if (name.isNotEmpty() && user != null) {
                    repository.createList(
                        name = name,
                        ownerId = user.uid
                    ) { success, result ->

                        if (success) {
                            newListName = ""
                            message = "Lista creada correctamente"

                            repository.getAllMyLists(user.uid) { updatedLists ->
                                lists = updatedLists
                            }

                        } else {
                            message = "Error al crear la lista: $result"
                        }
                    }
                } else {
                    message = "Escribe un nombre para la lista"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear lista")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            loading -> {
                CircularProgressIndicator()
            }

            lists.isEmpty() -> {
                Text("Todavía no tienes listas.")
            }

            else -> {
                lists.forEach { list ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        onClick = {
                            selectedList = list
                        }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = list.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                }
            }
        }
    }
}