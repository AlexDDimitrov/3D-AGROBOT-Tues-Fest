package ui.wizard

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class StartRobotRequest {
    private val baseUrl = "https://3d-agrobot-production.up.railway.app"

    fun startRobot(token: String, gardenId: Int): Int {
        val body = """
            {"garden_id": $gardenId}
        """.trimIndent()

        val connection =
            (URL("$baseUrl/request/start").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $token")
                connectTimeout = 5000
                readTimeout = 5000
                doOutput = true
            }

        connection.outputStream.use {
            it.write(body.toByteArray())
        }

        val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
        val response = stream.bufferedReader().use {
            it.readText()
        }
        connection.disconnect()

        return JSONObject(response).getInt("result")
    }
    fun getStatus(token: String): Int? {
        val connection =
            (URL("$baseUrl/request/start").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $token")
                connectTimeout = 5000
                readTimeout = 5000
            }


        val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
        val response = stream.bufferedReader().use {
            it.readText()
        }
        connection.disconnect()

        val request = JSONObject(response).optJSONObject("request")
        val status = request?.getInt("status")
        return status
    }
}