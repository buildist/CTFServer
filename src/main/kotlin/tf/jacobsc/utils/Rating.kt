package tf.jacobsc.utils

import de.gesundkrank.jskills.GameInfo
import de.gesundkrank.jskills.IPlayer
import de.gesundkrank.jskills.ITeam
import de.gesundkrank.jskills.Rating
import de.gesundkrank.jskills.TrueSkillCalculator
import kotlin.math.roundToInt
import org.opencraft.server.model.Player
import org.opencraft.server.model.World

fun displayRating(rating: Rating): String = with(rating) {
    "$mean+/-$conservativeRating"
}

data class NewPlayerRating(
    val player: Player,
    val rating: Rating,
)

private class RatingTeam(
    private val players: MutableMap<IPlayer, Rating>,
) : ITeam, MutableMap<IPlayer, Rating> by players {
    constructor(players: List<Player>, rating: (Player) -> Rating) : this(
        players
            .associateWith(rating)
            .toMutableMap()
    )
}

object DuelRatingSystem : RatingSystem {
    override fun getRating(p: Player): Rating = p.duelRating

    override fun setRating(p: Player, r: Rating) {
        p.duelRating = r
    }
}

object TeamRatingSystem : RatingSystem {
    override fun getRating(p: Player): Rating = p.teamRating

    override fun setRating(p: Player, r: Rating) {
        p.teamRating = r
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

interface RatingSystem {
    fun getRating(p: Player): Rating
    fun setRating(p: Player, r: Rating)

    fun setRatings(winners: List<Player>, losers: List<Player>) =
        rateMatch(winners, losers).forEach { (player, rating) ->
            setRating(player, rating)
        }

    fun setRatings(winner: Player, loser: Player) = setRatings(listOf(winner), listOf(loser))

    private fun teams(team1: List<Player>, team2: List<Player>): List<ITeam> = listOf(
        RatingTeam(team1, this::getRating),
        RatingTeam(team2, this::getRating),
    )

    fun rateMatch(winner: Player, loser: Player) =
        rateMatch(listOf(winner), listOf(loser))

    fun rateMatch(winners: List<Player>, losers: List<Player>): List<NewPlayerRating> {
        val newRatings = TrueSkillCalculator.calculateNewRatings(
            gameInfo,
            teams(winners, losers),
            1, 2,
        )

        val allPlayers = winners + losers

        return allPlayers.map { player ->
            NewPlayerRating(player, newRatings[player] ?: getRating(player))
        }
    }

    fun matchQuality(p1: Player, p2: Player): Int =
        matchQuality(listOf(p1), listOf(p2))

    fun matchQuality(team1: List<Player>, team2: List<Player>): Int =
        (TrueSkillCalculator.calculateMatchQuality(gameInfo, teams(team1, team2)) * 100).roundToInt()

    companion object {
        // Sources
        // https://www.moserware.com/assets/computing-your-skill/The%20Math%20Behind%20TrueSkill.pdf
        // https://trueskill.org/


        // the initial mean of ratings.
        const val initialMean = 1000.0

        // the initial standard deviation of ratings. The recommended value is a third of initialMean
        const val initialStandardDevation = 350.0


        // In (2), TrueSkill  β defines the length of the “skill
        //chain.” If a game has a wide range of skills, then β will tell you how wide each link is in the skill chain.
        //This can also be thought of how wide (in terms of skill points) each skill class.
        //Similiarly, β tells us the number of skill points a person must have above someone else to identify an
        //80% probability of win against that person.
        //For example, if β is 4 then a player Alice with a skill of “30” will tend to win against Bob who has a skill of
        //“26” approximately 80% of the time.

        // Default distance that guarantees about 76% chance of winning.
        // The recommended value is a half of sigma.
        const val beta = 175.0

        // Without τ, the TrueSkill algorithm would always cause the player’s standard deviation ( ) term to shrink
        //and therefore become more certain about a player. Before skill updates are calculated, we add in to
        //the player’s skill variance ( ). This ensures that the game retains “dynamics.” That is, the τ parameter
        //15
        //determines how easy it will be for a player to move up and down a leaderboard. A larger τ will tend to
        //cause more volatility of player positions
        // the dynamic factor which restrains a fixation of rating. The recommended value is sigma per cent
        const val dynamicFactor = 10.0

        // can't draw in CTF, either win or lose
        const val drawProbability = 0.0

        val defaultRating = Rating(initialMean, initialStandardDevation)

        val gameInfo = GameInfo(
            initialMean,
            initialStandardDevation,
            beta,
            dynamicFactor,
            drawProbability,
        )
    }
}
