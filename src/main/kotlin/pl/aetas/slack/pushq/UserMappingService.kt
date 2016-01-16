package pl.aetas.slack.pushq

class UserMappingService(private val pushqSystem: PushqSystem) {

    val userMap: MutableMap<String, String> = hashMapOf()


    fun addUserMapping(slackUsername: String, pushqUsername: String) {
        if (pushqSystem.users().contains(pushqUsername)) {
            userMap.put(slackUsername, pushqUsername)
        }
    }

    fun getPushqUsername(slackUsername: String): String? = userMap.get(slackUsername)
}