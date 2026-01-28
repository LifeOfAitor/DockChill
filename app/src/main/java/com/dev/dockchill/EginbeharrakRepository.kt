package com.dev.dockchill

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
//hainbat activity erabiliko dituzten funtzioak hemen gordetzea ideia ona da
class EginbeharrakRepository(private val context: Context) {

    private val gson = Gson()
    private val fileName = "egitekoak.json"

    /**
     * Eginbeharren zerrenda kargatzen du. Lehenik fitxategi lokaletik saiatzen da,
     * eta huts egiten badu (adibidez, lehen aldia denean), 'assets' karpetatik kargatzen du.
     */
    fun kargatuEginbeharrak(): MutableList<Eginbeharra> {
        return try {
            // Saiatu fitxategi lokaletik irakurtzen
            val jsonString = context.openFileInput(fileName).bufferedReader().use { it.readText() }
            val listType = object : TypeToken<MutableList<Eginbeharra>>() {}.type
            gson.fromJson(jsonString, listType)
        } catch (e: IOException) {
            // Errore bat gertatzen bada (adib. fitxategia ez da existitzen), assets-etik kargatu
            kargatuEginbeharrakAssets()
        }
    }

    /**
     * Eginbeharren zerrenda gorde fitxategi lokal batean JSON formatuan.
     */
    fun gordeEginbeharrak(lista: List<Eginbeharra>) {
        val jsonString = gson.toJson(lista)
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            it.write(jsonString.toByteArray())
        }
    }

    /**
     * 'assets' karpetan dagoen jatorrizko JSON fitxategia soilik kargatzen du.
     * Barne-erabilerarako da, lehen aldiz kargatzeko.
     */
    private fun kargatuEginbeharrakAssets(): MutableList<Eginbeharra> {
        return try {
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val listType = object : TypeToken<MutableList<Eginbeharra>>() {}.type
            gson.fromJson(jsonString, listType)
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            mutableListOf() // Errore kasuan, zerrenda hutsa itzuli
        }
    }
}