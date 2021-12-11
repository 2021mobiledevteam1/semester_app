package com.example.superchat

import android.content.Context
import android.graphics.Bitmap
import androidx.room.*

class TheRoom {

/*------------------------------------------------------------------------------------------*/
    //Holds user information
    @Entity
    data class File(
        @PrimaryKey val fid: Int,
        @ColumnInfo(name = "file_name") val fileName: String?, //Name of file to display
        @ColumnInfo(name = "file_path") val filePath: String?, //Path to file in android shared storage
        @ColumnInfo(name = "file_ext") val fileExt: String? //Extension of the file; the filetype
    )

    @Dao
    interface FileDao {
        @Query("SELECT * FROM file")
        fun getAll(): List<File>

        @Query("SELECT * FROM file WHERE fid IN (:files)")
        fun getFilesByIds(files: IntArray): List<File>

        @Query("SELECT * FROM file WHERE fid = :fileId")
        fun getFileById(fileId: Int): File

        @Query("SELECT * FROM file WHERE file_name LIKE :fName")
        fun getFileByName(fName: String): File

        @Delete
        fun delete(file: File)
    }

/*------------------------------------------------------------------------------------------*/

/*------------------------------------------------------------------------------------------*/
    //TODO: Finish modeling database
/*------------------------------------------------------------------------------------------*/
    //Cream
    @Database(
        entities = [
            File::class
                   ],
        version = 1 //This will decide whether or not the database is remade on create I think
    )
    abstract class AppDatabase : RoomDatabase() { //add DAO objects to each table
        abstract fun fileDao(): FileDao

        companion object {
            @Volatile private var instance: AppDatabase? = null
            private val LOCK = Any()

            operator fun invoke(context: Context)= instance ?: synchronized(LOCK){
                instance ?: buildDatabase(context).also { instance = it}
            }

            private fun buildDatabase(context: Context) = Room.databaseBuilder(context,
                AppDatabase::class.java, "superchat")
                .build()
        }
    }
}