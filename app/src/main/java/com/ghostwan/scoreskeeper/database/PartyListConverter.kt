package com.ghostwan.scoreskeeper.database

import androidx.room.TypeConverter
import com.ghostwan.scoreskeeper.model.Party
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class PartyListConverter {

    private var gson = Gson()

    @TypeConverter
    fun stringToList(data: String?): MutableList<Party>? {
        if (data == null){
            return Collections.emptyList()
        }
        val listType = object : TypeToken<ArrayList<String>>() {}.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun listToString(someObjects: MutableList<Party>?): String? {
        return gson.toJson(someObjects)
    }



}