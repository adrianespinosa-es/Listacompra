package com.adrian.listacompra.data

data class ShoppingItem(
    val id: String = "",
    val name: String = "",
    val quantity: Int = 1,
    val isBought: Boolean = false,
    val categoryId: String = ""
)