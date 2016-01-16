package pl.aetas.slack.pushq

class IntegrationController(private val userMappingService: UserMappingService, val lookupState: PlayersLookupState) {

    fun registerUser(slackUsername: String, pushqUsername: String): SlackResponse {
        try {
            userMappingService.addUserMapping(slackUsername, pushqUsername)
        } catch(e: UnknownPushqUsername) {
            return SlackResponse(SlackResponseType.ephemeral, "ERROR: user \"$pushqUsername\" has not been found in PushQ system")
        }
        return SlackResponse(SlackResponseType.ephemeral, "You have been registered as: $pushqUsername")
    }

    fun startLookingForPlayers(slackUsername: String): SlackResponse {
        val player = userMappingService.getPlayerBySlackUsername(slackUsername)

        if (player == null) {
            return SlackResponse(SlackResponseType.ephemeral,
                    "You are not registered yet. Register with \"/foos register [your_username]\".")
        }
        try {
            lookupState.start(player);
        } catch (e: IllegalStateChangeException) {
            return SlackResponse(SlackResponseType.ephemeral,
                    "Someone else is already looking for players use \"/foos +1\" instead.")
        }
        return SlackResponse(SlackResponseType.in_channel, "")
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