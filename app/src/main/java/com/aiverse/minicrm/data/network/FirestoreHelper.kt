package com.aiverse.minicrm.data.network

import com.aiverse.minicrm.data.model.CustomerEntity
import com.aiverse.minicrm.data.model.OrderEntity
import com.google.firebase.firestore.FirebaseFirestore
object FirestoreHelper {
    private val firestore = FirebaseFirestore.getInstance()

    fun saveCustomer(customer: CustomerEntity) {
        firestore.collection("customers")
            .document(customer.id.toString())
            .set(customer)
            .addOnSuccessListener { /* optional: log success */ }
            .addOnFailureListener { e -> e.printStackTrace() }
    }

    fun saveOrder(order: OrderEntity) {
        firestore.collection("orders")
            .document(order.id.toString())
            .set(order)
            .addOnSuccessListener { /* optional: log success */ }
            .addOnFailureListener { e -> e.printStackTrace() }
    }


    fun listenCustomersRealtime(onChange: (CustomerEntity) -> Unit) {
        firestore.collection("customers")
            .addSnapshotListener { snapshot, _ ->
                snapshot?.documentChanges?.forEach { change ->
                    val customer = change.document.toObject(CustomerEntity::class.java)
                    onChange(customer)
                }
            }
    }

    fun listenOrdersRealtime(onChange: (OrderEntity) -> Unit) {
        firestore.collection("orders")
            .addSnapshotListener { snapshot, _ ->
                snapshot?.documentChanges?.forEach { change ->
                    val order = change.document.toObject(OrderEntity::class.java)
                    onChange(order)
                }
            }
    }
}
