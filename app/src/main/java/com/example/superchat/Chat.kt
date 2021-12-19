package com.example.superchat

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.get
import com.example.superchat.databinding.ActivityChatBinding
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.call.enqueue
import io.getstream.chat.android.client.logger.ChatLogLevel
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

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


        client.connectUser(
            user = user,
            token = token
        ).enqueue {/* ... */ }

        print("Connected user successfully!\n")
        println(user)

        user.role = "ADMIN"

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

        binding.channelListView.setChannelLongClickListener { channel ->
            println("long press! channel = $channel")
            true
        }



        binding.cc.setOnClickListener {
            client.createChannel(
                channelType = "messaging",
                members = listOf("a@acom", user.id)
            ).enqueue { result ->
                if (result.isSuccess) {
                    val channel = result.data()
                    startActivity(ChannelActivity.newIntent(this, channel))
                } else {
                    Toast.makeText(this@Chat, "Could not make the channel!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        binding.logout.setOnClickListener {
            client.disconnect()

            //store logged in prefs
            pe.putBoolean("logged", false).apply()
            pe.putString("currUser", "").apply() //put user id into user prefs
            //go back to login
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }


    override fun onBackPressed() {
        print("Rip bruh\n")
    }
}