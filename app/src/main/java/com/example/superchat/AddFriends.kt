package com.example.superchat

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.example.superchat.databinding.ActivityChatBinding
import com.google.android.material.navigation.NavigationView
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import kotlinx.android.synthetic.main.activity_add_friends.*
import android.text.Editable

import android.text.TextWatcher
import android.view.View
import androidx.core.view.iterator
import androidx.core.view.size
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class AddFriends : AppCompatActivity() {
    lateinit var toggle: ActionBarDrawerToggle // will be initialized later
    lateinit var uReg: SharedPreferences
    val ourType = object : TypeToken<List<Friend>>() {}.type //funky stuff that allows conversion between generic and our dataclass
    lateinit var uEdit: SharedPreferences.Editor
    lateinit var fList: String
    lateinit var friendList: List<Friend>


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

        searchFriends.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                searchUsers(searchFriends.text.toString(), client)
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable) {}
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun searchUsers(searchedName: String, client: ChatClient) {
        val userID = searchedName.replace(".", "")

        println(searchedName)

        var request = QueryUsersRequest(
            filter = Filters.autocomplete("name", userID),
            offset = 0,
            limit = 10,
        )

        var request1 = QueryUsersRequest(
            filter = Filters.`in`("id", listOf(userID!!)), // I had to use the '!!' after the string there, if it assumes friendToBeAdded is datatype String? and not String
            offset = 0,
            limit = 3,
        )

        val searchLayout = findViewById<LinearLayout>(R.id.searchedNames) // the display of the


        searchLayout.removeAllViews() // clears the pre-existing names that were displayed

        client.queryUsers(request).enqueue { result ->
            if (result.isSuccess) {
                val users: List<User> = result.data()
                var index = 0

                for (user in users) {
                    index++
                    val text = TextView(this)
                    text.setText(user.name)
                    text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
                    text.setOnClickListener {
                        // name was clicked... confirm if user would like to add
                        findViewById<TextView>(R.id.textView3).setVisibility(View.VISIBLE) // Send Friend Request?
                        findViewById<TextView>(R.id.textView4).setText(user.name) // Nickname display
                        findViewById<TextView>(R.id.textView4).setVisibility(View.VISIBLE)

                        findViewById<Button>(R.id.sendFriendRequest).setVisibility(View.VISIBLE) // Button to send request
                        findViewById<Button>(R.id.sendFriendRequest).setOnClickListener {
                            // TODO: update friends status
                            uReg = getSharedPreferences(user.id, MODE_PRIVATE)
                            uEdit = uReg.edit()

                            fList = uReg.getString("friends", "")!!

                            val gson = Gson()

                            try {
                                if(fList != "") {

                                    friendList = gson.fromJson(fList, ourType)
                                    // this should give us a list of friend objects, associated with the current user
                                    // we can modify each Friend object in here for best friends and whatnot
                                    // We also would use this to display friends, and we can grab a friend id and use it to create channel
                                } else {
                                    friendList = listOf()
                                }
                            } catch (e: Exception) {
                                println("Error in try... " + e)
                                friendList = listOf()
                            }


                            //val newF = Friend(user.id, user.name)
                            // fList.add(newF) ---> not allowed
                            fList += Friend(user.id, user.name)
                            var list = gson.toJson(friendList)
                            uEdit.putString("friends", list).commit()

                            //uEdit.putString("friends", fList)
                            Toast.makeText(this, "Added " + user.name, Toast.LENGTH_SHORT)
                                .show()

                            println("\n\nHERE IS MY FRIENDS" + list + "\n\n LEAVE ME BE")
                        }
                        findViewById<Button>(R.id.cancelRequest).setVisibility(View.VISIBLE) // Button to cancel
                        findViewById<Button>(R.id.cancelRequest).setOnClickListener {
                            findViewById<TextView>(R.id.textView3).setVisibility(View.INVISIBLE) // Send Friend Request?
                            findViewById<TextView>(R.id.textView4).setVisibility(View.INVISIBLE)
                            findViewById<Button>(R.id.sendFriendRequest).setVisibility(View.INVISIBLE) // Button to send request
                            findViewById<Button>(R.id.cancelRequest).setVisibility(View.INVISIBLE) // Button to cancel
                            findViewById<EditText>(R.id.searchFriends).setText("") // clear search
                            searchLayout.removeAllViews() // clear searched names
                        }
                    }

                    searchLayout ?.addView(text)
                }

            } else {
                // Handle result.error()
                println("nah this sucks\n")
            }


        }

        client.queryUsers(request1).enqueue { result1 ->
            if (result1.isSuccess) {
                val users: List<User> = result1.data()


                for (user in users) {
                    //println("I have typed - id: " + result1.data() + "\ns")

                    val text = TextView(this)
                    text.setText(user.name)
                    text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
                    searchLayout ?.addView(text)

                }


            } else {
                // Handle result.error()
                println("nah this sucks\n")
            }
        }
    }
}