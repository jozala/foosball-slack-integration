package pl.aetas.slack.foos.state

import pl.aetas.slack.foos.mapping.Player

class PlayersLookupState {

    fun state(): State = when (players.size) {
            0 -> State.CLEAN
            in 1..3 -> State.LOOKING
            else -> State.FINISHED
        }

    val players: MutableList<Player> = arrayListOf()

    @Synchronized
    fun start(player: Player) {
        if (state() != State.CLEAN) {
            throw IllegalStateChangeException("Lookup is already started. Unable to start again.")
        }
        players.add(player)
    }

    enum class State {
        CLEAN, LOOKING, FINISHED
    }

    @Synchronized
    fun addPlayer(player: Player) {
        if (state() != State.LOOKING) {
            throw IllegalStateChangeException("Lookup is not started. Start it first.")
        }
        if (players.contains(player)) {
            throw IllegalAddPlayerException("Player already on list. Cannot be added twice.")
        }
        players.add(player);
    }

    @Synchronized
    fun reset() {
        players.clear()
    }

    @Synchronized
    fun removePlayer(player: Player) {
        if (state() != State.LOOKING) {
            throw IllegalStateChangeException("Current state does does not allow removing player")
        }
        if (!players.contains(player)) {
            throw IllegalRemovePlayerException("Player ${player.slackUsername} has is not on the players list")
        }
        players.remove(player);
    }
}

class IllegalStateChangeException(message: String): Exception(message)
class IllegalAddPlayerException(message: String): Exception(message)
class IllegalRemovePlayerException(message: String): Exception(message)