package com.odontosystem.app.ui.dentist

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.odontosystem.app.databinding.ActivityVerificationStatusBinding

class VerificationStatusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerificationStatusBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val status = intent.getStringExtra("status") ?: "Observado"
        setupView(status)
    }

    private fun setupView(status: String) {
        when(status) {
            "Observado" -> {
                binding.tvStatusTitle.text = "Perfil Observado (RF09)"
                binding.cardObservation.visibility = View.VISIBLE
                binding.ivStatusIcon.setImageResource(android.R.drawable.ic_dialog_alert)
                binding.ivStatusIcon.setColorFilter(android.graphics.Color.parseColor("#F59E0B"))
            }
            "Rechazado" -> {
                binding.tvStatusTitle.text = "Perfil Rechazado"
                binding.tvStatusDesc.text = "Tu perfil no cumple con los requisitos mínimos de OdontoSystem."
                binding.ivStatusIcon.setImageResource(android.R.drawable.ic_delete)
                binding.ivStatusIcon.setColorFilter(android.graphics.Color.RED)
            }
            else -> {
                binding.tvStatusTitle.text = "En Verificación"
            }
        }

        binding.btnResubmit.setOnClickListener {
            Toast.makeText(this, "Documento re-subido con éxito (RF08). Pendiente de nueva revisión.", Toast.LENGTH_LONG).show()
            finish()
        }

        binding.btnBackLogin.setOnClickListener {
            finish()
        }
    }
}
