package pl.aetas.slack.foos

import org.wasabi.app.AppServer
import pl.aetas.slack.foos.command.CommandParser
import pl.aetas.slack.foos.command.CommandRunner
import pl.aetas.slack.foos.mapping.UserMappingService
import pl.aetas.slack.foos.pushq.PushqSystem
import pl.aetas.slack.foos.state.PlayersLookupStateManagerFactory
import pl.aetas.slack.foos.state.TeamsCalculator

fun main(args : Array<String>) {

    val pushqSystem = PushqSystem()
    val userMappingService = UserMappingService(pushqSystem)
    val playersLookupStateManagerFactory = PlayersLookupStateManagerFactory()
    val teamsCalculator = TeamsCalculator()


    val server = AppServer()

    server.post("/integration", {

        val channelId = it.request.bodyParams["channel_id"] as String
        val command = it.request.bodyParams["text"] as String
        val slackUsername = it.request.bodyParams["user_name"] as String

        val commandRunner: CommandRunner = commandRunner(
                channelId,
                playersLookupStateManagerFactory,
                pushqSystem,
                teamsCalculator,
                userMappingService)

        commandRunner.run(slackUsername, command)
    })

    server.start()
}

private fun commandRunner(channelId: String, playersLookupStateManagerFactory: PlayersLookupStateManagerFactory, pushqSystem: PushqSystem, teamsCalculator: TeamsCalculator, userMappingService: UserMappingService): CommandRunner {
    val playersLookupStateManager = playersLookupStateManagerFactory.playersLookupStateManager(channelId, userMappingService, pushqSystem, teamsCalculator)
    val commandParser = CommandParser(playersLookupStateManager)
    val commandRunner: CommandRunner = CommandRunner(commandParser)
    return commandRunner
}