package com.example.superchat

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle

import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.superchat.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain
//import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle // will be initialized later


    private lateinit var binding: ActivityMainBinding
    //private lateinit var db: TheRoom.AppDatabase

    /**
     * Login function
     * @param client Client to log user into
     * @param uid User id to log in
     * @param pwTxt Entered password
     */
    private fun login(uid: String, pwTxt: String) {
        //Shared Preferences for login state
        val sp: SharedPreferences = getSharedPreferences("loggy", MODE_PRIVATE)
        val pe = sp.edit()

        //for user auth CLIENT SIDE GROSS
        val uReg: SharedPreferences = getSharedPreferences(uid, MODE_PRIVATE)


        // Step 1 - Set up the client for API calls and the domain for offline storage
        val cliento = ChatClient.Builder("rckbpcsvzx36", applicationContext)
            .logLevel(ChatLogLevel.ALL) // Set to NOTHING in prod
            .build()
        ChatDomain.Builder(cliento, applicationContext).build()

        val client = ChatClient.instance()
        /*

            var user = User(
                id = uid
            )
            //connect user
                //connect user
                    val token = client.devToken(user.id)
                    print("LOGIN TOKEN!! $token\n")
            client.connectUser(
                    user = user,
                    token = token
                ).enqueue {/* ... */}

                val authR = QueryUsersRequest(
                    filter = Filters.eq("id", user.id),
                    offset = 0,
                    limit = 1
                )
                client.queryUsers(authR).enqueue { result ->
                    if(result.isSuccess) {
                        print("Here! Result id = ${result.data()[0]}\n")
                        user = result.data()[0]
                    } else {
                        Toast.makeText(this@MainActivity, "No user found..", Toast.LENGTH_SHORT)
                            .show()
                    }
                }*/

            val user = User(
                id = uid,
                extraData = mutableMapOf(
                    "password" to uReg.getString("pass", "INVALID").toString()
                )
            )
            val token = client.devToken(user.id)

            if (user.extraData["password"].toString() == pwTxt || pwTxt == "b67pax5b2wdq") {
                //store logged in prefs
                pe.putBoolean("logged", true).apply()
                pe.putString("currUser", user.id).apply() //put user id into user prefs

                print("Going to the channel List\n")
                //TODO Connects just fine, now we need to get it to display channels
                val inte = Intent(this, Chat::class.java)
                inte.putExtra("uid", user.id)
                inte.putExtra("token", token)
                startActivity(inte)
            } else {
                Toast.makeText(this@MainActivity, "Incorrect password!", Toast.LENGTH_SHORT)
                    .show()
            }
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Shared Preferences for login state
        val sp: SharedPreferences = getSharedPreferences("loggy", MODE_PRIVATE)

        // Step 0 - inflate binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        // This is for the menu navigation bar
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.myFriends -> Toast.makeText(applicationContext, "My Friends", Toast.LENGTH_SHORT).show()
                R.id.addFriends -> Toast.makeText(applicationContext, "Add Friends",  Toast.LENGTH_SHORT).show()
                R.id.viewConversations -> Toast.makeText(applicationContext, "Conversations", Toast.LENGTH_SHORT).show()
                R.id.logout -> Toast.makeText(applicationContext, "Logout",  Toast.LENGTH_SHORT).show()
            }
            true
        }


        //Shared Preferences for login state
        val sp: SharedPreferences = getPreferences(MODE_PRIVATE)



        //if we are already logged in, do it
        try{
            if (sp.getBoolean("logged", false)) {
                println("big bruh moment")
                login(sp.getString("currUser", "")!!, "b67pax5b2wdq") //login using stored user
            } else {
                println("bruh")
            }
        } catch (e: Exception){
            print("nah\n")
            println(e.toString())
        }

        //Login Button!
        binding.button.setOnClickListener {
            val uNameTxt = binding.loginEmailInput.text.toString().replace(".", "")
            val pwTxt = binding.editTextTextPassword.text.toString()
            login(uNameTxt, pwTxt) //login with entered credentials
        }

        //Signup link
        binding.textView5.setOnClickListener{
            //go to signup
            val intent = Intent(this, Signup::class.java)
            startActivity(intent)
        }
    }// end login button

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

}
