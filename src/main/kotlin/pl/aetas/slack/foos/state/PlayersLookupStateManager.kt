package pl.aetas.slack.foos.state

import pl.aetas.slack.foos.ApplicationProperties
import pl.aetas.slack.foos.mapping.Player
import pl.aetas.slack.foos.mapping.UnknownPushqUsername
import pl.aetas.slack.foos.mapping.UserMappingService
import pl.aetas.slack.foos.pushq.PushqSystem
import kotlin.random.Random

class PlayersLookupStateManager(private val userMappingService: UserMappingService,
                                private val lookupState: PlayersLookupState,
                                private val pushqSystem: PushqSystem,
                                private val teamsCalculator: TeamsCalculator) {

    val PUSHQ_URL_WEB_REGISTER = ApplicationProperties.get("pushq.url.web.register")


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
            lookupState.start(player)
        } catch (e: IllegalStateChangeException) {
            return SlackResponse(SlackResponseType.ephemeral,
                    "Someone else is already looking for players. Use \"/foos +1\" instead.")
        }
        return SlackResponse(SlackResponseType.in_channel,
                "+$slackUsername is looking for 3 more players\n<!group> Join with \"/foos +1\"")
    }

    fun addPlayerByPushqUsername(pushqUsername: String): SlackResponse {
        val player = userMappingService.getPlayerByPushqUsername(pushqUsername)
        if (player == null) {
            return SlackResponse(SlackResponseType.ephemeral,
                    "User $pushqUsername is not registered. Ask user to register with \"/foos register [username]\".")
        }

        return addPlayer(player)
    }

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
            lookupState.addPlayer(player)
        } catch (e: IllegalStateChangeException) {
            return SlackResponse(SlackResponseType.ephemeral,
                    "ERROR: No one has started to look for players. Start with \"/foos\".")
        } catch (e: IllegalAddPlayerException) {
            return SlackResponse(SlackResponseType.ephemeral,
                    "ERROR: ${player.slackUsername} is already added.")
        }

        if (lookupState.state() == PlayersLookupState.State.FINISHED) {
            var response: SlackResponse
            try {
                val teams = teamsCalculator.calculateTeams(pushqSystem.ranking(), lookupState.players)
                val redPlayers = teams.first.players.map { it.pushqUsername }
                val bluePlayers = teams.second.players.map { it.pushqUsername }
                val teamWithBall = Random.nextInt(0, 2)
                val playersAsQueryParams = "playerRed1=${redPlayers.get(0)}&playerRed2=${redPlayers.get(1)}" +
                        "&playerBlue1=${bluePlayers.get(0)}&playerBlue2=${bluePlayers.get(1)}"
                val slackPlayers: String = teams.first.players.map { it.slackUsername }.joinToString { "<@$it> " } + teams.second.players.map { it.slackUsername }.joinToString { "<@$it> " }
                response = SlackResponse(SlackResponseType.in_channel,
                        "Let's play a game! ${insertBall(teamWithBall, 0)} ${teams.first} : ${teams.second} ${insertBall(teamWithBall, 1)}\n" +
                                "$slackPlayers: go go go!\n" +
                                "Have you won? Insert result " +
                                "<${PUSHQ_URL_WEB_REGISTER}?${playersAsQueryParams}|here>.")
                lookupState.reset()
            } catch (e: Exception) {
                val slackPlayers: String = lookupState.players.map { it.slackUsername }.joinToString { "<@$it> " }
                response = SlackResponse(SlackResponseType.in_channel,
                        ":electric_plug: Just play... randomly: $slackPlayers\n" +
                                "Pushq's server is gone :coffin:\n" +
                                "Have you won? Write result on whiteboard!")
                lookupState.reset()
            }

            return response
        }

        return SlackResponse(SlackResponseType.in_channel,
                "+${player.slackUsername} joined the game (${listPlayers(lookupState.players)}).\n" +
                        "${lookupState.playersMissing()} more needed!")
    }

    private fun insertBall(teamWithBall: Int, teamNumber: Int): String {
        return if (teamWithBall == teamNumber) ":football:" else ""
    }

    private fun listPlayers(players: Collection<Player>) = players.map { it.pushqUsername }.joinToString(", ")

    fun removePlayerBySlackUsername(slackUsername: String): SlackResponse {
        val player = userMappingService.getPlayerBySlackUsername(slackUsername)

        if (player == null) {
            return SlackResponse(SlackResponseType.ephemeral,
                    "You are not registered yet. Register with \"/foos register [your_username]\".")
        }

        return removePlayer(player)
    }

    fun removePlayerByPushqUsername(pushqUsername: String): SlackResponse {
        val player = userMappingService.getPlayerByPushqUsername(pushqUsername)
        if (player == null) {
            return SlackResponse(SlackResponseType.ephemeral,
                    "User $pushqUsername is not registered. Ask user to register with \"/foos register [username]\".")
        }
        return removePlayer(player)
    }

    private fun removePlayer(player: Player): SlackResponse {
        try {
            lookupState.removePlayer(player)
        } catch (e: IllegalStateChangeException) {
            return SlackResponse(SlackResponseType.ephemeral,
                    "ERROR: ${player.slackUsername} cannot be removed now. Either there are no players on the list or reset is performed.")
        } catch (e: IllegalRemovePlayerException) {
            return SlackResponse(SlackResponseType.ephemeral,
                    "ERROR: ${player.slackUsername} has not joined, so cannot be removed from players list.")
        }
        return SlackResponse(SlackResponseType.in_channel,
                "-${player.slackUsername} will not play (${listPlayers(lookupState.players)}).\n" +
                        "${lookupState.playersMissing()} more needed!")
    }

    fun reset(slackUsername: String): SlackResponse {
        lookupState.reset()
        return SlackResponse(SlackResponseType.in_channel, "Game cancelled by $slackUsername")
    }
}
