package com.example.superchat

/**
 * Stores data for a friend entry
 * @param uid User id of the friend
 * @param favorite Stores whether or not the user is a best friend. Defaults to false
 */
data class Friend(val uid: String, var favorite: Boolean? = false) {

    /**
     * Toggles favorite variable
     * Used for friending/unfriending
     */
    fun toggleFavorite(){
        favorite = !favorite!!
    }

    /**
     * Set this Friend as a best friend
     */
    fun setFav(){
        favorite = true
    }

    /**
     * Remove best friend standing
     */
    fun removeFav(){
        favorite = false
    }
}