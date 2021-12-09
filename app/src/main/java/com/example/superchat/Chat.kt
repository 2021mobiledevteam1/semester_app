package com.example.superchat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class Chat : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val db = TheRoom.AppDatabase(this)
        //dao
        val fileDao = db.fileDao()
    }
}