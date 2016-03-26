package pl.aetas.slack.foos.mapping

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pl.aetas.slack.foos.pushq.PushqSystem
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.FileReader
import java.io.FileWriter
import java.util.*

class UserMappingService(private val pushqSystem: PushqSystem) {

    val userMapBySlackUsername: MutableMap<String, String>
    val userMapByPushqUsername: MutableMap<String, String>

    val gson: Gson = Gson()
    val type = object : TypeToken<Map<String, String>>() {}.type

    init {
        val fileReader = BufferedReader(FileReader("config/players_mapping.json"))

        userMapBySlackUsername = gson.fromJson(fileReader, type)
        userMapByPushqUsername = userMapBySlackUsername.map { Pair(it.value, it.key) }.toMap().let { LinkedHashMap(it) }
        fileReader.close()
    }


    fun addUserMapping(slackUsername: String, pushqUsername: String) {
        if (pushqSystem.users().contains(pushqUsername)) {
            userMapBySlackUsername.put(slackUsername, pushqUsername)
            userMapByPushqUsername.put(pushqUsername, slackUsername)
            storeUserMap(userMapBySlackUsername)
        } else {
            throw UnknownPushqUsername("Username: $pushqUsername has not been found in PushQ's system")
        }
    }

    private fun storeUserMap(userMapBySlackUsernameToStore: Map<String, String>) {
        val mappingFile = BufferedWriter(FileWriter("config/players_mapping.json"))
        gson.toJson(userMapBySlackUsernameToStore, type, mappingFile)
        mappingFile.close()
    }

    fun getPlayerBySlackUsername(slackUsername: String): Player? {
        val pushqUsername = userMapBySlackUsername.get(slackUsername)
        if (pushqUsername != null) {
            return Player(slackUsername, pushqUsername)
        }
        return null;
    }

    fun getPlayerByPushqUsername(pushqUsername: String): Player? {
        val slackUsername = userMapByPushqUsername.get(pushqUsername)
        if (slackUsername != null) {
            return Player(slackUsername, pushqUsername)
        }
        return null;
    }
}

class UnknownPushqUsername(message: String): Exception(message)