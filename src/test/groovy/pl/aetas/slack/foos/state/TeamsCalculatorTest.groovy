package pl.aetas.slack.foos.state

import kotlin.Pair
import pl.aetas.slack.foos.mapping.Player
import spock.lang.Specification

class TeamsCalculatorTest extends Specification {

  TeamsCalculator teamsCalculator

  void setup() {
    teamsCalculator = new TeamsCalculator()
  }

  def "should make team from best and worst player in ranking"() {
    given:
    def ranking = ["p1", "p2", "p3", "p4", "p5", "p6"]


    def p1 = new Player("player1", "p1")
    def p3 = new Player("player3", "p3")
    def p4 = new Player("player4", "p4")
    def p6 = new Player("player6", "p6")

    when:
    Pair<Team, Team> teams = teamsCalculator.calculateTeams(ranking, [p1, p3, p6, p4])

    then:
    teams.first == new Team(p1, p6) || teams.first == new Team(p3, p4)
    teams.second == new Team(p1, p6) || teams.second == new Team(p3, p4)
  }


}
