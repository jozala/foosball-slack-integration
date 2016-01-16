package pl.aetas.slack.pushq

class IntegrationController(private val userMappingService: UserMappingService) {

    fun registerUser(slackUsername: String, pushqUsername: String): SlackResponse {
        try {
            userMappingService.addUserMapping(slackUsername, pushqUsername)
        } catch(e: UnknownPushqUsername) {
            return SlackResponse(SlackResponseType.ephemeral, "ERROR: user \"$pushqUsername\" has not been found in PushQ system")
        }
        return SlackResponse(SlackResponseType.ephemeral, "You have been registered as: $pushqUsername")
    }

    fun startLookingForPlayers(slackUsername: String): SlackResponse {
        TODO()
    }

    fun addUser(slackUsername: String): SlackResponse {
        TODO()
    }

    fun removeUser(slackUsername: String): SlackResponse {
        TODO()
    }

    fun reset(): SlackResponse {
        TODO()
    }
}