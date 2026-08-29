package com.odontosystem.app.ui.dentist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.odontosystem.app.data.model.Dentist
import com.odontosystem.app.databinding.ActivityDentistDetailBinding

class DentistDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDentistDetailBinding
    private var dentist: Dentist? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate()
        binding = ActivityDentistDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dentist = intent.getSerializableExtra("dentist") as? Dentist
        if (dentist == null) {
            finish()
            return
        }

        setupViews(dentist!!)
    }

    private fun setupViews(dentist: Dentist) {
        binding.tvName.text = dentist.name
        binding.tvSpecialty.text = dentist.specialty
        binding.tvAddress.text = "📍 ${dentist.address} (${dentist.district})"
        binding.tvDays.text = "📅 ${dentist.availableDays.joinToString(", ")} (8:00 AM - 6:00 PM)"
        binding.tvPrice.text = "💳 S/ ${String.format("%.2f", dentist.price)}"

        binding.btnDetailWhatsApp.setOnClickListener {
            openWhatsApp(dentist)
        }

        binding.btnBookAppointment.setOnClickListener {
            val dialog = BookAppointmentDialogFragment.newInstance(dentist) {
                Toast.makeText(this, "¡Cita reservada con éxito!", Toast.LENGTH_LONG).show()
                finish()
            }
            dialog.show(supportFragmentManager, "BookAppointmentDetail")
        }
    }

    private fun openWhatsApp(dentist: Dentist) {
        val message = "Hola ${dentist.name}, me contacto desde OdontoSystem sobre la consulta odontológica en el distrito de ${dentist.district}."
        val url = "https://api.whatsapp.com/send?phone=${dentist.phone}&text=${Uri.encode(message)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo abrir WhatsApp en el dispositivo.", Toast.LENGTH_SHORT).show()
        }
    }
}
