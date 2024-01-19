package tf.jacobsc.utils

import de.gesundkrank.jskills.GameInfo
import de.gesundkrank.jskills.ITeam
import de.gesundkrank.jskills.Rating
import de.gesundkrank.jskills.TrueSkillCalculator
import kotlin.math.roundToInt
import org.opencraft.server.model.Player

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
        if (team1.isNotEmpty() && team2.isNotEmpty())
            (TrueSkillCalculator.calculateMatchQuality(
                gameInfo,
                teams(team1, team2)
            ) * 100).roundToInt() else 0

    companion object {
        // Sources
        // https://www.moserware.com/assets/computing-your-skill/The%20Math%20Behind%20TrueSkill.pdf
        // https://trueskill.org/


        // the initial mean of ratings.
        const val initialMean = 1000.0

        // the initial standard deviation of ratings. The recommended value is a third of initialMean
        const val initialStandardDevation = initialMean / 3.0


        // In (2), TrueSkill  β defines the length of the “skill
        //chain.” If a game has a wide range of skills, then β will tell you how wide each link is in the skill chain.
        //This can also be thought of how wide (in terms of skill points) each skill class.
        //Similiarly, β tells us the number of skill points a person must have above someone else to identify an
        //80% probability of win against that person.
        //For example, if β is 4 then a player Alice with a skill of “30” will tend to win against Bob who has a skill of
        //“26” approximately 80% of the time.

        // Default distance that guarantees about 76% chance of winning.
        // The recommended value is a half of sigma.
        const val beta = initialStandardDevation / 2.0

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