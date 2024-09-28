data class MedicationResponse(
    val results: List<MedicationDetails>
)

data class MedicationDetails(
    val generic_name: String,
    val dosage_and_administration: String,
    val indications_and_usage: String
    // Add other fields you need based on OpenFDA response
)
