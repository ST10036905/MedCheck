data class DrugData(
    val meta: Meta,
    val results: List<DrugLabel>
)
data class Meta(val disclaimer: String, val results: Results)
data class Results(val skip: Int, val limit: Int, val total: Int)
data class DrugLabel(val id: String, val effective_time: String, val openfda: OpenFDA)
data class OpenFDA(val brand_name: List<String>)