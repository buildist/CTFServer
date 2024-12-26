package tf.jacobsc.utils

import org.opencraft.server.Constants
import org.opencraft.server.model.Player
import org.opencraft.server.model.World

fun World.topPlayers(count: Int? = null) =
    playerList.players
        .filter { it.team != Constants.SPEC_TEAM }
        .sortedWith(
            compareByDescending<Player> { it.currentRoundPointsEarned }
                .thenByDescending { it.captures }
                .thenByDescending { it.kills }
                .thenBy { it.deaths }
                .thenBy { it.name }
        )
        .let { if (count != null) it.take(count) else it }

fun World.playerPlacement(p: Player) = topPlayers().indexOf(p) + 1
