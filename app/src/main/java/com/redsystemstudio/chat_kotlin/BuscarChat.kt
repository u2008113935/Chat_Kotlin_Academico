package com.redsystemstudio.chat_kotlin

import android.widget.Filter
import com.redsystemstudio.chat_kotlin.Adaptadores.AdaptadorChats
import com.redsystemstudio.chat_kotlin.Modelos.Chats
import java.util.Locale

class BuscarChat : Filter {

    private val adaptadorChats : AdaptadorChats
    private val filtroLista : ArrayList<Chats>

    constructor(adaptadorChats: AdaptadorChats, filtroLista: ArrayList<Chats>) : super() {
        this.adaptadorChats = adaptadorChats
        this.filtroLista = filtroLista
    }


    override fun performFiltering(filtro: CharSequence?): FilterResults {
        var filtro: CharSequence?=filtro
        val resultados = FilterResults()

        if (!filtro.isNullOrBlank()){
            filtro = filtro.toString().uppercase(Locale.getDefault())
            val filtroModelo = ArrayList<Chats>()
            for (i in filtroLista.indices){
                if (filtroLista[i].nombres.uppercase().contains(filtro)){
                    filtroModelo.add(filtroLista[i])
                }
            }

            resultados.count = filtroModelo.size
            resultados.values = filtroModelo
        }else{
            resultados.count = filtroLista.size
            resultados.values = filtroLista
        }
        return resultados
    }

    override fun publishResults(filtro: CharSequence?, resultados: FilterResults) {
        adaptadorChats.chatArrayList = resultados.values as ArrayList<Chats>
        adaptadorChats.notifyDataSetChanged()
    }
}