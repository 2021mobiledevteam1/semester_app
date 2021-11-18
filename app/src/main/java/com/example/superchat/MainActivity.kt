package com.example.superchat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.room.Room

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val db = Room.databaseBuilder(
            applicationContext,
            TheRoom.AppDatabase::class.java, "superchat"
        ).build()

        //TODO: Create Dao objects for each table once database is finished.
        //hi
    }
}