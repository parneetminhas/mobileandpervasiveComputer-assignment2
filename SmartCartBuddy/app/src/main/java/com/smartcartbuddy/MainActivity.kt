package com.smartcartbuddy

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.smartcartbuddy.adapters.StockAdapter
 import com.smartcartbuddy.models.StockItem


class MainActivity : AppCompatActivity() {
    private lateinit var adapter: RecyclerView.Adapter<*>
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewCartButton: Button
     private lateinit var mDatabase: DatabaseReference
    private var stockItemList: MutableList<StockItem> = ArrayList()
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         setContentView(R.layout.activity_main)

        stockItemList = ArrayList()

        initializeFireBaseDB()

        val toolbar = findViewById<Toolbar>(R.id.homeToolbar)
        setSupportActionBar(toolbar)

        recyclerView = findViewById(R.id.stockRecyclerView)
        viewCartButton = findViewById(R.id.viewCartButton)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.back)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = StockAdapter(this, stockItemList)
        recyclerView.adapter = adapter

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Fetching items...")
        progressDialog.setCancelable(false)

        readDataIntoList()

        viewCartButton.setOnClickListener { viewCart() }
    }

    private fun readDataIntoList() {
        progressDialog.show()
        mDatabase.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                stockItemList.clear()

                for (childSnapshot in dataSnapshot.children) {
                    val stockItem = childSnapshot.getValue(StockItem::class.java)
                    stockItemList.add(stockItem!!)
                }
                adapter.notifyDataSetChanged()
                progressDialog.dismiss()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                progressDialog.dismiss()
                Log.e("MainActivity", "Error reading data from Firebase: ${databaseError.message}")
            }
        })
    }

    private fun initializeFireBaseDB() {
        mDatabase = FirebaseDatabase.getInstance().getReference("stockItems")
        val stockItems = listOf(
            StockItem(0, "Tomatoes", 20.0, "Vegetable"),
            StockItem(1, "Broccoli", 10.0, "Vegetable"),
            StockItem(2, "Lettuce", 5.00, "Vegetable"),
            StockItem(3, "Capsicum", 30.0, "Vegetable"),
            StockItem(4, "Cucumber", 100.00, "Vegetable")
        )

        mDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Push each stock item to Firebase if the node doesn't exist
                    for (item in stockItems) {
                        val itemMap: MutableMap<String, Any> = HashMap()
                        itemMap["productId"] = item.productId
                        itemMap["productName"] = item.productName!!
                        itemMap["price"] = item.price
                        addStockItems(itemMap)
                    }
                } else {
                    Log.d("MainActivity", "Data already exists in Firebase.")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("MainActivity", "Error reading data from Firebase: ${databaseError.message}")
            }
        })
    }

    private fun addStockItems(itemMap: Map<String, Any>) {
        mDatabase.push().setValue(itemMap) { databaseError, _ ->
            if (databaseError != null) {
                Log.e("MainActivity", "Data could not be saved: ${databaseError.message}")
            } else {
                Log.d("MainActivity", "Data saved successfully.")
            }
        }
    }
    fun loadUserCart(userId: String?, itemMap: HashMap<String, Any>) {
        val cartRef = FirebaseDatabase.getInstance().reference.child("carts").child(
            userId!!
        )
        val productId = itemMap["productId"].toString()
        val itemRef = cartRef.child(productId)
        itemRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("err", "Item already exists in user's cart")
                    val currentQuantity = dataSnapshot.child("quantity").getValue(
                        Int::class.java
                    )!!
                    val newQuantity = currentQuantity + itemMap["quantity"].toString().toInt()
                    itemRef.child("quantity").setValue(newQuantity)
                    Toast.makeText(
                        this@MainActivity, "Item Updated.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Log.d("err", "Adding new item to user's cart")
                    itemRef.setValue(itemMap)
                        .addOnSuccessListener { aVoid: Void? ->
                            Toast.makeText(
                                this@MainActivity, "Added to cart.",
                                Toast.LENGTH_LONG
                            ).show()
                            Log.d("err", "Item added to user's cart successfully")
                        }
                        .addOnFailureListener { e: Exception? ->
                            Toast.makeText(
                                this@MainActivity, "Error adding to cart.",
                                Toast.LENGTH_LONG
                            ).show()
                            Log.e("err", "Failed to add item to user's cart", e)
                        }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("err", "Database error: " + databaseError.message)
            }
        })
    }

    private fun viewCart() {
        startActivity(Intent(this@MainActivity, CartActivity::class.java))
    }
}
