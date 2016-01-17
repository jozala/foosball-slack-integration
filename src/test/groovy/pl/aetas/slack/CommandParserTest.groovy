package pl.aetas.slack

import kotlin.Function
import pl.aetas.slack.command.CommandParser
import pl.aetas.slack.command.UnknownCommandException
import pl.aetas.slack.control.PlayersLookupState
import pl.aetas.slack.control.PlayersLookupStateManager
import pl.aetas.slack.control.TeamsCalculator
import pl.aetas.slack.mapping.UserMappingService
import pl.aetas.slack.pushq.PushqSystem
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

  def "should failed with exception when unknown command is given"() {
    when:
    commandParser.parseCommand('unknown sth')
    then:
    def exception = thrown(UnknownCommandException)
    exception.message == 'Unknown command: unknown sth'
  }

  def "should parse known command to runnable function"() {
    when:
    def action = commandParser.parseCommand('register username')
    then:
    action instanceof Function
  }
}
