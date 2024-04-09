package com.smartcartbuddy.models

class CartItem {
    var productId = 0
    var productName: String? = null
    var quantity = 0
    var price = 0.0
    var category: String? = null

    constructor() {}

    constructor(
        productId: Int,
        productName: String?,
        quantity: Int,
        price: Double,
        category: String?
    ) {
        this.productId = productId
        this.productName = productName
        this.quantity = quantity
        this.price = price
        this.category = category
    }
}