package com.aiverse.minicrm.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aiverse.minicrm.ui.customer.CustomerListActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if a user is already logged in via Firebase
        if (FirebaseAuth.getInstance().currentUser != null) {
            // User logged in → go to Customer List
            startActivity(Intent(this, CustomerListActivity::class.java))
        } else {
            // User not logged in → go to Login screen
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Close SplashActivity so it cannot be navigated back to
        finish()
    }
}
