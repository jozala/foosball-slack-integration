package pl.aetas.slack.control

import pl.aetas.slack.mapping.Player
import pl.aetas.slack.mapping.UnknownPushqUsername
import pl.aetas.slack.mapping.UserMappingService
import pl.aetas.slack.pushq.PushqSystem

class PlayersLookupStateManager(private val userMappingService: UserMappingService,
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
                    "Someone else is already looking for players. Use \"/foos +1\" instead.")
        }
        return SlackResponse(SlackResponseType.in_channel, "+$slackUsername is looking for 3 more players")
    }

    fun addPlayerByPushqUsername(pushqUsername: String): SlackResponse {
        val player = userMappingService.getPlayerByPushqUsername(pushqUsername)
        if (player == null) {
            return SlackResponse(SlackResponseType.ephemeral,
                    "User $pushqUsername is not registered. Ask user to register with \"/foos register [username]\".")
        }

        return addPlayer(player);
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
            lookupState.addPlayer(player);
        } catch (e: IllegalStateChangeException) {
            return SlackResponse(SlackResponseType.ephemeral,
                    "ERROR: No one has started to look for players. Start with \"/foos\".")
        } catch (e: IllegalAddPlayerException) {
            return SlackResponse(SlackResponseType.ephemeral,
                    "ERROR: ${player.slackUsername} is already added.")
        }

        if (lookupState.state() == PlayersLookupState.State.FINISHED) {
            val teams = teamsCalculator.calculateTeams(pushqSystem.ranking(), lookupState.players)
            val response = SlackResponse(SlackResponseType.in_channel,
                    "Let's play a game! ${teams.first} : ${teams.second}")
            lookupState.reset();
            return response
        }

        return SlackResponse(SlackResponseType.in_channel, "+${player.slackUsername} joined the game")
    }

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
        return SlackResponse(SlackResponseType.in_channel, "-${player.slackUsername} will not play")
    }

    fun reset(slackUsername: String): SlackResponse {
        lookupState.reset()
        return SlackResponse(SlackResponseType.in_channel, "Game cancelled by $slackUsername")
    }
}