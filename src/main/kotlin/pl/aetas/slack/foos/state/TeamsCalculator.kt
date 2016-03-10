package pl.aetas.slack.foos.state

import pl.aetas.slack.foos.mapping.Player

class TeamsCalculator {

    fun calculateTeams(ranking: List<String>, players: List<Player>): Pair<Team, Team> {

        val sortedPlayers = players.sortedBy { player ->
            ranking.indexOf(ranking.find { rankingUser -> rankingUser == player.pushqUsername })
        }

        return Pair(Team(sortedPlayers[0], sortedPlayers[3]), Team(sortedPlayers[1], sortedPlayers[2]))
    }
}


class Team(player1: Player, player2: Player) {

    val players: Set<Player> = setOf(player1, player2);

    override fun toString(): String = players.map { it.pushqUsername }.joinToString(" ")

    override fun equals(other: Any?): Boolean{
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Team

        if (players != other.players) return false

        return true
    }

    override fun hashCode(): Int{
        return players.hashCode()
    }


}