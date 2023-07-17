package com.ghostwan.scoreskeeper.model

import io.realm.kotlin.types.RealmObject
import java.util.*

class Party : RealmObject{
    val id: String = UUID.randomUUID().toString()
    var date: Date = Date()
    var players: MutableList<String> = mutableListOf()
}

