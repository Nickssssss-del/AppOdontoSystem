package com.odontosystem.app.repository

import com.odontosystem.app.data.model.Dentist
import com.odontosystem.app.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DentistRepository(private val apiService: ApiService) {

    suspend fun getDentists(district: String? = null, query: String? = null): Result<List<Dentist>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDentists(district, query)
                if (response.isSuccessful && response.body() != null) {
                    var list = response.body()!!

                    if (!district.isNullOrEmpty() && district != "Todos los distritos") {
                        list = list.filter { it.district.equals(district, ignoreCase = true) }
                    }

                    if (!query.isNullOrEmpty()) {
                        list = list.filter {
                            it.name.contains(query, ignoreCase = true) ||
                                    it.specialty.contains(query, ignoreCase = true)
                        }
                    }

                    Result.success(list)
                } else {
                    Result.failure(Exception("Error al cargar odontólogos"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
