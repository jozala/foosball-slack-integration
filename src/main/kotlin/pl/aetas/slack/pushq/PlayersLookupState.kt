package pl.aetas.slack.pushq

class PlayersLookupState {

    var state: State = State.NONE
    val players: MutableList<Player> = arrayListOf()

    @Synchronized
    fun start(player: Player) {
        if (state == State.LOOKING) {
            throw IllegalStateChangeException("Lookup is already started. Unable to start again.")
        }
        state = State.LOOKING
        players.add(player)
    }

    enum class State {
        NONE, LOOKING
    }
}

class IllegalStateChangeException(message: String): Exception(message)