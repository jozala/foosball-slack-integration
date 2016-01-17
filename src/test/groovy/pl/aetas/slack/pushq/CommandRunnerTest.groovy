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
    def teamsCalculator = new TeamsCalculator()
    def integrationController = new IntegrationController(userMappingService, lookupState, pushqSystem, teamsCalculator)
    def commandParser = new CommandParser(integrationController)
    commandRunner = new CommandRunner(commandParser)
    pushqSystem.users() >> ['existingUser', 'pushqUser1', 'pushqUser2', 'pushqUser3', 'pushqUser4']
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
    def response = commandRunner.run('slackUser1', '');
    then:
    lookupState.state == PlayersLookupState.State.LOOKING
    lookupState.players.contains(new Player('slackUser1', 'pushqUser1'))
    response.responseType == SlackResponseType.in_channel
    response.text == 'slackUser1 is looking for 3 more players'
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

  def "should add player to lookup when +1 command executed"() {
    given:
    userMappingService.addUserMapping("slackUser1", "pushqUser1")
    userMappingService.addUserMapping("slackUser2", "pushqUser2")
    commandRunner.run('slackUser1', '')
    when:
    def response = commandRunner.run('slackUser2', '+1');
    then:
    lookupState.players == [new Player('slackUser1', 'pushqUser1'),
                            new Player('slackUser2', 'pushqUser2')]
    response.responseType == SlackResponseType.in_channel
    response.text == 'slackUser2 joined the game'
  }

  def "should add specified player to lookup when +[username] command executed"() {
    given:
    userMappingService.addUserMapping("slackUser1", "pushqUser1")
    userMappingService.addUserMapping("slackUser3", "pushqUser3")
    commandRunner.run('slackUser1', '')
    when:
    def response = commandRunner.run('slackUser1', '+pushqUser3');
    then:
    lookupState.players == [new Player('slackUser1', 'pushqUser1'),
                            new Player('slackUser3', 'pushqUser3')]
    response.responseType == SlackResponseType.in_channel
    response.text == 'slackUser3 joined the game'
  }

  def "should return error when trying to add same player twice"() {
    given:
    userMappingService.addUserMapping("slackUser1", "pushqUser1")
    userMappingService.addUserMapping("slackUser2", "pushqUser2")
    commandRunner.run('slackUser1', '')
    when:
    def response = commandRunner.run('slackUser1', '+1');
    then:
    response.responseType == SlackResponseType.ephemeral
    response.text == 'ERROR: slackUser1 is already added.'
  }

  def "should inform lookup is not started when +1 command executed but lookup not started"() {
    given:
    userMappingService.addUserMapping("slackUser2", "pushqUser2")
    when:
    def response = commandRunner.run('slackUser2', '+1');
    then:
    response.responseType == SlackResponseType.ephemeral
    response.text == 'ERROR: No one has started to look for players. Start with "/foos".'
  }

  def "should reset players when +1 command executed by 4th player"() {
    given:
    pushqSystem.ranking() >> ['pushqUser1', 'pushqUser2', 'pushqUser3', 'pushqUser4']
    userMappingService.addUserMapping("slackUser1", "pushqUser1")
    userMappingService.addUserMapping("slackUser2", "pushqUser2")
    userMappingService.addUserMapping("slackUser3", "pushqUser3")
    userMappingService.addUserMapping("slackUser4", "pushqUser4")
    commandRunner.run('slackUser1', '')
    when:
    commandRunner.run('slackUser2', '+1');
    commandRunner.run('slackUser3', '+1');
    def response = commandRunner.run('slackUser4', '+1');
    then:
    lookupState.players.isEmpty()
    lookupState.state == PlayersLookupState.State.CLEAN
    response.responseType == SlackResponseType.in_channel
    response.text == "Let's play a game! pushqUser1 pushqUser4 : pushqUser2 pushqUser3"
  }
}
