data class DrugData(
    val meta: Meta,
    val results: List<DrugLabel>
)
data class Meta(val disclaimer: String, val results: Results)
data class Results(val skip: Int, val limit: Int, val total: Int)
data class DrugLabel(
    // List based on response
    val spl_product_data_elements: List<String>?,
    val recent_major_changes: List<String>?,
    val boxed_warning: List<String>?,
    val indications_and_usage: List<String>?,
    val dosage_and_administration: List<String>?, // List based on response
    val dosage_forms_and_strengths: List<String>?,
    val contraindications: List<String>?,

    val warnings_and_cautions: List<String>?,
    val adverse_reactions: List<String>?,
    val drug_interactions: List<String>?,
    val overdosage: List<String>?,
    val description: List<String>?, // List based on response
    val openfda: OpenFDA
)
data class OpenFDA(
    val brand_name: List<String>?,
    val generic_name: List<String>?,
    val manufacturer_name: List<String>?,
    val product_type: List<String>?
)