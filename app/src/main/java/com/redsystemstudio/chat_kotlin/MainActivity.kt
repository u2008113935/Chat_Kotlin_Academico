package com.redsystemstudio.chat_kotlin

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.redsystemstudio.chat_kotlin.Fragmentos.FragmentChats
import com.redsystemstudio.chat_kotlin.Fragmentos.FragmentPerfil
import com.redsystemstudio.chat_kotlin.Fragmentos.FragmentUsuarios
import com.redsystemstudio.chat_kotlin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        comprobarSesion()


        //Fragmento por defecto
        verFragmentoPerfil()

        binding.bottomNV.setOnItemSelectedListener { item->
            when(item.itemId){
                R.id.item_perfil->{
                    //Visualizar el fragmento Perfil
                    verFragmentoPerfil()
                    true
                }
                R.id.item_usuarios->{
                    //Visualizar el fragmento Usuarios
                    verFragmentoUsuarios()
                    true
                }
                R.id.item_chats->{
                    //Visualizar el fragmento Chats
                    verFragmentoChats()
                    true
                }
                else->{
                    false
                }
            }
        }
    }

    private fun comprobarSesion() {
        if (firebaseAuth.currentUser == null){
            startActivity(Intent(applicationContext, OpcionesLoginActivity::class.java))
            finishAffinity()
        }else{
            agregarToken()
            solicitarPermisoNotificaciones()
        }
    }

    private fun verFragmentoPerfil(){
        binding.tvTitulo.text = "Perfil"

        val fragment = FragmentPerfil()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentoFL.id, fragment, "Fragment Perfil")
        fragmentTransaction.commit()
    }

    private fun verFragmentoUsuarios(){
        binding.tvTitulo.text = "Usuarios"

        val fragment = FragmentUsuarios()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentoFL.id, fragment, "Fragment Usuarios")
        fragmentTransaction.commit()
    }

    private fun verFragmentoChats(){
        binding.tvTitulo.text = "Chats"

        val fragment = FragmentChats()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentoFL.id, fragment, "Fragment Chats")
        fragmentTransaction.commit()
    }

    private fun actualizarEstado(estado : String){
        val ref = FirebaseDatabase.getInstance().reference.child("Usuarios").child(firebaseAuth.uid!!)

        val hashMap = HashMap<String, Any>()
        hashMap["estado"] = estado
        ref!!.updateChildren(hashMap)
    }

    override fun onResume() {
        super.onResume()
        if (firebaseAuth.currentUser !=null){
            actualizarEstado("Online")
        }

    }

    override fun onPause() {
        super.onPause()
        if (firebaseAuth.currentUser !=null){
            actualizarEstado("Offline")
        }
    }

    private fun agregarToken(){
        val miUid = "${firebaseAuth.uid}"
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener {fcmToken->
                val hashMap = HashMap<String,Any>()
                hashMap["fcmToken"] = "${fcmToken}"
                val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
                ref.child(miUid)
                    .updateChildren(hashMap)
                    .addOnSuccessListener {
                        /*El token se agregó con éxito*/
                    }
                    .addOnFailureListener {e->
                        Toast.makeText(this, "${e.message}",Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {e->
                Toast.makeText(this, "${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private fun solicitarPermisoNotificaciones(){
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU){
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_DENIED){
                concederPermiso.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val concederPermiso =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){esConcedido->
            //El permiso se ha concedido
        }

}