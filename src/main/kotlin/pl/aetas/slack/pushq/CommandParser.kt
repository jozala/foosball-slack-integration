package pl.aetas.slack.pushq

class CommandParser(val controller: IntegrationController) {

    fun parseCommand(text: String): (slackUsername: String) -> SlackResponse {
        if (text.isBlank()) {
            return { slackUsername -> controller.startLookingForPlayers(slackUsername) }
        }

        if (text == "+1") {
            return { slackUsername -> controller.addPlayerBySlackUsername(slackUsername) }
        }

        if (text.startsWith("+")) {
            val pushqUsername = text.substring(1)
            return { slackUsername -> controller.addPlayerByPushqUsername(pushqUsername) }
        }

        if (text.startsWith("register")) {
            val splitCommand = text.split(" ")
            return { slackUsername -> controller.registerUser(slackUsername, splitCommand[1]) }
        }
        throw UnknownCommandException("Unknown command: $text")
    }
}

class UnknownCommandException(message: String): Exception(message)