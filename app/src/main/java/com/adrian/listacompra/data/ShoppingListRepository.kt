
package com.adrian.listacompra.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class ShoppingListRepository {

    private val db = FirebaseFirestore.getInstance()

    private val listsCollection = db.collection("lists")

    // CREAR LISTA
    fun createList(
        name: String,
        ownerId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val newList = ShoppingList(
            name = name,
            ownerId = ownerId
        )

        listsCollection
            .add(newList)
            .addOnSuccessListener { document ->
                onResult(true, document.id)
            }
            .addOnFailureListener { exception ->
                onResult(false, exception.localizedMessage)
            }
    }

    // OBTENER MIS LISTAS
    fun getMyLists(
        userId: String,
        onResult: (List<ShoppingList>) -> Unit
    ) {
        listsCollection
            .whereEqualTo("ownerId", userId)
            .get()
            .addOnSuccessListener { result ->
                val lists = result.documents.mapNotNull { document ->
                    document.toObject(ShoppingList::class.java)?.copy(
                        id = document.id
                    )
                }
                onResult(lists)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    // COMPARTIR LISTA
    fun addMember(
        listId: String,
        userId: String,
        email: String,
        role: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val member = ListMember(
            userId = userId,
            email = email,
            role = role
        )

        val memberRef = listsCollection
            .document(listId)
            .collection("members")
            .document(userId)

        val accessRef = db.collection("users")
            .document(userId)
            .collection("listAccess")
            .document(listId)

        val accessData = hashMapOf(
            "listId" to listId,
            "role" to role
        )

        db.runBatch { batch ->
            batch.set(memberRef, member)
            batch.set(accessRef, accessData)
        }
            .addOnSuccessListener {
                onResult(true, "Lista compartida correctamente")
            }
            .addOnFailureListener { exception ->
                onResult(false, exception.localizedMessage)
            }
    }

    // OBTENER TODAS MIS LISTAS (PROPIAS Y COMPARTIDAS)
    fun getAllMyLists(
        userId: String,
        onResult: (List<ShoppingList>) -> Unit
    ) {
        getMyLists(userId) { myLists ->

            db.collection("users")
                .document(userId)
                .collection("listAccess")
                .get()
                .addOnSuccessListener { result ->

                    val sharedIds = result.documents.mapNotNull {
                        it.getString("listId")
                    }.distinct()

                    if (sharedIds.isEmpty()) {
                        onResult(myLists)
                        return@addOnSuccessListener
                    }

                    val sharedLists = mutableListOf<ShoppingList>()
                    val chunks = sharedIds.chunked(10)
                    var completed = 0

                    chunks.forEach { ids ->

                        listsCollection
                            .whereIn(
                                com.google.firebase.firestore.FieldPath.documentId(),
                                ids
                            )
                            .get()
                            .addOnSuccessListener { documents ->

                                val lists = documents.documents.mapNotNull {
                                    it.toObject(ShoppingList::class.java)?.copy(
                                        id = it.id
                                    )
                                }

                                sharedLists.addAll(lists)
                                completed++

                                if (completed == chunks.size) {
                                    onResult(
                                        (myLists + sharedLists).distinctBy { it.id }
                                    )
                                }
                            }
                            .addOnFailureListener {

                                completed++

                                if (completed == chunks.size) {
                                    onResult(
                                        (myLists + sharedLists).distinctBy { it.id }
                                    )
                                }
                            }
                    }
                }
                .addOnFailureListener {
                    onResult(myLists)
                }
        }
    }

    // AÑADIR PRODUCTO
    fun addItem(
        listId: String,
        name: String,
        quantity: Int,
        categoryId: String = "",
        onResult: (Boolean, String?) -> Unit
    ) {
        val itemName = name.trim()

        if (itemName.isEmpty()) {
            onResult(false, "El nombre del producto es obligatorio")
            return
        }

        if (quantity <= 0) {
            onResult(false, "La cantidad debe ser mayor que cero")
            return
        }

        val newItem = ShoppingItem(
            name = itemName,
            quantity = quantity,
            categoryId = categoryId
        )

        listsCollection
            .document(listId)
            .collection("items")
            .add(newItem)
            .addOnSuccessListener {
                onResult(true, "Producto añadido correctamente")
            }
            .addOnFailureListener { exception ->
                onResult(false, exception.localizedMessage)
            }
    }

    // OBTENER PRODUCTOS
    fun getItems(
        listId: String,
        onResult: (List<ShoppingItem>) -> Unit
    ) {
        listsCollection
            .document(listId)
            .collection("items")
            .addSnapshotListener { result, error ->

                if (error != null || result == null) {
                    onResult(emptyList())
                    return@addSnapshotListener
                }

                val items = result.documents.mapNotNull { document ->
                    document.toObject(ShoppingItem::class.java)?.copy(
                        id = document.id
                    )
                }

                onResult(items)
            }
    }

    // EDITAR PRODUCTO
    fun updateItem(
        listId: String,
        item: ShoppingItem,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (item.name.trim().isEmpty()) {
            onResult(false, "El nombre del producto es obligatorio")
            return
        }

        if (item.quantity <= 0) {
            onResult(false, "La cantidad debe ser mayor que cero")
            return
        }

        listsCollection
            .document(listId)
            .collection("items")
            .document(item.id)
            .update(
                mapOf(
                    "name" to item.name.trim(),
                    "quantity" to item.quantity,
                    "categoryId" to item.categoryId,
                    "isBought" to item.isBought
                )
            )
            .addOnSuccessListener {
                onResult(true, "Producto actualizado correctamente")
            }
            .addOnFailureListener { exception ->
                onResult(false, exception.localizedMessage)
            }
    }

    // ELIMINAR PRODUCTO
    fun deleteItem(
        listId: String,
        itemId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        listsCollection
            .document(listId)
            .collection("items")
            .document(itemId)
            .delete()
            .addOnSuccessListener {
                onResult(true, "Producto eliminado correctamente")
            }
            .addOnFailureListener { exception ->
                onResult(false, exception.localizedMessage)
            }
    }

    // AÑADIR CATEGORÍA
    fun addCategory(
        listId: String,
        name: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val categoryName = name.trim()

        if (categoryName.isEmpty()) {
            onResult(false, "El nombre de la categoría es obligatorio")
            return
        }

        val category = ShoppingCategory(
            name = categoryName
        )

        listsCollection
            .document(listId)
            .collection("categories")
            .add(category)
            .addOnSuccessListener {
                onResult(true, "Categoría creada correctamente")
            }
            .addOnFailureListener { exception ->
                onResult(false, exception.localizedMessage)
            }
    }

    // OBTENER CATEGORÍAS
    fun getCategories(
        listId: String,
        onResult: (List<ShoppingCategory>) -> Unit
    ) {
        listsCollection
            .document(listId)
            .collection("categories")
            .get()
            .addOnSuccessListener { result ->

                val categories = result.documents.mapNotNull { document ->
                    document.toObject(ShoppingCategory::class.java)?.copy(
                        id = document.id
                    )
                }

                onResult(categories)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
    // EDITAR CATEGORÍA
    fun updateCategory(
        listId: String,
        category: ShoppingCategory,
        onResult: (Boolean, String?) -> Unit
    ) {
        val categoryName = category.name.trim()

        if (categoryName.isEmpty()) {
            onResult(false, "El nombre de la categoría es obligatorio")
            return
        }

        listsCollection
            .document(listId)
            .collection("categories")
            .document(category.id)
            .update("name", categoryName)
            .addOnSuccessListener {
                onResult(true, "Categoría actualizada correctamente")
            }
            .addOnFailureListener { exception ->
                onResult(false, exception.localizedMessage)
            }
    }
    // ELIMINAR CATEGORÍA
    fun deleteCategory(
        listId: String,
        categoryId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        listsCollection
            .document(listId)
            .collection("categories")
            .document(categoryId)
            .delete()
            .addOnSuccessListener {
                onResult(true, "Categoría eliminada correctamente")
            }
            .addOnFailureListener { exception ->
                onResult(false, exception.localizedMessage)
            }
    }
    fun findUserByEmail(
        email: String,
        onResult: (String?, String?) -> Unit
    ) {
        val normalizedEmail = email.trim().lowercase()

        Log.d("SHARE_DEBUG", "Buscando correo: $normalizedEmail")

        db.collection("users")
            .whereEqualTo("email", normalizedEmail)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->

                Log.d("SHARE_DEBUG", "Documentos encontrados: ${result.size()}")

                if (result.isEmpty) {
                    onResult(null, "No existe ningún usuario con ese correo")
                } else {
                    val userId = result.documents.first().getString("uid")

                    if (userId == null) {
                        onResult(null, "El usuario no tiene un UID guardado")
                    } else {
                        onResult(userId, null)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("SHARE_DEBUG", "Error buscando usuario", exception)
                onResult(null, exception.localizedMessage)
            }
    }
}
