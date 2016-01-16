package pl.aetas.slack.pushq

class UserMappingService(private val pushqSystem: PushqSystem) {

    // TODO - keep it persisted somewhere
    val userMap: MutableMap<String, String> = hashMapOf()


    fun addUserMapping(slackUsername: String, pushqUsername: String) {
        if (pushqSystem.users().contains(pushqUsername)) {
            userMap.put(slackUsername, pushqUsername)
        } else {
            throw UnknownPushqUsername("Username: $pushqUsername has not been found in PushQ's system")
        }
    }

    fun getPlayerBySlackUsername(slackUsername: String): Player? {
        val pushqUsername = userMap.get(slackUsername)
        if (pushqUsername != null) {
            return Player(slackUsername, pushqUsername)
        }
        return null;
    }
}

class UnknownPushqUsername(message: String): Exception(message)