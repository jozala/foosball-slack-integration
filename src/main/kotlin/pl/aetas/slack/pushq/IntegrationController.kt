package pl.aetas.slack.pushq

class IntegrationController(private val userMappingService: UserMappingService) {

    fun registerUser(slackUsername: String, pushqUsername: String): SlackResponse {
        userMappingService.addUserMapping(slackUsername, pushqUsername)
        return SlackResponse(SlackResponseType.ephemeral, "You have been registered as: $pushqUsername", emptyList())
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