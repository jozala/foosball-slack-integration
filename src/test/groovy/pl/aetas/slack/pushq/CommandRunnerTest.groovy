package pl.aetas.slack.pushq

import spock.lang.Specification

class CommandRunnerTest extends Specification {

  CommandRunner commandRunner
  UserMappingService userMappingService

  def pushqSystem = Mock(PushqSystem)

  void setup() {
    userMappingService = new UserMappingService(pushqSystem)
    def integrationController = new IntegrationController(userMappingService)
    def commandParser = new CommandParser(integrationController)
    commandRunner = new CommandRunner(commandParser)
  }

  def "should register user when command: register someUser"() {
    given:
    pushqSystem.users() >> ['someUser']
    when:
    commandRunner.run('slackUser', 'register someUser');
    then:
    userMappingService.getPushqUsername('slackUser') == 'someUser'
  }

  def "should not register user when given user name does not exist in PushQ system"() {
    given:
    pushqSystem.users() >> ['existingUser']
    when:
    commandRunner.run('slackUser', 'register nonExistingUser');
    then:
    userMappingService.getPushqUsername('slackUser') == null
  }

  def "should give information about registered user for command: register someUser"() {
    given:
    pushqSystem.users() >> ['someUser']
    when:
    def response = commandRunner.run('slackUser', 'register someUser');
    then:
    response.text == "You have been registered as: someUser"
  }

  def "should give error mesage when given user name does not exist in PushQ system"() {
    given:
    pushqSystem.users() >> ['existingUser']
    when:
    def response = commandRunner.run('slackUser', 'register nonExistingUser');
    then:
    response.text == 'ERROR: user "nonExistingUser" has not been found in PushQ system'
  }
}
