package tf.jacobsc.ctf.server.commands

import kotlin.math.roundToInt
import org.opencraft.server.cmd.Command
import org.opencraft.server.cmd.CommandParameters
import org.opencraft.server.model.Player
import tf.jacobsc.utils.matchQuality
import tf.jacobsc.utils.separatePlayers

object QualityCommand : Command {
    private fun sumTeamTrueSkill(players: Pair<List<Player>, List<Player>>): Pair<Int, Int> {
        val (redTeam, blueTeam) = players
        return redTeam.sumOf { it.teamRating.conservativeRating }
            .roundToInt() to blueTeam.sumOf { it.teamRating.conservativeRating }.roundToInt()
    }

    override fun execute(player: Player, params: CommandParameters) {
        val quality = matchQuality()
        val (redTeamSkill, blueTeamSkill) = sumTeamTrueSkill(separatePlayers())

        player.actionSender.sendChatMessage("- &bGame quality is $quality%")
        player.actionSender.sendChatMessage("- &bRed team total $redTeamSkill")
        player.actionSender.sendChatMessage("- &bBlue team total $blueTeamSkill")
        player.actionSender.sendChatMessage("- &bPlayer skill uncertainty will make game quality worse.")
    }
}
