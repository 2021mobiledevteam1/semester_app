package com.example.superchat

import android.content.Context
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

    public fun saveArray(array: ArrayList<String>, arrayName: String, mContext: Context, sPrefName: String): Boolean {
        val prefs: SharedPreferences = mContext.getSharedPreferences(sPrefName, MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putInt(arrayName + "_size", array.size)
        for (i in array.indices) editor.putString(arrayName + "_" + i, array[i])
        return editor.commit()
    }

    public fun loadArray(arrayName: String, mContext: Context, sPrefName: String): ArrayList<String?> {
        val prefs = mContext.getSharedPreferences(sPrefName, 0)
        val size = prefs.getInt(arrayName + "_size", 0)
        val array = arrayOfNulls<String>(size)
        for (i in 0 until size) array[i] = prefs.getString(arrayName + "_" + i, null)
        return array.toCollection(ArrayList())
    }

    private lateinit var binding: ActivitySignupBinding

    private lateinit var userInfo: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Step 1 - Set up the client
        val client = ChatClient.Builder("rckbpcsvzx36", applicationContext)
            .logLevel(ChatLogLevel.ALL) // Set to NOTHING in prod
            .build()
        ChatDomain.Builder(client, applicationContext).build()

        val cli = ChatClient.instance()

        //bind button
        binding.signupButton.setOnClickListener {
            //TODO: Test to make sure all values are valid. Passwords must match and no fields should be empty. We can possibly use Android XML to make things required, but password matching might need to be here
            val idInp = binding.signupEmailAddress.text.toString().replace(".", "")
            val nameInp = binding.editTextTextPersonName.text.toString()
            val passInp = binding.editTextTextPassword2.text.toString()
            val passConf = binding.editTextTextPassword3.text.toString()

            if(idInp != "" && nameInp != "" && passInp != "" && passInp == passConf) {
                //create user!
                userInfo = User(
                    id = idInp, //temporary? we might wanna use the email as ID or otherwise we can set the email as "name" and programmatically generate ids or smth
                    extraData = mutableMapOf(
                        "name" to nameInp, //nickname can be non-unique. It doesn't matter what your nickname is!
                        "password" to passInp, //password may need hashing of some sort
                        "pfp" to "" //Store b64 string for downscaled profile picture
                    )
                )

                print("Signing up user: $userInfo")

                //dev token so we dont need an endpoint
                val token = cli.devToken(userInfo.id)

                //unless.. Maybe create token for this user unless we decide against it but-

                //TODO: Check to see if this user already exists based on id

                // create user
                cli.connectUser(userInfo, token).enqueue { /* ... */ }
                print("HERE!!!\n")
                print("Token: $token\n")


                cli.disconnect() //logout right away

                //for user auth CLIENT SIDE GROSS
                val uReg: SharedPreferences = getSharedPreferences(userInfo.id, MODE_PRIVATE)
                val regEdit = uReg.edit()

                regEdit.putString("uid", userInfo.id).apply()
                regEdit.putString("nickname", userInfo.name).apply()
                regEdit.putString("pass", userInfo.getExtraValue("password", "INVALID")).apply()
                regEdit.putString("pfp", userInfo.getExtraValue("pfp", "INVALID")).apply()


                //go back to login
                val intent = Intent(this, m::class.java)
                startActivity(intent)
            }
        } //end button binding

        //Login link
        binding.signupLoginLink.setOnClickListener{
            //go to signup
            val intent = Intent(this, m::class.java)
            startActivity(intent)
        }
    }
}