package com.redsystemstudio.chat_kotlin.Chat

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.redsystemstudio.chat_kotlin.Adaptadores.AdaptadorChat
import com.redsystemstudio.chat_kotlin.Constantes
import com.redsystemstudio.chat_kotlin.Modelos.Chat
import com.redsystemstudio.chat_kotlin.R
import com.redsystemstudio.chat_kotlin.databinding.ActivityChatBinding
import org.json.JSONObject

class ChatActivity : AppCompatActivity() {

    private lateinit var binding : ActivityChatBinding
    private var uid = ""

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog : ProgressDialog
    private var miUid = ""

    private var chatRuta = ""
    private var imagenUri : Uri?= null

    private var miNombre = ""
    private var recibimosToken = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        uid = intent.getStringExtra("uid")!!
        miUid = firebaseAuth.uid!!

        chatRuta = Constantes.rutaChat(uid,miUid)

        cargarMiInformacion()

        binding.adjuntarFAB.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                imagenGaleria()
            }else{
                solicitarPermisoAlmacenamiento.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.enviarFAB.setOnClickListener {
            validarMensaje()
        }

        cargarInfo()
        cargarMensajes()
    }

    private fun cargarMiInformacion(){
        var ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    miNombre = "${snapshot.child("nombres").value}"
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun cargarMensajes() {
        val mensajesArrayList = ArrayList<Chat>()
        val ref = FirebaseDatabase.getInstance().getReference("Chats")
        ref.child(chatRuta)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    mensajesArrayList.clear()
                    for (ds : DataSnapshot in snapshot.children){
                        try {
                            val chat = ds.getValue(Chat::class.java)
                            mensajesArrayList.add(chat!!)
                        }catch (e:Exception){

                        }
                    }

                    val adaptadorChat = AdaptadorChat(this@ChatActivity, mensajesArrayList)
                    binding.chatsRV.adapter = adaptadorChat

                    binding.chatsRV.setHasFixedSize(true)
                    var linearLayoutManager = LinearLayoutManager(this@ChatActivity)
                    linearLayoutManager.stackFromEnd = true
                    binding.chatsRV.layoutManager = linearLayoutManager
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun validarMensaje() {
        val mensaje = binding.EtMensajeChat.text.toString().trim()
        val tiempo = Constantes.obtenerTiempoD()

        if (mensaje.isEmpty()){
            Toast.makeText(this, "Ingrese un mensaje",Toast.LENGTH_SHORT).show()
        }else{
            enviarMensaje(Constantes.MENSAJE_TIPO_TEXTO, mensaje , tiempo)
        }
    }

    private fun cargarInfo(){
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(uid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombres = "${snapshot.child("nombres").value}"
                    val imagen = "${snapshot.child("imagen").value}"
                    val estado = "${snapshot.child("estado").value}"
                    recibimosToken = "${snapshot.child("fcmToken").value}"

                    binding.txtEstadoChat.text = estado
                    binding.txtNombreUsuario.text = nombres

                    try {
                        Glide.with(applicationContext)
                            .load(imagen)
                            .placeholder(R.drawable.perfil_usuario)
                            .into(binding.toolbarIv)
                    }catch (e:Exception){

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun imagenGaleria(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        resultadoGaleriaARL.launch(intent)
    }

    private val resultadoGaleriaARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){resultado->
            if (resultado.resultCode == Activity.RESULT_OK){
                val data = resultado.data
                imagenUri = data!!.data
                subirImgStorage()
            }else{
                Toast.makeText(
                    this,
                    "Cancelado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val solicitarPermisoAlmacenamiento =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){esConcedido->
            if (esConcedido){
                imagenGaleria()
            }else{
                Toast.makeText(
                    this,
                    "El permiso de almacenamiento no ha sido concedido",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun subirImgStorage(){
        progressDialog.setMessage("Subiendo imagen")
        progressDialog.show()

        val tiempo = Constantes.obtenerTiempoD()
        val nombreRutaImg = "ImagenesChat/$tiempo"
        val storageRef = FirebaseStorage.getInstance().getReference(nombreRutaImg)
        storageRef.putFile(imagenUri!!)
            .addOnSuccessListener {taskSnapshot->
                val uriTask = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val urlImagen = uriTask.result.toString()
                if (uriTask.isSuccessful){
                    enviarMensaje(Constantes.MENSAJE_TIPO_IMAGEN, urlImagen , tiempo)
                }

            }
            .addOnFailureListener {e->
                    Toast.makeText(this,
                        "No se pudo enviar la imagen debido a ${e.message}",
                        Toast.LENGTH_SHORT).show()

            }

    }

    private fun enviarMensaje(tipoMensaje: String, mensaje: String, tiempo: Long) {
        progressDialog.setMessage("Enviando mensaje")
        progressDialog.show()

        val refChat = FirebaseDatabase.getInstance().getReference("Chats")
        val keyId = "${refChat.push().key}"
        val hashMap = HashMap<String,Any>()

        hashMap["idMensaje"] = "${keyId}"
        hashMap["tipoMensaje"] = "${tipoMensaje}"
        hashMap["mensaje"] = "${mensaje}"
        hashMap["emisorUid"] = "${miUid}"
        hashMap["receptorUid"] = "$uid"
        hashMap["tiempo"] = tiempo

        refChat.child(chatRuta)
            .child(keyId)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                binding.EtMensajeChat.setText("")

                if (tipoMensaje == Constantes.MENSAJE_TIPO_TEXTO){
                    prepararNotificacion(mensaje)
                }else{
                    prepararNotificacion("Se envió una imagen")
                }

            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this,
                    "No se pudo enviar el mensaje debido a ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }

    }

    private fun actualizarEstado(estado : String){
        val ref = FirebaseDatabase.getInstance().reference.child("Usuarios").child(firebaseAuth.uid!!)

        val hashMap = HashMap<String, Any>()
        hashMap["estado"] = estado
        ref!!.updateChildren(hashMap)
    }

    override fun onResume() {
        super.onResume()
        if (firebaseAuth.currentUser!=null){
            actualizarEstado("Online")
        }

    }

    override fun onPause() {
        super.onPause()
        if (firebaseAuth.currentUser!=null){
            actualizarEstado("Offline")
        }

    }

    private fun prepararNotificacion(mensaje: String){
        val notificationJo = JSONObject()
        val notificationDataJo = JSONObject()
        val notificationNotificationJo = JSONObject()

        try {
            notificationDataJo.put("notificationType", "${Constantes.NOTIFICACION_DE_NUEVO_MENSAJE}")
            notificationDataJo.put("senderUid", "${firebaseAuth.uid}")
            notificationNotificationJo.put("title", "${miNombre}")
            notificationNotificationJo.put("body", "${mensaje}")
            notificationNotificationJo.put("sound", "default")

            notificationJo.put("to", "${recibimosToken}")
            notificationJo.put("notification", notificationNotificationJo)
            notificationJo.put("data", notificationDataJo)

        }catch (e: Exception){

        }

        enviarNotificacion(notificationJo)

    }

    private fun enviarNotificacion(notificationJo: JSONObject) {
        val jsonObjectRequest : JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            "https://fcm.googleapis.com/fcm/send",
            notificationJo,
            com.android.volley.Response.Listener {
                //Si la notifiación fue enviada
            },
            com.android.volley.Response.ErrorListener {
                //Si la notificación NO fue enviada
            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["Authorization"] = "key=${Constantes.FCM_SERVER_KEY}"
                return headers
            }
        }
        Volley.newRequestQueue(this).add(jsonObjectRequest)

    }

}