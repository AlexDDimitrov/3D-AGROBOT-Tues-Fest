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
    fun getNotification(token: String): String? {
        val connection = (URL("$baseUrl/report/notifications").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Authorization", "Bearer $token")
        }
        val response = connection.inputStream.bufferedReader().readText()
        val json = JSONObject(response)

        if (json.getBoolean("has_notifications")) {
            val notifications = json.getJSONArray("notifications")
            val first = notifications.getJSONObject(0)

            val plantType = first.getString("plant_type")
            val issues = first.getJSONArray("issues")
            val issuesText = (0 until issues.length()).joinToString(", ") { issues.getString(it) }

            return "$plantType: $issuesText"
        }
        return null
    }
}
