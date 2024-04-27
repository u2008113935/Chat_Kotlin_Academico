package com.redsystemstudio.chat_kotlin

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.redsystemstudio.chat_kotlin.databinding.ActivityOpcionesLoginBinding
import java.lang.Exception

class OpcionesLoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityOpcionesLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog : ProgressDialog
    private lateinit var mGoogleSignInClient : GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpcionesLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        comprobarSesion()

        binding.opcionEmail.setOnClickListener {
            startActivity(Intent(applicationContext, LoginEmailActivity::class.java))
        }

        binding.opcionGoogle.setOnClickListener {
            iniciarGoogle()
        }

    }

    private fun iniciarGoogle() {
        val googleSignInIntent = mGoogleSignInClient.signInIntent
        googleSingInARL.launch(googleSignInIntent)
    }

    private val googleSingInARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){resultado->
        if (resultado.resultCode == RESULT_OK){
            val data = resultado.data

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val cuenta = task.getResult(ApiException::class.java)
                autenticarCuentaGoogle(cuenta.idToken)
            }catch (e : Exception){
                Toast.makeText(
                    this,
                    "${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }else{
            Toast.makeText(
                this,
                "Cancelado",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun autenticarCuentaGoogle(idToken: String?) {
        val credencial = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credencial)
            .addOnSuccessListener { authResultado->
                if (authResultado.additionalUserInfo!!.isNewUser){
                    actualizarInfoUsuario()
                }else{
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                }
            }
            .addOnFailureListener {e->
                Toast.makeText(
                    this,
                    "${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }

    private fun actualizarInfoUsuario() {
        progressDialog.setMessage("Guardando información")

        val uidU = firebaseAuth.uid
        val nombresU = firebaseAuth.currentUser!!.displayName
        val emailU = firebaseAuth.currentUser!!.email
        val tiempoR = Constantes.obtenerTiempoD()

        val datosUsuario = HashMap<String, Any>()

        datosUsuario["uid"] = "$uidU"
        datosUsuario["nombres"] = "$nombresU"
        datosUsuario["email"] = "$emailU"
        datosUsuario["tiempoR"] = "$tiempoR"
        datosUsuario["proveedor"] = "Google"
        datosUsuario["estado"] = "Online"
        datosUsuario["imagen"] = ""

        val reference = FirebaseDatabase.getInstance().getReference("Usuarios")
        reference.child(uidU!!)
            .setValue(datosUsuario)
            .addOnSuccessListener {
                progressDialog.dismiss()

                startActivity(Intent(applicationContext, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Falló la creación de la cuenta debido a ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun comprobarSesion() {
        if (firebaseAuth.currentUser != null){
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }
    }
}