package pl.aetas.slack.pushq

class IntegrationController(private val userMappingService: UserMappingService,
                            private val lookupState: PlayersLookupState,
                            private val pushqSystem: PushqSystem,
                            private val teamsCalculator: TeamsCalculator) {

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
        return SlackResponse(SlackResponseType.in_channel, "$slackUsername is looking for 3 more players")
    }

    fun addPlayerByPushqUsername(pushqUsername: String): SlackResponse {
        val player = userMappingService.getPlayerByPushqUsername(pushqUsername)
        if (player == null) {
            return SlackResponse(SlackResponseType.ephemeral,
                    "User $pushqUsername is not registered. Ask user to register with \"/foos register [username]\".")
        }

        return addPlayer(player);
    }

    // TODO throw exception when same player tries to add multiple times
    fun addPlayerBySlackUsername(slackUsername: String): SlackResponse {
        val player = userMappingService.getPlayerBySlackUsername(slackUsername)

        if (player == null) {
            return SlackResponse(SlackResponseType.ephemeral,
                    "You are not registered yet. Register with \"/foos register [your_username]\".")
        }

        return addPlayer(player)
    }

    private fun addPlayer(player: Player): SlackResponse {
        try {
            lookupState.addPlayer(player);
        } catch (e: IllegalStateChangeException) {
            return SlackResponse(SlackResponseType.ephemeral,
                    "ERROR: No one has started to look for players. Start with \"/foos\".")
        } catch (e: IllegalAddPlayerException) {
            return SlackResponse(SlackResponseType.ephemeral,
                    "ERROR: ${player.slackUsername} is already added.")
        }

        if (lookupState.state == PlayersLookupState.State.FINISHED) {
            val teams = teamsCalculator.calculateTeams(pushqSystem.ranking(), lookupState.players)
            val response = SlackResponse(SlackResponseType.in_channel,
                    "Let's play a game! ${teams.first} : ${teams.second}")
            lookupState.reset();
            return response
        }

        return SlackResponse(SlackResponseType.in_channel, "${player.slackUsername} joined the game")
    }

    fun removeUser(slackUsername: String): SlackResponse {
        TODO()
    }

    fun reset(): SlackResponse {
        TODO()

    }
}