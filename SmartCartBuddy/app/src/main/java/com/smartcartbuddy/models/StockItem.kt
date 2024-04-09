package com.smartcartbuddy.models

class StockItem {
    var productId = 0
    var productName: String? = null
    var price = 0.0
    var category: String? = null

    constructor() {}
    constructor(productId: Int, productName: String?, price: Double, category: String?) {
        this.productId = productId
        this.productName = productName
        this.price = price
        this.category = category
    }


}