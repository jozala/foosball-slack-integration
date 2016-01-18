package pl.aetas.slack.foos.pushq

import com.google.gson.Gson
import pl.aetas.slack.foos.ApplicationProperties
import java.net.URL
import java.util.*

open class PushqSystem {

    val gson: Gson = Gson()

    val PUSHQ_URL = ApplicationProperties.get("pushq.url")

    open fun users(): List<String> {
        val response = URL(PUSHQ_URL).readText()
        val stats: ArrayList<Map<String, String>> = gson.fromJson(response, typeLiteral<ArrayList<Map<String, String>>>())
        return stats.map { it.get("name")!! }
    }

    inline private fun <reified T: List<Map<String, String>>> typeLiteral() = T::class.java


    open fun ranking(): List<String> = users()
}