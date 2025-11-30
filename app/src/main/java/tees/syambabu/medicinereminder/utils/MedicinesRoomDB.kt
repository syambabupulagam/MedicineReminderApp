package tees.syambabu.medicinereminder.utils


import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// --- 1. Data Models ---

// Type Converters for Room to store List<String> and List<Int>
class Converters {
    @TypeConverter
    fun fromStringList(value: String?): List<String>? {
        return value?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    fun fromIntList(value: String?): List<Int>? {
        return value?.split(",")?.mapNotNull { it.trim().toIntOrNull() }?.filter { it >= 1 && it <= 7 } // Ensure valid day of week
    }

    @TypeConverter
    fun toIntList(list: List<Int>?): String? {
        return list?.joinToString(",")
    }
}

// Medicine Entity (Updated)
@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val photoUri: String?,
    val dosage: String,
    val times: List<String>,
    val frequencyType: String,
    val specificDays: List<Int>?,
    val totalQuantity: Int?,
    val currentQuantity: Int?,
    val refillThreshold: Int?,
    val notes: String?,
    val sideEffects: String?,
    val purpose: String?
)

@Entity(tableName = "medicine_history")
data class MedicineHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val medicineId: Int,
    val takenTimestamp: Long,
    val dosageTaken: String
)

// --- 2. Room Database Setup ---
@Dao
interface MedicineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: Medicine): Long // Return row ID for new medicine

    @Update
    suspend fun updateMedicine(medicine: Medicine)

    @Query("SELECT * FROM medicines ORDER BY name ASC")
    fun getAllMedicines(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE id = :medicineId")
    fun getMedicineById(medicineId: Int): Flow<Medicine?>

    @Delete
    suspend fun deleteMedicine(medicine: Medicine)
}

@Dao
interface MedicineHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicineHistory(history: MedicineHistory)

    @Query("SELECT * FROM medicine_history WHERE medicineId = :medicineId ORDER BY takenTimestamp DESC")
    fun getMedicineHistoryForMedicine(medicineId: Int): Flow<List<MedicineHistory>>

    // New: Delete all history entries for a specific medicine
    @Query("DELETE FROM medicine_history WHERE medicineId = :medicineId")
    suspend fun deleteHistoryForMedicine(medicineId: Int)
}

@Database(
    entities = [Medicine::class, MedicineHistory::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class) // Register the Type Converters
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicineDao(): MedicineDao
    abstract fun medicineHistoryDao(): MedicineHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medicine_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
