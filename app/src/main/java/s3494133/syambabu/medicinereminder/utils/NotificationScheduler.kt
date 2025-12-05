package s3494133.syambabu.medicinereminder.utils


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar

import android.app.AlarmManager
import s3494133.syambabu.medicinereminder.data.AppDatabase
import s3494133.syambabu.medicinereminder.data.Medicine


object NotificationScheduler {
    const val CHANNEL_ID = "medicine_reminder_channel"
    const val CHANNEL_NAME = "Medicine Reminders"
    const val CHANNEL_DESCRIPTION = "Notifications for scheduled medicine intake"

    const val REFILL_CHANNEL_ID = "medicine_refill_channel"
    const val REFILL_CHANNEL_NAME = "Medicine Refill Reminders"
    const val REFILL_CHANNEL_DESCRIPTION = "Notifications when medicine supply is low"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val reminderChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = CHANNEL_DESCRIPTION
            }
            notificationManager.createNotificationChannel(reminderChannel)

            val refillChannel = NotificationChannel(REFILL_CHANNEL_ID, REFILL_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = REFILL_CHANNEL_DESCRIPTION
            }
            notificationManager.createNotificationChannel(refillChannel)
        }
    }

    // New helper function to check if SCHEDULE_EXACT_ALARM permission is granted
    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 (API 31) and above
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true // Permission not required or granted at install time for older Android versions
        }
    }

    fun scheduleAllNotifications(context: Context, medicine: Medicine) {
        // Cancel existing alarms for this medicine first to avoid duplicates
        cancelAllNotifications(context, medicine.id)

        // Schedule daily dose reminders
        medicine.times.forEachIndexed { index, timeString ->
            scheduleMedicineDoseNotification(context, medicine, timeString, index)
        }

        // Schedule refill reminder if applicable
        if (medicine.totalQuantity != null && medicine.refillThreshold != null && medicine.currentQuantity != null) {
            if (medicine.currentQuantity <= medicine.refillThreshold) {
                scheduleRefillNotification(context, medicine)
            }
        }
    }

    fun scheduleMedicineDoseNotification(context: Context, medicine: Medicine, timeString: String, timeIndex: Int) {
        // Only attempt to schedule if the permission is granted
        if (!canScheduleExactAlarms(context)) {
            Toast.makeText(context, "Please grant 'Alarms & reminders' permission for exact reminders.", Toast.LENGTH_LONG).show()
            return // Exit if permission is not granted
        }

        val requestCode = medicine.id * 1000 + timeIndex // Unique request code for each dose time

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "ACTION_MEDICINE_REMINDER"
            putExtra("medicine_id", medicine.id)
            putExtra("medicine_name", medicine.name)
            putExtra("medicine_dosage", medicine.dosage)
            putExtra("medicine_time", timeString)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // Use FLAG_IMMUTABLE for API 31+
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val nextAlarmTimeMillis = calculateNextAlarmTime(medicine, timeString)

        if (nextAlarmTimeMillis > 0) {
            alarmManager.setExactAndAllowWhileIdle( // Requires SCHEDULE_EXACT_ALARM permission on API 31+
                AlarmManager.RTC_WAKEUP,
                nextAlarmTimeMillis,
                pendingIntent
            )
            Toast.makeText(context, "Reminder set for ${medicine.name} at $timeString", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Could not schedule reminder for ${medicine.name} at $timeString", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scheduleRefillNotification(context: Context, medicine: Medicine) {
        // Only attempt to schedule if the permission is granted
        if (!canScheduleExactAlarms(context)) {
            Toast.makeText(context, "Please grant 'Alarms & reminders' permission for refill reminders.", Toast.LENGTH_LONG).show()
            return // Exit if permission is not granted
        }

        val requestCode = medicine.id * 100000 // Unique request code for refill (different range)

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "ACTION_REFILL_REMINDER"
            putExtra("medicine_id", medicine.id)
            putExtra("medicine_name", medicine.name)
            putExtra("current_quantity", medicine.currentQuantity)
            putExtra("refill_threshold", medicine.refillThreshold)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 9) // 9 AM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
        Toast.makeText(context, "Refill reminder set for ${medicine.name}", Toast.LENGTH_SHORT).show()
    }


    private fun calculateNextAlarmTime(medicine: Medicine, timeString: String): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            val timeParts = timeString.split(":")
            if (timeParts.size == 2) {
                set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                set(Calendar.MINUTE, timeParts[1].toInt())
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            } else {
                return -1 // Invalid time format
            }
        }

        val now = Calendar.getInstance()

        when (medicine.frequencyType) {
            "Daily" -> {
                if (calendar.before(now)) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            "Alternate Day" -> {
                if (calendar.before(now)) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            "Specific Days" -> {
                medicine.specificDays?.let { days ->
                    if (days.isEmpty()) return -1L

                    var nextValidTime: Long = -1L
                    var tempCalendar = calendar.clone() as Calendar

                    for (i in 0 until 8) {
                        if (tempCalendar.before(now)) {
                            tempCalendar.add(Calendar.DAY_OF_YEAR, 1)
                            tempCalendar.set(Calendar.HOUR_OF_DAY, timeString.split(":")[0].toInt())
                            tempCalendar.set(Calendar.MINUTE, timeString.split(":")[1].toInt())
                            tempCalendar.set(Calendar.SECOND, 0)
                            tempCalendar.set(Calendar.MILLISECOND, 0)
                        }

                        if (days.contains(tempCalendar.get(Calendar.DAY_OF_WEEK))) {
                            nextValidTime = tempCalendar.timeInMillis
                            break
                        }
                        tempCalendar.add(Calendar.DAY_OF_YEAR, 1)
                    }
                    if (nextValidTime == -1L) {
                        return -1L
                    }
                    calendar.timeInMillis = nextValidTime
                } ?: return -1L
            }
        }
        return calendar.timeInMillis
    }


    fun cancelAllNotifications(context: Context, medicineId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for (i in 0 until 10) {
            val requestCode = medicineId * 1000 + i
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = "ACTION_MEDICINE_REMINDER"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let { alarmManager.cancel(it) }
        }

        val refillRequestCode = medicineId * 100000
        val refillIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = "ACTION_REFILL_REMINDER"
        }
        val refillPendingIntent = PendingIntent.getBroadcast(
            context,
            refillRequestCode,
            refillIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        refillPendingIntent?.let { alarmManager.cancel(it) }

//        Toast.makeText(context, "All reminders cancelled for ID $medicineId", Toast.LENGTH_SHORT).show()
    }
}

