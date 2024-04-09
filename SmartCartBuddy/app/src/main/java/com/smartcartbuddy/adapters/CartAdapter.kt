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
import com.smartcartbuddy.CartActivity
import com.smartcartbuddy.Common
import com.smartcartbuddy.R
import com.smartcartbuddy.models.CartItem

class CartAdapter(
    private val context: Context?,
    private var shoppingCart: MutableList<CartItem>,
    private val listener: GrandTotalListener
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.item_name)
        val itemQuantity: TextView = itemView.findViewById(R.id.item_quantity)
        val itemPrice: TextView = itemView.findViewById(R.id.item_price)
        val itemsTotal: TextView = itemView.findViewById(R.id.items_total)
        val removeButton: CardView = itemView.findViewById(R.id.remove_button)
        val updateButton: CardView = itemView.findViewById(R.id.update_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cart_item_layout, parent, false)
        return CartViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val currentItem = shoppingCart[position]

        holder.itemName.text = currentItem.productName
        holder.itemQuantity.text = "Quantity: ${currentItem.quantity}"
        holder.itemPrice.text = String.format("$%.2f", currentItem.price)
        holder.itemsTotal.text = String.format("$%.2f", currentItem.price * currentItem.quantity)

        holder.removeButton.setOnClickListener {
            val currentQuantity = currentItem.quantity
            if (currentQuantity > 1) {
                currentItem.quantity = currentQuantity - 1
                updateQuantity(currentItem)
                notifyItemChanged(position)
            } else {
                shoppingCart.removeAt(position)
                removeItem(currentItem)
                notifyItemRemoved(position)
            }
        }

        holder.updateButton.setOnClickListener {
            val newQuantity = currentItem.quantity + 1
            currentItem.quantity = newQuantity
            updateQuantity(currentItem)
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int {
        return shoppingCart.size
    }

    private fun calculateGrandTotal(): Double {
        var grandTotal = 0.0
        for (item in shoppingCart) {
            grandTotal += item.price * item.quantity
        }
        return grandTotal
    }

    private fun updateGrandTotal() {
        val grandTotal = calculateGrandTotal()
        listener.onGrandTotalChanged(grandTotal)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateQuantity(currentItem: CartItem) {
        context?.let { ctx ->
            val common = Common(ctx.applicationContext)
            val userId = common.userId
            Log.d("err", "User ID: $userId")
            if (ctx is CartActivity) {
                ctx.updateQuantityInFirebase(userId!!, currentItem)
                Log.d("err", "updateQuantityInFirebase() called from CartActivity")
            } else {
                Log.e("err", "Context is not an instance of CartActivity")
            }

            notifyDataSetChanged()
            updateGrandTotal()
        } ?: Log.e("err", "Context is null")
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun removeItem(currentItem: CartItem) {
        context?.let { ctx ->
            val common = Common(ctx.applicationContext)
            val userId = common.userId
            Log.d("err", "User ID: $userId")
            if (ctx is CartActivity) {
                ctx.removeItemFromFirebase(userId!!, currentItem)
                Log.d("err", "removeItemFromFirebase() called from CartActivity")
            } else {
                Log.e("err", "Context is not an instance of CartActivity")
            }

            notifyDataSetChanged()
            updateGrandTotal()
        } ?: Log.e("err", "Context is null")
    }

    interface GrandTotalListener {
        fun onGrandTotalChanged(grandTotal: Double)
    }
}
