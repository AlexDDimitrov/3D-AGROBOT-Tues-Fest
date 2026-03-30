package ui.wizard

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class SignupData {
    fun register(
        first_name: String,
        last_name: String,
        email: String,
        password: String
    ): String {
        val url = URL("https://3d-agrobot-production.up.railway.app/auth/register")
        val connection = url.openConnection() as HttpURLConnection


        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        val body = JSONObject().apply {
            put("first_name", first_name)
            put("last_name", last_name)
            put("email", email)
            put("password", password)
        }.toString()

        connection.outputStream.write(body.toByteArray())
        val response = connection.inputStream.bufferedReader().readText()
        connection.disconnect()

        return response
    }
}