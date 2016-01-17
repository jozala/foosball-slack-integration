package pl.aetas.slack.state

import pl.aetas.slack.mapping.Player

class TeamsCalculator {

    fun calculateTeams(ranking: List<String>, players: List<Player>): Pair<Team, Team> {

        players.sortedBy { player ->
            ranking.indexOfRaw(ranking.find { rankingUser -> rankingUser == player.pushqUsername})
        }

        return Pair(Team(players[0], players[3]), Team(players[1], players[2]))
    }
}

data class Team(val player1: Player, val player2: Player) {
    override fun toString(): String {
        return "${player1.pushqUsername} ${player2.pushqUsername}"
    }
}