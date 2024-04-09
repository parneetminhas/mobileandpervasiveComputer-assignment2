package com.smartcartbuddy.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.smartcartbuddy.Common
import com.smartcartbuddy.MainActivity
import com.smartcartbuddy.R
import com.smartcartbuddy.models.CartItem
import com.smartcartbuddy.models.StockItem

class StockAdapter(private val context: Context, private val stockItems: List<StockItem>) :
    RecyclerView.Adapter<StockAdapter.StockViewHolder>() {

    inner class StockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.item_name)
        val itemPrice: TextView = itemView.findViewById(R.id.item_price)
        val addButton: CardView = itemView.findViewById(R.id.add_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.stock_item_layout, parent, false)
        return StockViewHolder(view)
    }

    @SuppressLint("DefaultLocale")
    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val currentItem = CartItem(
            stockItems[position].productId,
            stockItems[position].productName,
            1,
            stockItems[position].price,
            ""
        )
        holder.itemName.text = currentItem.productName
        holder.itemPrice.text = String.format("$%.2f", currentItem.price)
        holder.addButton.setOnClickListener { addToCart(currentItem) }
    }

    override fun getItemCount(): Int {
        return stockItems.size
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addToCart(cartItem: CartItem) {
        val itemMap = hashMapOf<String, Any>(
            "productId" to cartItem.productId,
            "productName" to cartItem.productName!!,
            "price" to cartItem.price,
            "quantity" to cartItem.quantity
        )
        Log.d("err", "xxx")
        context.let { ctx ->
            val common = Common(ctx.applicationContext)
            val userId = common.userId
            Log.d("err", "User ID: $userId")
            if (ctx is MainActivity) {
                ctx.loadUserCart(userId, itemMap)
                Log.d("err", "loadUserCart() called from MainActivity")
            } else {
                Log.e("err", "Context is not an instance of MainActivity")
            }
            notifyDataSetChanged()
        } ?: Log.e("err", "Context is null")
    }
}
