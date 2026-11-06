
package com.adrian.listacompra.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.adrian.listacompra.data.ShoppingItem
import com.adrian.listacompra.data.ShoppingCategory
import com.adrian.listacompra.data.ShoppingListRepository
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ShoppingListScreen(
    listId: String,
    listName: String,
    ownerId: String,
    onBack: () -> Unit
) {
    val repository = remember { ShoppingListRepository()
    }

    var shoppingItems by remember {
        mutableStateOf<List<ShoppingItem>>(emptyList())
    }
    var showShareDialog by remember { mutableStateOf(false) }
    var shareEmail by remember { mutableStateOf("") }
    var shareRole by remember { mutableStateOf("editor") }
    var shareMessage by remember { mutableStateOf("") }
    var isSharing by remember { mutableStateOf(false) }
    var shoppingCategories by remember {
        mutableStateOf<List<ShoppingCategory>>(emptyList())
    }

    var loading by remember { mutableStateOf(true) }
    var newItemName by remember { mutableStateOf("") }
    var newItemQuantity by remember { mutableStateOf("1") }
    var newCategoryName by remember { mutableStateOf("") }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var editingCategory by remember {
        mutableStateOf<ShoppingCategory?>(null)
    }

    var editCategoryName by remember {
        mutableStateOf("")
    }

    var deletingCategory by remember {
        mutableStateOf<ShoppingCategory?>(null)
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Producto que estamos editando
    var editingItem by remember {
        mutableStateOf<ShoppingItem?>(null)
    }

    var editName by remember { mutableStateOf("") }
    var editQuantity by remember { mutableStateOf("1") }
    var selectedCategoryId by remember {
        mutableStateOf("")
    }

    // Producto que queremos eliminar
    var deletingItem by remember {
        mutableStateOf<ShoppingItem?>(null)
    }

    fun reloadItems() {
        repository.getItems(listId) { result ->
            shoppingItems = result
            loading = false
        }
    }
    fun reloadCategories() {
        repository.getCategories(listId) { result ->
            shoppingCategories = result
        }
    }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

    LaunchedEffect(listId) {
        loading = true
        reloadItems()
        reloadCategories()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = listName,
            style = MaterialTheme.typography.headlineMedium
        )
        // BOTÓN COMPARTIR
        OutlinedButton(
            onClick = {
                showShareDialog = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Compartir lista")
        }

        Spacer(modifier = Modifier.height(20.dp))

        var showCategories by remember {
            mutableStateOf(false)
        }


// AÑADIMOS CATEGORIAS
        Text(
            text = "Categorías",
            style = MaterialTheme.typography.titleMedium
        )

        Button(
            onClick = {
                showCategoryDialog = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear categoría")
        }

// Botón para mostrar u ocultar las categorías
        OutlinedButton(
            onClick = {
                showCategories = !showCategories
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (showCategories) {
                    "Ocultar categorías ▲"
                } else {
                    "Mostrar categorías ▼"
                }
            )
        }

// Mostrar las categorías solamente cuando esté desplegado
        if (showCategories) {
            shoppingCategories.forEach { category ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category.name,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            editingCategory = category
                            editCategoryName = category.name
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar categoría"
                        )
                    }

                    IconButton(
                        onClick = {
                            deletingCategory = category
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar categoría"
                        )
                    }
                }
            }
        }


        //AÑADIMOS UNN PRODUCTO NUEVO
        Text(
            text = "Añadir producto",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = newItemName,
            onValueChange = { newItemName = it },
            label = { Text("Nombre del producto") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = newItemQuantity,
            onValueChange = { newItemQuantity = it },
            label = { Text("Cantidad") },
            modifier = Modifier.fillMaxWidth()
        )

        var expandedCategoryMenu by remember {
            mutableStateOf(false)
        }

        val selectedCategory = shoppingCategories.find {
            it.id == selectedCategoryId
        }

// Selector de categoría
        OutlinedButton(
            onClick = {
                expandedCategoryMenu = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                selectedCategory?.name ?: "Seleccionar categoría"
            )
        }

        DropdownMenu(
            expanded = expandedCategoryMenu,
            onDismissRequest = {
                expandedCategoryMenu = false
            }
        ) {
            DropdownMenuItem(
                text = {
                    Text("Sin categoría")
                },
                onClick = {
                    selectedCategoryId = ""
                    expandedCategoryMenu = false
                }
            )

            shoppingCategories.forEach { category ->
                DropdownMenuItem(
                    text = {
                        Text(category.name)
                    },
                    onClick = {
                        selectedCategoryId = category.id
                        expandedCategoryMenu = false
                    }
                )
            }
        }
        Button(
            onClick = {
                val quantity = newItemQuantity.toIntOrNull()

                if (newItemName.isBlank()) {
                    errorMessage = "Escribe el nombre del producto"
                } else if (quantity == null || quantity <= 0) {
                    errorMessage = "Introduce una cantidad válida"
                } else {
                    repository.addItem(
                        listId = listId,
                        name = newItemName,
                        quantity = quantity,
                        categoryId = selectedCategoryId
                    ) { success, result ->

                        if (success) {
                            newItemName = ""
                            newItemQuantity = "1"
                            selectedCategoryId = ""
                            errorMessage = null
                            reloadItems()
                        } else {
                            errorMessage = result ?: "No se pudo añadir"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Añadir producto")
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Productos de la lista",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (loading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (shoppingItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Todavía no hay productos en esta lista.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = shoppingItems,
                    key = { it.id }
                ) { item ->

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        //LISTA DE LA COMPRA
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = item.isBought,
                                    onCheckedChange = { checked ->

                                        // Actualizar inmediatamente la pantalla
                                        shoppingItems = shoppingItems.map { currentItem ->
                                            if (currentItem.id == item.id) {
                                                currentItem.copy(isBought = checked)
                                            } else {
                                                currentItem
                                            }
                                        }

                                        // Guardar el cambio en Firebase
                                        repository.updateItem(
                                            listId = listId,
                                            item = item.copy(isBought = checked)
                                        ) { success, result ->

                                            if (!success) {
                                                // Si falla Firebase, recuperar los datos originales
                                                reloadItems()
                                                errorMessage = result ?: "No se pudo actualizar el producto"
                                            }
                                        }
                                    }
                                )

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        textDecoration = if (item.isBought) {
                                            TextDecoration.LineThrough
                                        } else {
                                            TextDecoration.None
                                        }
                                    )

                                    Text(
                                        text = "Cantidad: ${item.quantity}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Text(
                                        text = "Categoría: ${
                                            shoppingCategories.find { it.id == item.categoryId }?.name
                                                ?: "Sin categoría"
                                        }",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        editingItem = item
                                        editName = item.name
                                        editQuantity = item.quantity.toString()
                                    }
                                ) {
                                    Text("Editar")
                                }

                                TextButton(
                                    onClick = {
                                        deletingItem = item
                                    },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("← Volver a mis listas")
        }
    }

    // VENTANA PARA EDITAR PRODUCTO
    if (editingItem != null) {
        AlertDialog(
            onDismissRequest = {
                editingItem = null
            },
            title = {
                Text("Editar producto")
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nombre") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editQuantity,
                        onValueChange = { editQuantity = it },
                        label = { Text("Cantidad") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val quantity = editQuantity.toIntOrNull()

                        if (editName.isBlank()) {
                            errorMessage = "El nombre es obligatorio"
                        } else if (quantity == null || quantity <= 0) {
                            errorMessage = "La cantidad debe ser mayor que cero"
                        } else {
                            val updatedItem = editingItem!!.copy(
                                name = editName.trim(),
                                quantity = quantity
                            )

                            repository.updateItem(
                                listId = listId,
                                item = updatedItem
                            ) { success, result ->

                                if (success) {
                                    editingItem = null
                                    errorMessage = null
                                    reloadItems()
                                } else {
                                    errorMessage = result ?: "Error al editar"
                                }
                            }
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        editingItem = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // VENTANA PARA CONFIRMAR ELIMINACIÓN
    if (deletingItem != null) {
        AlertDialog(
            onDismissRequest = {
                deletingItem = null
            },
            title = {
                Text("Eliminar producto")
            },
            text = {
                Text("¿Seguro que quieres eliminar ${deletingItem!!.name}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val itemId = deletingItem!!.id

                        repository.deleteItem(
                            listId = listId,
                            itemId = itemId
                        ) { success, result ->

                            if (success) {
                                deletingItem = null
                                reloadItems()
                            } else {
                                errorMessage = result ?: "Error al eliminar"
                                deletingItem = null
                            }
                        }
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        deletingItem = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = {
                showCategoryDialog = false
                newCategoryName = ""
            },
            title = {
                Text("Crear categoría")
            },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = {
                        newCategoryName = it
                    },
                    label = {
                        Text("Nombre de la categoría")
                    },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newCategoryName.isBlank()) {
                            errorMessage = "Escribe el nombre de la categoría"
                        } else {
                            repository.addCategory(
                                listId,
                                newCategoryName
                            ) { success, result ->
                                if (success) {
                                    newCategoryName = ""
                                    showCategoryDialog = false
                                    errorMessage = null
                                    reloadCategories()
                                } else {
                                    errorMessage = result
                                        ?: "No se pudo crear la categoría"
                                }
                            }
                        }
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCategoryDialog = false
                        newCategoryName = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
    if (editingCategory != null) {
        AlertDialog(
            onDismissRequest = {
                editingCategory = null
                editCategoryName = ""
            },
            title = {
                Text("Editar categoría")
            },
            text = {
                OutlinedTextField(
                    value = editCategoryName,
                    onValueChange = {
                        editCategoryName = it
                    },
                    label = {
                        Text("Nuevo nombre")
                    },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editCategoryName.isBlank()) {
                            errorMessage = "Escribe un nombre para la categoría"
                        } else {
                            repository.updateCategory(
                                listId = listId,
                                category = editingCategory!!.copy(
                                    name = editCategoryName.trim()
                                )
                            ) { success, result ->
                                if (success) {
                                    editingCategory = null
                                    editCategoryName = ""
                                    errorMessage = null
                                    reloadCategories()
                                } else {
                                    errorMessage = result
                                        ?: "No se pudo editar la categoría"
                                }
                            }
                        }
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        editingCategory = null
                        editCategoryName = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
    if (deletingCategory != null) {
        AlertDialog(
            onDismissRequest = {
                deletingCategory = null
            },
            title = {
                Text("Eliminar categoría")
            },
            text = {
                Text(
                    "¿Seguro que quieres eliminar ${deletingCategory!!.name}?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        repository.deleteCategory(
                            listId = listId,
                            categoryId = deletingCategory!!.id
                        ) { success, result ->
                            if (success) {
                                deletingCategory = null
                                errorMessage = null
                                reloadCategories()
                            } else {
                                errorMessage = result
                                    ?: "No se pudo eliminar la categoría"
                                deletingCategory = null
                            }
                        }
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        deletingCategory = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = {
                showShareDialog = false
            },
            title = {
                Text("Compartir lista")
            },
            text = {
                Column {

                    Text("Introduce el correo electrónico:")

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = shareEmail,
                        onValueChange = {
                            shareEmail = it
                        },
                        label = {
                            Text("Correo electrónico")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Permisos:")

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = shareRole == "editor",
                            onClick = {
                                shareRole = "editor"
                            }
                        )
                        Text("Editor")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = shareRole == "viewer",
                            onClick = {
                                shareRole = "viewer"
                            }
                        )
                        Text("Lector")
                    }
                    if (shareMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = shareMessage,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isSharing,
                    onClick = {
                        val email = shareEmail.trim()

                        when {
                            currentUserId == null -> {
                                shareMessage = "Debes iniciar sesión"
                            }

                            currentUserId != ownerId -> {
                                shareMessage = "Solo el propietario puede compartir la lista"
                            }

                            email.isBlank() -> {
                                shareMessage = "Introduce un correo electrónico"
                            }

                            currentUserEmail?.equals(email, ignoreCase = true) == true -> {
                                shareMessage = "No puedes compartir la lista contigo mismo"
                            }

                            else -> {
                                isSharing = true
                                shareMessage = ""

                                repository.findUserByEmail(email) { userId, error ->

                                    if (userId == null) {
                                        isSharing = false
                                        shareMessage = error ?: "No se ha encontrado el usuario"

                                    } else if (userId == currentUserId) {
                                        isSharing = false
                                        shareMessage = "No puedes compartir la lista contigo mismo"

                                    } else {
                                        repository.addMember(
                                            listId = listId,
                                            userId = userId,
                                            email = email,
                                            role = shareRole
                                        ) { success, message ->

                                            isSharing = false

                                            if (success) {
                                                shareMessage = "¡Lista compartida correctamente!"
                                                shareEmail = ""
                                            } else {
                                                shareMessage = message ?: "Error al compartir la lista"
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Text(if (isSharing) "Compartiendo..." else "Compartir")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showShareDialog = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )

    }
}
