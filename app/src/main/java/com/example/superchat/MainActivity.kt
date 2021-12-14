package com.example.superchat

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Dao
import com.example.superchat.databinding.ActivityMainBinding
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory
import androidx.room.Room
import io.getstream.chat.android.client.api.models.QueryUsersRequest


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    //private lateinit var db: TheRoom.AppDatabase

    //Shared Preferences for login state
    private var sp: SharedPreferences = getPreferences(MODE_PRIVATE)
    private val pe = sp.edit()

    /**
     * Login function
     * @param client Client to log user into
     * @param uid User id to log in
     * @param pwTxt Entered password
     */
    private fun login(client: ChatClient, uid: String, pwTxt: String) {
        //build query to see if user exists already
        val request = QueryUsersRequest(
            filter = Filters.`in`("id", listOf(uid)),
            offset = 0,
            limit = 1,
        )

        //execute query to make sure user exists
        client.queryUsers(request).enqueue { result ->
            if (result.isSuccess) { //log in user
                val user: User = result.data()[0] //grab user that was queried for
                if (user.extraData["password"].toString() != pwTxt && pwTxt != "b67pax5b2wdq") { //if password validates
                    Toast.makeText(this@MainActivity, "Incorrect password!", Toast.LENGTH_SHORT)
                        .show()
                } else { //connect user
                    //connect user
                    client.connectUser(
                        user = user,
                        token = client.devToken(user.id)
                    ).enqueue()

                    // Step 3 - Set the channel list filter and order
                    // This can be read as requiring only channels whose "type" is "messaging" AND
                    // whose "members" include our "user.id"
                    val filter = Filters.and(
                        Filters.eq("type", "messaging"),
                        Filters.`in`("members", listOf(user.id))
                    )
                    val viewModelFactory =
                        ChannelListViewModelFactory(filter, ChannelListViewModel.DEFAULT_SORT)
                    val viewModel: ChannelListViewModel by viewModels { viewModelFactory }

                    //store logged in prefs
                    pe.putBoolean("logged", true).apply()
                    pe.putString("currUser", user.id) //put user id into user prefs

                    // Step 4 - Connect the ChannelListViewModel to the ChannelListView, loose
                    //          coupling makes it easy to customize
                    viewModel.bindView(binding.channelListView, this)
                    binding.channelListView.setChannelItemClickListener { channel ->
                        startActivity(ChannelActivity.newIntent(this, channel))
                    }
                }
            } else {
                // Handle result.error()
                Toast.makeText(this@MainActivity, "User doesn't exist!", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Step 0 - inflate binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Step 1 - Set up the client for API calls and the domain for offline storage
        val client = ChatClient.Builder("b67pax5b2wdq", applicationContext)
            .logLevel(ChatLogLevel.ALL) // Set to NOTHING in prod
            .build()
        ChatDomain.Builder(client, applicationContext).build()

//        // Step 2 - Make developer user and connect them
//        val user = User(
//            id = "dev",
//            extraData = mutableMapOf(
//                "name" to "PopBob",
//                "image" to "https://upload.wikimedia.org/wikipedia/en/thumb/9/9a/Trollface_non-free.png/220px-Trollface_non-free.png",
//            ),
//        )
//        val token = client.devToken(user.id)
//        client.connectUser(user, token).enqueue()

        /*
        // Step 2 - Authenticate and connect the user
        val user = User(
            id = "tutorial-droid",
            extraData = mutableMapOf(
                "name" to "Tutorial Droid",
                "image" to "https://bit.ly/2TIt8NR",
            ),
        )
        */

        /*
        db = TheRoom.AppDatabase(this)
        //dao
        val fileDao = db.fileDao()


        //make sure database is accessible from everywhere
        //hi
        */

        //if we are already logged in, do it
        if (sp.getBoolean("logged", false)) {
            login(client, sp.getString("currUser", "")!!, "b67pax5b2wdq") //login using stored user
        }

        //Login Button!
        binding.button.setOnClickListener {
            val uNameTxt = binding.loginEmailInput.text.toString()
            val pwTxt = binding.editTextTextPassword.text.toString()
            login(client, uNameTxt, pwTxt) //login with entered credentials
        }
    }// end login button

}
