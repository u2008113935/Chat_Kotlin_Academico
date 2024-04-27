package com.redsystemstudio.chat_kotlin.Fragmentos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.redsystemstudio.chat_kotlin.CambiarPassword
import com.redsystemstudio.chat_kotlin.Constantes
import com.redsystemstudio.chat_kotlin.EditarInformacion
import com.redsystemstudio.chat_kotlin.OpcionesLoginActivity
import com.redsystemstudio.chat_kotlin.R
import com.redsystemstudio.chat_kotlin.databinding.FragmentPerfilBinding

class FragmentPerfil : Fragment() {

    private lateinit var binding : FragmentPerfilBinding
    private lateinit var mContext : Context
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentPerfilBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        cargarInformacion()

        binding.btnActualizarInfo.setOnClickListener {
            startActivity(Intent(mContext, EditarInformacion::class.java))
        }

        binding.btnCambiarPass.setOnClickListener {
            startActivity(Intent(mContext, CambiarPassword::class.java))
        }

        binding.btnCerrarsesion.setOnClickListener {
            actualizarEstado()
            cerrarSesion()
        }
    }

    private fun cerrarSesion(){
        object : CountDownTimer(3000, 1000){
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                 //Pasado 3 segundos se va a ejecutar estas líneas de código
                firebaseAuth.signOut()
                startActivity(Intent(mContext, OpcionesLoginActivity::class.java))
                activity?.finishAffinity()
            }

        }.start()
    }

    private fun actualizarEstado(){
        val ref = FirebaseDatabase.getInstance().reference.child("Usuarios").child(firebaseAuth.uid!!)

        val hashMap = HashMap<String, Any>()
        hashMap["estado"] = "Offline"
        ref!!.updateChildren(hashMap)
    }

    private fun cargarInformacion() {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombres = "${snapshot.child("nombres").value}"
                    val email = "${snapshot.child("email").value}"
                    val proveedor = "${snapshot.child("proveedor").value}"
                    var t_registro = "${snapshot.child("tiempoR").value}"
                    val imagen = "${snapshot.child("imagen").value}"

                    if (t_registro == "null"){
                        t_registro = "0"
                    }

                    //Conversión a fecha
                    val fecha = Constantes.formatoFecha(t_registro.toLong())

                    /*Setear la información en las vistas*/
                    binding.tvNombres.text = nombres
                    binding.tvEmail.text = email
                    binding.tvProveedor.text = proveedor
                    binding.tvTRegistro.text = fecha

                    /*Setear la imagen en el Iv*/
                    try {
                        Glide.with(mContext.applicationContext)
                            .load(imagen)
                            .placeholder(R.drawable.ic_img_perfil)
                            .into(binding.ivPerfil)
                    }catch (e:Exception){
                        Toast.makeText(
                            mContext,
                            "${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    if (proveedor == "Email"){
                        binding.btnCambiarPass.visibility = View.VISIBLE
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })


















    }

}