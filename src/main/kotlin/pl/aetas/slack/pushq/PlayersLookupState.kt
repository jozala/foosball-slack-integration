package pl.aetas.slack.pushq

class PlayersLookupState {

    var state: State = State.CLEAN
        private set

    val players: MutableList<Player> = arrayListOf()

    @Synchronized
    fun start(player: Player) {
        if (state != State.CLEAN) {
            throw IllegalStateChangeException("Lookup is already started. Unable to start again.")
        }
        state = State.LOOKING
        players.add(player)
    }

    enum class State {
        CLEAN, LOOKING, FINISHED
    }

    @Synchronized
    fun addPlayer(player: Player) {
        if (state != State.LOOKING) {
            throw IllegalStateChangeException("Lookup is not started. Start it first.")
        }
        if (players.contains(player)) {
            throw IllegalAddPlayerException("Player already on list. Cannot be added twice.")
        }
        players.add(player);

        if (players.size == 4) {
            state = State.FINISHED
        }
    }

    @Synchronized
    fun reset() {
        players.clear()
        state = State.CLEAN
    }

    @Synchronized
    fun removePlayer(player: Player) {
        if (state != State.LOOKING) {
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