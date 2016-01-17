package pl.aetas.slack.state

import spock.lang.Specification

class PlayersLookupStateFactoryTest extends Specification {

  private PlayersLookupStateManagerFactory factory

  void setup() {
    factory = new PlayersLookupStateManagerFactory()
  }

  def "should create different state object for each channel"() {
    when:
    def stateForChannel1 = factory.playersLookupState('channel1')
    def stateForChannel2 = factory.playersLookupState('channel2')
    then:
    stateForChannel1 != stateForChannel2
  }

  def "should return same state object for given channel name"() {
    when:
    def stateForChannel1Ref1 = factory.playersLookupState('channel1')
    def stateForChannel1Ref2 = factory.playersLookupState('channel1')
    then:
    stateForChannel1Ref1.is(stateForChannel1Ref2)

  }
}
