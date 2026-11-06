package com.adrian.listacompra.data

data class ShoppingList(
    val id: String = "",
    val name: String = "",
    val ownerId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)