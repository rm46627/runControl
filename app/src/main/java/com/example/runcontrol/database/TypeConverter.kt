package com.example.runcontrol.database

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TypeConverter {

    var gson = Gson()

    @TypeConverter
    fun fromLatLngArrayList(value: List<LatLng>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toLatLngArrayList(value: String): List<LatLng> {
        val listType = object: TypeToken<List<LatLng>>() {}.type
        return gson.fromJson(value, listType)
    }

}