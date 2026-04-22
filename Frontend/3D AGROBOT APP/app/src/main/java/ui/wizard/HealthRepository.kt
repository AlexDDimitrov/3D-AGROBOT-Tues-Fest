package ui.wizard

import androidx.compose.runtime.Composable
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
class ReportRepository {
    private val baseUrl = "https://3d-agrobot-tues-fest-production.up.railway.app"

    fun getReports(token: String): List<ReportData> {
        val connection = (URL("$baseUrl/report/list").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Authorization", "Bearer $token")
            connectTimeout = 5000
            readTimeout = 5000
        }
        val response = connection.inputStream.bufferedReader().readText()
        connection.disconnect()

        val json = JSONObject(response)
        val reportsArray = json.getJSONArray("reports")

        return (0 until reportsArray.length()).map { i ->
            val reports = reportsArray.getJSONObject(i)
            val issuesArr = reports.getJSONArray("issues")
            val recsArr = reports.getJSONArray("recommendations")
            ReportData(
                id = reports.getInt("id"),
                plantType = if (reports.isNull("plant_type")) null else reports.getString("plant_type"),
                health = reports.getString("health"),
                hasPlant = reports.getInt("has_plant") == 1,
                issues = (0 until issuesArr.length()).map {
                    issuesArr.getString(it)
                                                          },
                recommendations = (0 until recsArr.length()).map {
                    recsArr.getString(it)
                                                                 },
                receivedAt = if (reports.has("received_at")) reports.getString("received_at") else null
            )
        }
    }
}
