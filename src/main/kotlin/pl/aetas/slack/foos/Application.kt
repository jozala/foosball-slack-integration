package pl.aetas.slack.foos

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import pl.aetas.slack.foos.command.CommandParser
import pl.aetas.slack.foos.command.CommandRunner
import pl.aetas.slack.foos.mapping.UserMappingService
import pl.aetas.slack.foos.pushq.PushqSystem
import pl.aetas.slack.foos.state.PlayersLookupStateManagerFactory
import pl.aetas.slack.foos.state.TeamsCalculator
import spark.*
import java.io.FileInputStream
import java.util.*

fun main(args : Array<String>) {

    val gson: Gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

    if (ApplicationProperties.isTrue("ssl.enabled")) {
        val sslPassword = ApplicationProperties.get("ssl.password")

        Spark.port(443);
        Spark.secure("ssl/MyDSKeyStore.jks", sslPassword, null, null);
    }

    Spark.post("/integration", MyRoute(), ResponseTransformer { gson.toJson(it) })
}

object ApplicationProperties {

    val properties: Properties = Properties()
    val inputStream: FileInputStream = FileInputStream("foos.properties")

    init {
        properties.load(inputStream)
    }

    fun get(key: String) = properties.getProperty(key)
    fun isTrue(key:String) = properties.getProperty(key).equals("true", true)
}

class MyRoute: Route {

    val pushqSystem = PushqSystem()
    val userMappingService = UserMappingService(pushqSystem)
    val playersLookupStateManagerFactory = PlayersLookupStateManagerFactory()
    val teamsCalculator = TeamsCalculator()


    override fun handle(request: Request, response: Response): Any? {
        val channelId = request.queryParams("channel_id")
        val command = request.queryParams("text")
        val slackUsername = request.queryParams("user_name")

        if (ApplicationProperties.isTrue("slack.token.required")) {
            if (request.queryParams("token") != ApplicationProperties.get("slack.token")) {
                response.status(401)
                return "Incorrect Slack token";
            }
        }

        val commandRunner: CommandRunner = commandRunner(
                channelId,
                playersLookupStateManagerFactory,
                pushqSystem,
                teamsCalculator,
                userMappingService)

        val slackResponse = commandRunner.run(slackUsername, command)

        response.status(200)
        response.type("application/json")
        return slackResponse
    }

}

private fun commandRunner(channelId: String, playersLookupStateManagerFactory: PlayersLookupStateManagerFactory, pushqSystem: PushqSystem, teamsCalculator: TeamsCalculator, userMappingService: UserMappingService): CommandRunner {
    val playersLookupStateManager = playersLookupStateManagerFactory.playersLookupStateManager(channelId, userMappingService, pushqSystem, teamsCalculator)
    val commandParser = CommandParser(playersLookupStateManager)
    val commandRunner: CommandRunner = CommandRunner(commandParser)
    return commandRunner
}