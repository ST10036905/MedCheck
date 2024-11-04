data class MedicationSchedule(
    val id: Int,
    val name: String,
    val dosage: String,
    val frequency: Long, // Interval in milliseconds, e.g., 8 hours
    val startTime: Long  // Epoch time in milliseconds
)
