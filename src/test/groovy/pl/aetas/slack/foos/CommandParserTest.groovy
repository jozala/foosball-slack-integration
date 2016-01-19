package pl.aetas.slack.foos
import kotlin.Function
import pl.aetas.slack.foos.command.CommandParser
import pl.aetas.slack.foos.mapping.UserMappingService
import pl.aetas.slack.foos.pushq.PushqSystem
import pl.aetas.slack.foos.state.PlayersLookupState
import pl.aetas.slack.foos.state.PlayersLookupStateManager
import pl.aetas.slack.foos.state.SlackResponse
import pl.aetas.slack.foos.state.SlackResponseType
import pl.aetas.slack.foos.state.TeamsCalculator
import spock.lang.Specification

class CommandParserTest extends Specification {

  CommandParser commandParser

  void setup() {
    PushqSystem pushqSystem = Mock(PushqSystem)
    commandParser = new CommandParser(
            new PlayersLookupStateManager(
                    new UserMappingService(pushqSystem),
                    new PlayersLookupState(),
                    pushqSystem,
                    new TeamsCalculator()
            )
    );
  }

  def "should return SlackResponse with unknown command information"() {
    when:
    def command = commandParser.parseCommand('unknown sth')
    then:
    command.invoke("slackUsername") == new SlackResponse(SlackResponseType.ephemeral, "Unknown command: unknown sth", [])
  }

  def "should parse known command to runnable function"() {
    when:
    def action = commandParser.parseCommand('register username')
    then:
    action instanceof Function
  }
}
