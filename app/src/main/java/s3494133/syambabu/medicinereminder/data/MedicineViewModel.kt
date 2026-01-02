package s3494133.syambabu.medicinereminder.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import s3494133.syambabu.medicinereminder.data.MedicineRepository
import kotlin.math.max

open class MedicineViewModel(application: Application) : AndroidViewModel(application) {
    private lateinit var repository: MedicineRepository
    val allMedicines: StateFlow<List<Medicine>> = MutableStateFlow(emptyList())
    val isReady = MutableStateFlow(false)

    init {
        Log.d("MedicineViewModel", "Initializing ViewModel...")
        viewModelScope.launch {
            try {
                val database = AppDatabase.getDatabase(application)
                repository =
                    MedicineRepository(database.medicineDao(), database.medicineHistoryDao())
                repository.allMedicines.collect { medicines ->
                    (allMedicines as MutableStateFlow).value = medicines
                }
                isReady.value = true
                Log.d("MedicineViewModel", "ViewModel initialized and ready.")
            } catch (e: Exception) {
                Log.e("MedicineViewModel", "Error initializing ViewModel: ${e.message}", e)
            }
        }
    }

    private fun ensureRepositoryInitialized() {
        if (!::repository.isInitialized) {
            Log.e("MedicineViewModel", "Repository not initialized when accessed!")
            throw IllegalStateException("Repository not initialized. ViewModel not ready.")
        }
    }

    suspend fun insert(medicine: Medicine): Long {
        ensureRepositoryInitialized()
        return repository.insert(medicine)
    }

    fun update(medicine: Medicine) = viewModelScope.launch {
        ensureRepositoryInitialized()
        repository.update(medicine)
    }

    fun delete(medicine: Medicine) = viewModelScope.launch {
        ensureRepositoryInitialized()
        repository.delete(medicine)
    }

    fun getMedicineById(medicineId: Int): Flow<Medicine?> {
        ensureRepositoryInitialized()
        return repository.getMedicineById(medicineId)
    }

    fun markMedicineTakenOld(medicineId: Int, dosage: String) = viewModelScope.launch {
        ensureRepositoryInitialized()
        repository.insertMedicineHistory(
            MedicineHistory(
                medicineId = medicineId,
                takenTimestamp = System.currentTimeMillis(),
                dosageTaken = dosage
            )
        )
        val medicine = repository.getMedicineById(medicineId).first()
        medicine?.let {
            val newQuantity = (it.currentQuantity ?: 0) - 1
            repository.update(it.copy(currentQuantity = max(0, newQuantity)))
        }
    }

    fun markMedicineTaken(updatedMedicine: Medicine) = viewModelScope.launch {
        ensureRepositoryInitialized()

        repository.insertMedicineHistory(
            MedicineHistory(
                medicineId = updatedMedicine.id,
                takenTimestamp = System.currentTimeMillis(),
                dosageTaken = updatedMedicine.dosage
            )
        )

        val newQuantity = (updatedMedicine.currentQuantity ?: 0) - 1

        repository.update(
            updatedMedicine.copy(
                currentQuantity = max(0, newQuantity)
            )
        )
    }


    fun getMedicineHistoryForMedicine(medicineId: Int): Flow<List<MedicineHistory>> {
        ensureRepositoryInitialized()
        return repository.getMedicineHistoryForMedicine(medicineId)
    }

    fun deleteHistoryForMedicine(medicineId: Int) = viewModelScope.launch {
        ensureRepositoryInitialized()
        repository.deleteHistoryForMedicine(medicineId)
    }

    class MedicineViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MedicineViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MedicineViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}