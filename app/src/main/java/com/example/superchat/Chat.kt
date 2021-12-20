// This Class is for the conversation history display for a user

package com.example.superchat

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.get
import androidx.drawerlayout.widget.DrawerLayout
import com.example.superchat.databinding.ActivityChatBinding
import com.google.android.material.navigation.NavigationView
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.call.enqueue
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelInfo
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory
import okhttp3.internal.wait

class Chat : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle // will be initialized later

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Shared Preferences for login state
        val sp: SharedPreferences = getSharedPreferences("loggy", MODE_PRIVATE)
        val pe = sp.edit()

        //for user auth CLIENT SIDE GROSS
        val uReg: SharedPreferences = getSharedPreferences(intent.getStringExtra("uid"), MODE_PRIVATE)

        val binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val cli = ChatClient.Builder("rckbpcsvzx36", applicationContext)
            .logLevel(ChatLogLevel.ALL) // Set to NOTHING in prod
            .build()
        ChatDomain.Builder(cli, applicationContext).build()

        val client = ChatClient.instance()
        val user = User(
            id = uReg.getString("uid", "")!!,
            extraData = mutableMapOf(
                "name" to uReg.getString("nickname", "")!!,
                "password" to uReg.getString("pass", "")!!,
                "pfp" to uReg.getString("pfp", "")!!
            )
        )

        val me = intent.getStringExtra("uid")
        println("ME: $me")
        val token = intent.getStringExtra("token")!!

        user.role = "ADMIN"

        client.connectUser(
            user = user,
            token = token
        ).enqueue {/* ... */ }

        print("Connected user successfully!\n")
        println(user)


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


        // Step 4 - Connect the ChannelListViewModel to the ChannelListView, loose
        //          coupling makes it easy to customize
        viewModel.bindView(binding.channelListView, this)
        binding.channelListView.setChannelItemClickListener { channel ->
            startActivity(ChannelActivity.newIntent(this, channel))
        }



        fun inviteDialog(cid: String){
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Invite Friend")

            var friendToInvite= ""

            val input = EditText(this)
            input.setHint("Enter Friend's Email")
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            builder.setPositiveButton("Send") { dialog, which ->
                friendToInvite = input.text.toString().replace(".", "")

                println(friendToInvite + "POG")
                val channelClient = cli.channel(cid)
                channelClient.addMembers(friendToInvite).enqueue { result ->
                    if (result.isSuccess) {
                        val channel: Channel = result.data()
                    } else {
                        // Handle result.error()
                    }
                }
            }
            builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

            builder.show()
        }

        binding.channelListView.setChannelLongClickListener { channel ->
            println("long press! channel = $channel")
            inviteDialog(channel.cid)
            true
        }


        fun createDialogue(){
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("What would you like to name this chat?")

            var chanName= ""

            val input = EditText(this)
            input.setHint("Enter Chat Name")
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            builder.setPositiveButton("Create") { dialog, which ->
                chanName = input.text.toString()

                chanName = chanName.replace("\\s".toRegex(), "")

                println(chanName + "POG")

                val chanCli = cli.channel(channelType = "messaging", channelId = chanName)
                chanCli.create().enqueue { result ->
                    if (result.isSuccess) {
                        val newChannel: Channel = result.data()
                        val channyCli = cli.channel(newChannel.cid)
                        channyCli.update(extraData = mapOf("name" to chanName)).enqueue { result ->
                            if(result.isSuccess){
                                channyCli.addMembers(user.id).enqueue { /*...*/ }
                            }
                        }
                    } else {
                        println("a")
                    }
                }
            }
            builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

            builder.show()
        }



        binding.cc.setOnClickListener {
            createDialogue()
        }

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
                    intent.putExtra("uid", user.id)
                    intent.putExtra("token", token)
                    startActivity(intent)
                }
                R.id.addFriends ->
                {
                    // Go to Channel activity
                    val intent = Intent(this, AddFriends::class.java)
                    intent.putExtra("uid", user.id)
                    intent.putExtra("token", token)
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


    override fun onBackPressed() {
        print("Rip bruh\n")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}