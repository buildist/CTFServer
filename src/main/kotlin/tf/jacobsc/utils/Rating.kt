package tf.jacobsc.utils

import de.gesundkrank.jskills.Rating
import kotlin.math.roundToInt
import org.opencraft.server.game.impl.GameSettings
import org.opencraft.server.model.Player
import org.opencraft.server.model.World

enum class RatingType {
    Casual,
    Duel,
    Team;

    fun lowerName() = name.lowercase()
}

fun Rating.displayRating(): String = "${conservativeRating.roundToInt()}"

fun gameCountColor(count: Int): String = when {
    count < 10 -> "&0"
    count < 20 -> "&7"
    else -> "&7"
}

fun Rating.displayFullRating(): String {
    val conservative = conservativeRating.roundToInt()
    val mean = mean.roundToInt()
    val dev = (conservativeStandardDeviationMultiplier * standardDeviation).roundToInt()
    return "$conservative ($mean±$dev)"
}

fun Player.deductRatingForTeamAbandonmentIfTournamentRunningAndOnTeam() {
    val isTournament = GameSettings.getBoolean("Tournament")
    val gameIsRunning =
        World.getWorld().gameMode.tournamentGameStarted && !World.getWorld().gameMode.voting
    val playerWasOnATeam = team >= 0

    if (isTournament && gameIsRunning && playerWasOnATeam) {
        val (team1, team2) = separatePlayers()

        if (team1.isEmpty() || team2.isEmpty()) return

        val newPlayerRatings = when (team) {
            team1.first().team -> TeamRatingSystem.rateMatch(team2, (team1 + this).distinct())
            team2.first().team -> TeamRatingSystem.rateMatch(team1, (team2 + this).distinct())
            else -> return
        }

        val newRating = newPlayerRatings.firstOrNull { newRating ->
            newRating.player == this
        } ?: return

        setRating(RatingType.Team, newRating.rating)
    }
}

fun separatePlayers(): Pair<List<Player>, List<Player>> {
    val team1 = mutableListOf<Player>()
    val team2 = mutableListOf<Player>()

    World.getWorld().playerList.players.forEach { player ->
        if (player.team < 0) return@forEach

        if (player.team == 0) team1.add(player)
        else team2.add(player)
    }

    return team1 to team2
}

fun matchQuality(): Int {
    val (team1, team2) = separatePlayers()

    return TeamRatingSystem.matchQuality(team1, team2)
}

fun rateMatch(winningTeam: Int) {
    val winningPlayers = mutableListOf<Player>()
    val losingPlayers = mutableListOf<Player>()

    for (p in World.getWorld().playerList.players) {
        if (p.team == -1) {
            continue
        }

        if (p.team == winningTeam) {
            winningPlayers.add(p)
        } else {
            losingPlayers.add(p)
        }
    }

    TeamRatingSystem.setRatings(winningPlayers, losingPlayers)
}
