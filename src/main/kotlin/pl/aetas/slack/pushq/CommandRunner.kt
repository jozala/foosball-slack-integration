package pl.aetas.slack.pushq

class CommandRunner(private val commandParser: CommandParser) {

    fun run(slackUsername: String, command: String): SlackResponse {
        val action = commandParser.parseCommand(command)
        return action(slackUsername);
    }
}