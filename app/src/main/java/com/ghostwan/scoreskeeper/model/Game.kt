package com.ghostwan.scoreskeeper.model

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class Game: RealmObject {
    @PrimaryKey
    var name: String = ""
    var classification: GameClassification = GameClassification.HIGHEST
    var parties: MutableList<Party> = arrayListOf()
}
