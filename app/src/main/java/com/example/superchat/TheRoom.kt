package com.example.superchat

import android.graphics.Bitmap
import androidx.room.*

class TheRoom {

/*------------------------------------------------------------------------------------------*/
    //Holds user information
    @Entity

/*------------------------------------------------------------------------------------------*/

/*------------------------------------------------------------------------------------------*/
    //TODO: Finish modeling database
/*------------------------------------------------------------------------------------------*/
    //Cream
    @Database(
        entities = [

                   ],
        version = 1 //This will decide whether or not the database is remade on create I think
    )
    abstract class AppDatabase : RoomDatabase() { //add DAO objects to each table
        //abstract fun userDao(): UserDao
    }
}