package ui.wizard

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class GardenRepository {

    private val baseUrl = "https://3d-agrobot-production.up.railway.app"

    fun getGardens(token: String): List<GardenData> {
        val url = URL("$baseUrl/garden/list")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Authorization", "Bearer $token")
        connection.connectTimeout = 5000
        connection.readTimeout = 5000

        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val response = stream.bufferedReader().use { it.readText() }
        connection.disconnect()

        val list = mutableListOf<GardenData>()

        try {
            val root = JSONObject(response)

            if (root.has("gardens")) {
                val arr = root.getJSONArray("gardens")

                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        GardenData(
                            id = obj.getInt("id"),
                            garden_name = obj.optString("garden_name", "Unknown"),
                            garden_width = obj.optInt("garden_width", 0),
                            garden_height = obj.optInt("garden_height", 0),
                            path_width = obj.optInt("path_width", 0),
                            number_beds = obj.optInt("number_beds", 0),
                            plant = obj.optString("plant", ""),
                            plantsNum = obj.optInt("number_of_plants", 0)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return list
    }

    fun createGarden(token: String, data: GardenData): Int {
        val url = URL("$baseUrl/garden/create")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer $token")
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        connection.doOutput = true

        val body = JSONObject().apply {
            put("garden_name", data.garden_name)
            put("garden_width", data.garden_width)
            put("garden_height", data.garden_height)
            put("path_width", data.path_width)
            put("number_beds", data.number_beds)
            put("plant", data.plant)
            put("number_of_plants", data.plantsNum)
        }.toString()

        connection.outputStream.use {
            it.write(body.toByteArray())
        }
        val code = connection.responseCode
        connection.disconnect()
        return code
    }

    fun editGarden(token: String, id: Int, data: GardenData): Int {
        val url = URL("$baseUrl/garden/edit/$id")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "PUT"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer $token")
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        connection.doOutput = true

        val body = JSONObject().apply {
            put("garden_name", data.garden_name)
            put("garden_width", data.garden_width)
            put("garden_height", data.garden_height)
            put("path_width", data.path_width)
            put("number_beds", data.number_beds)
            put("plant", data.plant)
            put("number_of_plants", data.plantsNum)
        }.toString()

        connection.outputStream.use { it.write(body.toByteArray()) }
        val code = connection.responseCode
        connection.disconnect()
        return code
    }
}