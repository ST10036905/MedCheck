import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface IOpenFDAApi {
    @GET("drug/label.json")
    fun getDrugInfo(
        @Query("search") search: String,
        @Query("limit") limit: Int,
        @Query("api_key") apiKey: String
    ): Call<DrugData>
}