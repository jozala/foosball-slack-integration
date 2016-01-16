package pl.aetas.slack.pushq

class CommandParser(val controller: IntegrationController) {

    fun parseCommand(text: String): (slackUsername: String) -> SlackResponse {
        if (text.isBlank()) {
            return { slackUsername -> controller.startLookingForPlayers(slackUsername) }
        }
        val splitCommand = text.split(" ")
        if (text.startsWith("register")) {
            return { slackUsername -> controller.registerUser(slackUsername, splitCommand[1]) }
        }
        throw UnknownCommandException("Unknown command: $text")
    }
}

class UnknownCommandException(message: String): Exception(message)