package pl.aetas.slack.pushq

import kotlin.Function
import spock.lang.Specification

class CommandParserTest extends Specification {

  CommandParser commandParser

  void setup() {
    commandParser = new CommandParser(new IntegrationController());
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
