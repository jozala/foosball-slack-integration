package pl.aetas.slack.pushq

class UserMappingService(private val pushqSystem: PushqSystem) {

    // TODO - keep it persisted somewhere
    val userMapBySlackUsername: MutableMap<String, String> = hashMapOf()
    val userMapByPushqUsername: MutableMap<String, String> = hashMapOf()


    fun addUserMapping(slackUsername: String, pushqUsername: String) {
        if (pushqSystem.users().contains(pushqUsername)) {
            userMapBySlackUsername.put(slackUsername, pushqUsername)
            userMapByPushqUsername.put(pushqUsername, slackUsername)
        } else {
            throw UnknownPushqUsername("Username: $pushqUsername has not been found in PushQ's system")
        }
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