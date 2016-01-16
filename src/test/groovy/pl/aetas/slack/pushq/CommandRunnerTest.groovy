package pl.aetas.slack.pushq

import spock.lang.Specification

class CommandRunnerTest extends Specification {

  CommandRunner commandRunner
  UserMappingService userMappingService
  PlayersLookupState lookupState

  def pushqSystem = Mock(PushqSystem)

  void setup() {
    userMappingService = new UserMappingService(pushqSystem)
    lookupState = new PlayersLookupState()
    def integrationController = new IntegrationController(userMappingService, lookupState)
    def commandParser = new CommandParser(integrationController)
    commandRunner = new CommandRunner(commandParser)
    pushqSystem.users() >> ['existingUser', 'pushqUser1', 'pushqUser2']
  }

  def "should register user when command: register someUser"() {
    when:
    commandRunner.run('slackUser', 'register existingUser');
    then:
    userMappingService.getPlayerBySlackUsername('slackUser').pushqUsername == 'existingUser'
  }

  def "should not register user when given user name does not exist in PushQ system"() {
    when:
    commandRunner.run('slackUser', 'register nonExistingUser');
    then:
    userMappingService.getPlayerBySlackUsername('slackUser') == null
  }

  def "should give information about registered user for command: register someUser"() {
    when:
    def response = commandRunner.run('slackUser', 'register existingUser');
    then:
    response.text == "You have been registered as: existingUser"
  }

  def "should give error mesage when given user name does not exist in PushQ system"() {
    when:
    def response = commandRunner.run('slackUser', 'register nonExistingUser');
    then:
    response.text == 'ERROR: user "nonExistingUser" has not been found in PushQ system'
  }

  def "should start players lookup and add user when command without any params executed"() {
    given:
    userMappingService.addUserMapping("slackUser1", "pushqUser1")
    when:
    commandRunner.run('slackUser1', '');
    then:
    lookupState.state == PlayersLookupState.State.LOOKING
    lookupState.players.contains(new Player('slackUser1', 'pushqUser1'))
  }

  def "should return info about non-registered user when user is not registered but tries to start lookup"() {
    when:
    def response = commandRunner.run('nonRegisteredUser', '');
    then:
    response.responseType == SlackResponseType.ephemeral
    response.text == 'You are not registered yet. Register with "/foos register [your_username]".'
  }

  def "should return info about already started lookup when user is trying to start, but lookup already started"() {
    given:
    userMappingService.addUserMapping("slackUser1", "pushqUser1")
    userMappingService.addUserMapping("slackUser2", "pushqUser2")
    commandRunner.run('slackUser1', '');
    when:
    def response = commandRunner.run('slackUser2', '');
    then:
    response.responseType == SlackResponseType.ephemeral
    response.text == 'Someone else is already looking for players use "/foos +1" instead.'
  }
}
