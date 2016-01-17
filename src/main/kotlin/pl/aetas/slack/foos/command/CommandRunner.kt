package pl.aetas.slack.foos.command

import pl.aetas.slack.foos.state.SlackResponse

class CommandRunner(private val commandParser: CommandParser) {

    fun run(slackUsername: String, command: String): SlackResponse {
        val action = commandParser.parseCommand(command)
        return action(slackUsername);
    }
}