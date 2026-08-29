package com.odontosystem.app.ui.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.odontosystem.app.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val uploadedDocs = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnUploadDni.setOnClickListener { markUploaded(1, "DNI") }
        binding.btnUploadTitulo.setOnClickListener { markUploaded(2, "Título") }
        binding.btnUploadColegiatura.setOnClickListener { markUploaded(3, "Colegiatura") }
        binding.btnUploadCv.setOnClickListener { markUploaded(4, "CV") }

        binding.btnRegisterSubmit.setOnClickListener {
            if (uploadedDocs.size < 4) {
                Toast.makeText(this, "Por favor, sube los 4 documentos requeridos (RF08).", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Registro enviado. El administrador revisará tu perfil (RF09).", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun markUploaded(id: Int, name: String) {
        uploadedDocs.add(id)
        Toast.makeText(this, "Documento $name cargado con éxito.", Toast.LENGTH_SHORT).show()
        
        when(id) {
            1 -> binding.btnUploadDni.text = "✅ DNI Cargado"
            2 -> binding.btnUploadTitulo.text = "✅ Título Cargado"
            3 -> binding.btnUploadColegiatura.text = "✅ Colegiatura Cargada"
            4 -> binding.btnUploadCv.text = "✅ CV Cargado"
        }
    }
}
