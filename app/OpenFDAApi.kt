import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class OpenFDAApi {
    companion object {
        private const val BASE_URL = "https://api.fda.gov/drug/drugsfda.json"
        private const val API_KEY = "ly8sKkGIDMmEaQH9nCy1QMC8nqMAKFxVQdjII9Rd"

        // Function to create and return Retrofit instance
        fun getInstance(): OpenFDAService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(OpenFDAService::class.java)
        }
    }
}
