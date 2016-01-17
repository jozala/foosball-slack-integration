package pl.aetas.slack.foos.command

import pl.aetas.slack.foos.state.PlayersLookupStateManager
import pl.aetas.slack.foos.state.SlackResponse

class CommandParser(val stateManager: PlayersLookupStateManager) {

    fun parseCommand(text: String): (slackUsername: String) -> SlackResponse {
        if (text.isBlank()) {
            return { slackUsername -> stateManager.startLookingForPlayers(slackUsername) }
        }

        if (text == "+1") {
            return { slackUsername -> stateManager.addPlayerBySlackUsername(slackUsername) }
        }

        if (text.startsWith("+")) {
            val pushqUsername = text.substring(1)
            return { slackUsername -> stateManager.addPlayerByPushqUsername(pushqUsername) }
        }

        if (text == "-1") {
            return { slackUsername -> stateManager.removePlayerBySlackUsername(slackUsername) }
        }

        if (text.startsWith("-")) {
            val pushqUsername = text.substring(1)
            return { slackUsername -> stateManager.removePlayerByPushqUsername(pushqUsername) }
        }

        if (text == "reset") {
            return { slackUsername -> stateManager.reset(slackUsername) }
        }

        if (text.startsWith("register")) {
            val splitCommand = text.split(" ")
            return { slackUsername -> stateManager.registerUser(slackUsername, splitCommand[1]) }
        }
        throw UnknownCommandException("Unknown command: $text")
    }
}

class UnknownCommandException(message: String): Exception(message)