package tf.jacobsc.ctf.server.commands

import kotlin.math.max
import kotlin.math.roundToInt
import org.opencraft.server.cmd.Command
import org.opencraft.server.cmd.CommandParameters
import org.opencraft.server.model.Player
import org.opencraft.server.model.World
import tf.jacobsc.utils.RatingType
import tf.jacobsc.utils.displayRating
import tf.jacobsc.utils.gameCountColor
import tf.jacobsc.utils.matchQuality
import tf.jacobsc.utils.separatePlayers

object QualityCommand : Command {
    private const val columnWidth = 60 / 2

    private fun printPlayer(player: Player?): String {
        if (player == null) return ""

        val ratedGames = player.getRatedGamesFor(RatingType.Team)

        val gamesColor = gameCountColor(ratedGames)

        val gamesString = "&b($gamesColor$ratedGames&b)"

        return "${player.coloredName} $gamesString ${player.teamRating.displayRating()}"
    }

    private fun sumTeamTrueSkill(redTeam: List<Player>, blueTeam: List<Player>): Pair<Int, Int> {
        return redTeam.sumOf { it.teamRating.conservativeRating }
            .roundToInt() to blueTeam.sumOf { it.teamRating.conservativeRating }.roundToInt()
    }

    private fun tableString(): String {
        val players = separatePlayers()
        val (red, blue) = players
        val (redTeamSkill, blueTeamSkill) = sumTeamTrueSkill(red, blue)

        val redTeam = red.sortedByDescending { it.teamRating.conservativeRating }
        val blueTeam = blue.sortedByDescending { it.teamRating.conservativeRating }
        val specTeam = World.getWorld().playerList.players.filter { it.team < 0 && !it.AFK}
            .sortedByDescending { it.teamRating.conservativeRating }

        val rows = max(redTeam.size, blueTeam.size)

        return buildString {
            val header = "Player (games) "
            appendLine(
                "&b$header&c$redTeamSkill".padEnd(columnWidth) +
                        "&b$header&9$blueTeamSkill".padStart(columnWidth)
            )
            appendLine("".padEnd(columnWidth * 2, '-'))

            for (row in 0 until rows) {
                val redPlayerText = printPlayer(redTeam.getOrNull(row))
                val bluePlayerText = printPlayer(blueTeam.getOrNull(row))
                appendLine(
                    redPlayerText.padEnd(columnWidth) +
                            bluePlayerText.padStart(columnWidth)
                )
            }

            specTeam.forEach { player ->
                appendLine(printPlayer(player))
            }
        }
    }

    override fun execute(player: Player, params: CommandParameters) {
        val quality = matchQuality()

        player.actionSender.sendChatMessage("- &bGame quality is $quality%")
        tableString().lines().dropLast(1).forEach {
            player.actionSender.sendChatMessage("- $it")
        }
    }
}
