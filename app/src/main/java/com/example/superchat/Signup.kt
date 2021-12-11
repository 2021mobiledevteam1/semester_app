package com.example.superchat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.superchat.databinding.ActivitySignupBinding
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain
import com.example.superchat.MainActivity as m
import android.content.SharedPreferences
import com.example.superchat.Friend




class Signup : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    private lateinit var userInfo: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Step 1 - Set up the client for API calls and the domain for offline storage
        val client = ChatClient.Builder("vnnjqybjbun8", applicationContext)
            .logLevel(ChatLogLevel.ALL) // Set to NOTHING in prod
            .build()
        val staticClientRef = ChatClient.instance()

        //bind button
        binding.signupButton.setOnClickListener {
            //TODO: Test to make sure all values are valid. Passwords must match and no fields should be empty. We can possibly use Android XML to make things required, but password matching might need to be here

            //create user!
            userInfo = User(
                id = binding.signupEmailAddress.text.toString(), //temporary? we might wanna use the email as ID or otherwise we can set the email as "name" and programmatically generate ids or smth
                extraData = mutableMapOf(
                    "name" to binding.signupEmailAddress.text.toString(), //unique email username
                    "nickname" to binding.editTextTextPersonName.text.toString(), //nickname can be non-unique. It doesn't matter what your nickname is!
                    "password" to binding.editTextTextPassword2.text.toString(), //password may need hashing of some sort
                    "friends" to ArrayList<Friend>(), //empty arraylist to store references to friend IDs! Stores a friend object
                    "pfp" to "" //Store b64 string for downscaled profile picture
                )
            )

            //dev token so we dont need an endpoint
            val token = staticClientRef.devToken(userInfo.id)

            //unless.. Maybe create token for this user unless we decide against it but-

            //TODO: Check to see if this user already exists based on id

            // create user
            staticClientRef.connectUser(userInfo, token).enqueue { /* ... */ }
            print("HERE!!!\n")
            print("Token: $token\n")
            staticClientRef.disconnect() //logout right away

            //go back to login
            val intent = Intent(this, m::class.java)
            startActivity(intent)
        } //end button binding
    }
}