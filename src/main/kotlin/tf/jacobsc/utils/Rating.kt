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

fun showFullRatingWithGames(rating: Rating, games: Int): String {
    val gamesColor = gameCountColor(games)
    return rating.displayFullRating() + " &b($gamesColor$games&b)"
}

fun Rating.displayFullRating(): String {
    val conservative = conservativeRating.roundToInt()
    val mean = mean.roundToInt()
    val dev = (conservativeStandardDeviationMultiplier * standardDeviation).roundToInt()
    return "$conservative ($mean±$dev)"
}

fun Player.checkForTeamAbandonment() {
    val isTournament = GameSettings.getBoolean("Tournament")
    val gameIsRunning =
        World.getWorld().gameMode.tournamentGameStarted && !World.getWorld().gameMode.voting
    val playerWasOnATeam = team >= 0

    if (isTournament && gameIsRunning && playerWasOnATeam) {
        val ratingSystem = RatingSystem(RatingType.Team)
        val (team1, team2) = separatePlayers()

        if (team1.isEmpty() || team2.isEmpty()) return

        val newPlayerRatings = when (team) {
            team1.first().team -> ratingSystem.rateMatch(team2, (team1 + this).distinct())
            team2.first().team -> ratingSystem.rateMatch(team1, (team2 + this).distinct())
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

    return RatingSystem(RatingType.Team).matchQuality(team1, team2)
}

private fun rateMatch(winningTeam: Int, type: RatingType) {
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

    RatingSystem(type).setRatings(winningPlayers, losingPlayers)
}

fun rateTeamMatch(winningTeam: Int) {
    rateMatch(winningTeam, RatingType.Team)
}

fun rateCasualMatch(winningTeam: Int) {
    rateMatch(winningTeam, RatingType.Casual)
}
