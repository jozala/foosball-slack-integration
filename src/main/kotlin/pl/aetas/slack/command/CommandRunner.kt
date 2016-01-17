package pl.aetas.slack.command

import pl.aetas.slack.command.CommandParser
import pl.aetas.slack.control.SlackResponse

class CommandRunner(private val commandParser: CommandParser) {

    fun run(slackUsername: String, command: String): SlackResponse {
        val action = commandParser.parseCommand(command)
        return action(slackUsername);
    }
}