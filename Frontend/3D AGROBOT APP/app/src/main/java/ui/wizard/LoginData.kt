package ui.wizard

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class LoginData {
    fun login(
        email: String,
        password: String
    ): String {
        val url = URL("https://3d-agrobot-production.up.railway.app/auth/login")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        val body = JSONObject().apply {
            put("email", email)
            put("password", password)
        }.toString()

        connection.outputStream.write(body.toByteArray())

        val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
        val response = stream.bufferedReader().readText()
        connection.disconnect()

        return response
    }
}