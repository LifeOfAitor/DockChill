package com.dev.dockchill

import com.google.type.DateTime

// Pomodoroko kontadorerako ezaugarriak hemen gordeko dira
class Kontadore {
    var minutuak: DateTime?
    var deskantsoa: DateTime?
    var rondak: Int = 0

    constructor(minutuak: DateTime, deskantsoa: DateTime, rondak: Int) {
        this.minutuak = minutuak
        this.deskantsoa = deskantsoa
        this.rondak = rondak
    }
}
