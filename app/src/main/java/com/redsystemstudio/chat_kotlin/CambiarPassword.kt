package com.redsystemstudio.chat_kotlin

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.redsystemstudio.chat_kotlin.databinding.ActivityCambiarPasswordBinding

class CambiarPassword : AppCompatActivity() {

    private lateinit var binding : ActivityCambiarPasswordBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var progressDialog : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCambiarPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnCambiarPassword.setOnClickListener {
            validarInformacion()
        }
    }

    private var pass_actual = ""
    private var pass_nueva = ""
    private var r_pass_nueva = ""
    private fun validarInformacion() {
        pass_actual = binding.etPassActual.text.toString().trim()
        pass_nueva = binding.etPassNueva.text.toString().trim()
        r_pass_nueva = binding.etRPassNueva.text.toString().trim()

        if (pass_actual.isEmpty()){
            binding.etPassActual.error = "Ingrese contraseña actual"
            binding.etPassActual.requestFocus()
        }
        else if (pass_nueva.isEmpty()){
            binding.etPassNueva.error = "Ingrese nueva contraseña"
            binding.etPassNueva.requestFocus()
        }
        else if (r_pass_nueva.isEmpty()){
            binding.etRPassNueva.error = "Repita nueva contraseña"
            binding.etRPassNueva.requestFocus()
        }
        else if (pass_nueva != r_pass_nueva){
            binding.etRPassNueva.error = "No coinciden las contraseñas"
            binding.etRPassNueva.requestFocus()
        }
        else{
            autenticarUsuario()
        }


    }

    private fun autenticarUsuario() {
        progressDialog.setMessage("Autenticando usuario")
        progressDialog.show()

        val authCredential = EmailAuthProvider.getCredential(firebaseUser.email.toString(), pass_actual)
        firebaseUser.reauthenticate(authCredential)
            .addOnSuccessListener {
                actualizarPassword()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Falló la autenticación debido a ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }

    private fun actualizarPassword() {
        progressDialog.setMessage("Cambiando contraseña")
        progressDialog.show()

        firebaseUser.updatePassword(pass_nueva)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "La contraseña se ha actualizado",
                    Toast.LENGTH_SHORT
                ).show()
                cerrarSesion()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Falló la autenticación debido a ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }


    }

    private fun cerrarSesion(){
        firebaseAuth.signOut()
        startActivity(Intent(applicationContext, OpcionesLoginActivity::class.java))
        finishAffinity()

    }
}