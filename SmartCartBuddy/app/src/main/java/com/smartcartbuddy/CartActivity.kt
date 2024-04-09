package com.smartcartbuddy

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.smartcartbuddy.adapters.CartAdapter
import com.smartcartbuddy.models.CartItem
import java.util.*

class CartActivity : AppCompatActivity(), CartAdapter.GrandTotalListener {
    private lateinit var adapter: RecyclerView.Adapter<*>
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var itemsTotal: TextView
    private lateinit var addedToCart: MutableList<CartItem>
    private var progressDialog: ProgressDialog? = null
    private var mDatabase: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        addedToCart = ArrayList()
        initializeFirebaseDB()
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        recyclerView = findViewById(R.id.cartRecyclerView)
        emptyView = findViewById(R.id.empty_view)
        itemsTotal = findViewById(R.id.items_total)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.back)
        progressDialog = ProgressDialog(this)
        progressDialog!!.setMessage("Fetching cart items...")
        progressDialog!!.setCancelable(false)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CartAdapter(this@CartActivity, addedToCart, this)
        recyclerView.adapter = adapter
        readDataIntoList()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initializeFirebaseDB() {
        val common = Common(applicationContext)
        val userId = common.userId
        userId?.let {
            mDatabase = FirebaseDatabase.getInstance().getReference("carts").child(userId)
        } ?: run {
            Log.e("Error", "User ID is null")
        }
    }

    private fun readDataIntoList() {
        progressDialog?.show()
        mDatabase?.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                addedToCart.clear()
                for (childSnapshot in dataSnapshot.children) {
                    val cartItem = childSnapshot.getValue(CartItem::class.java)
                    cartItem?.let {
                        addedToCart.add(it)
                    }
                }
                adapter.notifyDataSetChanged()
                updateEmptyViewVisibility()
                progressDialog?.dismiss()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                progressDialog?.dismiss()
                Log.e("Firebase", "Error reading data from Firebase: ${databaseError.message}")
            }
        })
    }

    private fun updateEmptyViewVisibility() {
        if (addedToCart.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    fun updateQuantityInFirebase(userId: String, currentItem: CartItem) {
        val cartRef = FirebaseDatabase.getInstance().getReference("carts").child(userId)
        val itemRef = cartRef.child(currentItem.productId.toString())
        itemRef.child("quantity").setValue(currentItem.quantity)
            .addOnSuccessListener {
                Log.d("Firebase", "Quantity updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed to update quantity", e)
            }
    }

    fun removeItemFromFirebase(userId: String, currentItem: CartItem) {
        val cartRef = FirebaseDatabase.getInstance().getReference("carts").child(userId)
        val itemRef = cartRef.child(currentItem.productId.toString())
        itemRef.removeValue()
            .addOnSuccessListener {
                Log.d("Firebase", "Item removed successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed to remove item", e)
            }
    }

    override fun onGrandTotalChanged(grandTotal: Double) {
        itemsTotal.text = String.format(Locale.getDefault(), "$%.2f", grandTotal)
    }
}
