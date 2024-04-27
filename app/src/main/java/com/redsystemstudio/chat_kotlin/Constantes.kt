package com.redsystemstudio.chat_kotlin

import android.text.format.DateFormat
import java.util.Arrays
import java.util.Calendar
import java.util.Locale

object Constantes {

    const val MENSAJE_TIPO_TEXTO = "TEXTO"
    const val MENSAJE_TIPO_IMAGEN = "IMAGEN"

    const val NOTIFICACION_DE_NUEVO_MENSAJE = "NOTIFICACION_DE_NUEVO_MENSAJE"
    const val FCM_SERVER_KEY = "AAAAcUwv2ko:APA91bFhGozIj4wbwW56WvW5K89PXq3yFeMly85DSDZomr3dmCRxskZl8TvWpJDeEtXGTo6PtwUvr7NY6MvvW-ye__TngLcueKpIsiwWQFXbxF6uOBkP771-96fYkSO4xW1IP7kWeXTm"

    fun obtenerTiempoD() : Long{
        return System.currentTimeMillis()
    }

    fun formatoFecha (tiempo : Long) :String{
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = tiempo

        return DateFormat.format("dd/MM/yyyy", calendar).toString()
    }

    fun obtenerFechaHora(tiempo: Long) : String{
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = tiempo
        return DateFormat.format("dd/MM/yyyy hh:mm:a", calendar).toString()
    }

    fun rutaChat(receptorUid : String , emisorUid : String) : String{
        val arrayUid = arrayOf(receptorUid,emisorUid)
        Arrays.sort(arrayUid)
        //Uid del usuario con quien entablamos la conversación [receptor] = BqxDDpcD4BbhSWVGRkndTDkqt2o2
        //Nuestro uid [emisor] = 1plNxwsL2wgjsEsyoZWt1TeD1cf1
        //La ruta seria = 1plNxwsL2wgjsEsyoZWt1TeD1cf1_BqxDDpcD4BbhSWVGRkndTDkqt2o2
        return "${arrayUid[0]}_${arrayUid[1]}"
    }

}