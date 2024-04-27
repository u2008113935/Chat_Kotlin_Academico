package com.redsystemstudio.chat_kotlin

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.redsystemstudio.chat_kotlin.databinding.ActivityLoginEmailBinding

class LoginEmailActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginEmailBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.btnIngresar.setOnClickListener {
            validarInformacion()
        }

        binding.tvRecuperarCuenta.setOnClickListener {
            startActivity(Intent(applicationContext, OlvidePassword::class.java))
        }

        binding.tvRegistrarme.setOnClickListener {
            startActivity(Intent(applicationContext, RegistroEmailActivity::class.java))
        }
    }

    private var email = ""
    private var password = ""
    private fun validarInformacion() {
        email = binding.etEmail.text.toString().trim()
        password = binding.etPassword.text.toString().trim()

        if (email.isEmpty()){
            binding.etEmail.error = "Ingrese email"
            binding.etEmail.requestFocus()
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.etEmail.error = "Email no válido"
            binding.etEmail.requestFocus()
        }
        else if (password.isEmpty()){
            binding.etPassword.error = "Ingrese password"
            binding.etPassword.requestFocus()
        }
        else{
            logearUsuario()
        }
    }

    private fun logearUsuario() {
        progressDialog.setMessage("Ingresando")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                progressDialog.dismiss()
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "No se realizó el logeo debido a ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }
}