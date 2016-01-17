package pl.aetas.slack.state

import pl.aetas.slack.mapping.UserMappingService
import pl.aetas.slack.pushq.PushqSystem

class PlayersLookupStateManagerFactory {

    private val statesByChannel = hashMapOf<String, PlayersLookupState>()

    @Synchronized
    private fun playersLookupState(channelId: String): PlayersLookupState {
        if (statesByChannel.containsKey(channelId)) {
            return statesByChannel.get(channelId)!!
        }

        val newState = PlayersLookupState()
        statesByChannel.put(channelId, newState)
        return newState
    }

    public fun playersLookupStateManager(channelId: String,
                                         userMappingService: UserMappingService,
                                         pushqSystem: PushqSystem,
                                         teamsCalculator: TeamsCalculator): PlayersLookupStateManager {

        val playersLookupState = playersLookupState(channelId)
        return PlayersLookupStateManager(userMappingService, playersLookupState, pushqSystem, teamsCalculator)

    }
}