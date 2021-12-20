package com.example.superchat

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.example.superchat.databinding.ActivityChatBinding
import com.google.android.material.navigation.NavigationView
import io.getstream.chat.android.client.ChatClient

class AddFriends : AppCompatActivity() {
    lateinit var toggle: ActionBarDrawerToggle // will be initialized later

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friends)

        //Shared Preferences for login state
        val sp: SharedPreferences = getSharedPreferences("loggy", MODE_PRIVATE)
        val pe = sp.edit()

        val binding = ActivityChatBinding.inflate(layoutInflater)
        val client = ChatClient.instance()

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        // This is for the menu navigation bar
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.myFriends ->
                {
                    // Go to Manage Friends activity
                    val intent = Intent(this, ManageFriends::class.java)
                    startActivity(intent)
                }
                R.id.addFriends ->
                {
                    // Go to Channel activity
                    val intent = Intent(this, AddFriends::class.java)
                    startActivity(intent)
                }
                R.id.viewConversations ->
                {
                    // Go to Channel activity
                    val intent = Intent(this, Chat::class.java)
                    startActivity(intent)
                }
                R.id.logout ->
                {
                    client.disconnect()

                    //store logged in prefs
                    pe.putBoolean("logged", false).apply()
                    pe.putString("currUser", "").apply() //put user id into user prefs
                    //go back to login
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}