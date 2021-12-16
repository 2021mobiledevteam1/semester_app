package com.example.superchat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.example.superchat.databinding.ActivityChatBinding
import com.example.superchat.databinding.ActivitySignupBinding
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import androidx.activity.viewModels
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory

class Chat : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)


        val binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val cli = ChatClient.Builder("rckbpcsvzx36", applicationContext)
            .logLevel(ChatLogLevel.ALL) // Set to NOTHING in prod
            .build()
        ChatDomain.Builder(cli, applicationContext).build()

        val client = ChatClient.instance()

        val user = User(
            id = intent.getStringExtra("uid")!!
        )

        val token = intent.getStringExtra("token")!!

        client.connectUser(
            user = user,
            token = token
        ).enqueue {/* ... */}

        print("Connected user successfully!\n")

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

        binding.cc.setOnClickListener{
            client.createChannel(
                channelType = "messaging",
                members = listOf("a@acom", user.id)
            ).enqueue { result ->
                if (result.isSuccess) {
                    val channel = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }

        binding.logout.setOnClickListener{
            client.disconnect()
            //go back to login
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        print("Rip bruh\n")
    }
}