class NotificationReceiver : BroadcastReceiver() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NotificationReceiver", "onReceive: Action = ${intent.action}")

        val pendingResult: PendingResult = goAsync()

        coroutineScope.launch {
            try {val database = AppDatabase.getDatabase(context.applicationContext)
                val medicineDao = database.medicineDao()

                when (intent.action) {
                    "ACTION_MEDICINE_REMINDER" -> {
                        val medicineId = intent.getIntExtra("medicine_id", 0)
                        val medicineName = intent.getStringExtra("medicine_name") ?: "Medicine"
                        val medicineDosage = intent.getStringExtra("medicine_dosage") ?: ""
                        val medicineTime = intent.getStringExtra("medicine_time") ?: ""

                        Log.d("NotificationReceiver", "Medicine Reminder: $medicineName at $medicineTime (ID: $medicineId)")

                        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        val builder = NotificationCompat.Builder(context, NotificationScheduler.CHANNEL_ID)
                            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                            .setContentTitle("Time for your medicine!")
                            .setContentText("$medicineName ($medicineTime): Take $medicineDosage")
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setAutoCancel(true)

                        notificationManager.notify(medicineId + medicineTime.hashCode(), builder.build())

                        // Reschedule the next alarm for this medicine
                        medicineDao.getMedicineById(medicineId).collect { medicine ->
                            medicine?.let {
                                // Ensure the timeString exists in the medicine's times list
                                val timeIndex = it.times.indexOf(medicineTime)
                                if (timeIndex != -1) {
                                    NotificationScheduler.scheduleMedicineDoseNotification(context, it, medicineTime, timeIndex)
                                } else {
                                    Log.e("NotificationReceiver", "Scheduled time $medicineTime not found in medicine ID $medicineId's times list.")
                                }
                            } ?: Log.e("NotificationReceiver", "Medicine with ID $medicineId not found for rescheduling.")
                        }
                    }
                    "ACTION_REFILL_REMINDER" -> {
                        val medicineId = intent.getIntExtra("medicine_id", 0)
                        val medicineName = intent.getStringExtra("medicine_name") ?: "Medicine"
                        val currentQuantity = intent.getIntExtra("current_quantity", 0)
                        val refillThreshold = intent.getIntExtra("refill_threshold", 0)

                        Log.d("NotificationReceiver", "Refill Reminder: $medicineName, Qty: $currentQuantity (ID: $medicineId)")

                        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        val builder = NotificationCompat.Builder(context, NotificationScheduler.REFILL_CHANNEL_ID)
                            .setSmallIcon(android.R.drawable.ic_dialog_alert)
                            .setContentTitle("Medicine Refill Alert!")
                            .setContentText("$medicineName is low. Only $currentQuantity doses left. Refill soon!")
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setAutoCancel(true)

                        notificationManager.notify(medicineId + 99999, builder.build())
                    }
                    Intent.ACTION_BOOT_COMPLETED -> {
                        Log.d("NotificationReceiver", "BOOT_COMPLETED received. Rescheduling all alarms.")
                        medicineDao.getAllMedicines().collect { medicines ->
                            medicines.forEach { medicine ->
                                NotificationScheduler.scheduleAllNotifications(context, medicine)
                            }
                            Log.d("NotificationReceiver", "Rescheduled ${medicines.size} medicines after boot.")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("NotificationReceiver", "Error in onReceive: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
