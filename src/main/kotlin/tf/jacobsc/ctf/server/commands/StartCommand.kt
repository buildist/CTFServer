package tf.jacobsc.ctf.server.commands

import org.opencraft.server.Constants
import org.opencraft.server.cmd.Command
import org.opencraft.server.cmd.CommandParameters
import org.opencraft.server.game.impl.GameSettings
import org.opencraft.server.model.Player
import org.opencraft.server.model.World
import tf.jacobsc.ctf.server.timedAnnouncer
import tf.jacobsc.utils.matchQuality

object StartCommand : Command {
    override fun execute(player: Player, params: CommandParameters) {
        if (!player.isOp && !player.isVIP) {
            player.actionSender.sendChatMessage("You must be OP to do that!")
            return
        }

        if (!GameSettings.getBoolean("Tournament")) {
            player.actionSender.sendChatMessage("Tournament mode must be activated.")
            return
        }

        if (World.getWorld().gameMode.startCommandExecuted) {
            player.actionSender.sendChatMessage("/start has already been issued.")
            return
        }

        val seconds = if (params.argumentCount >= 1) {
            try {
                val seconds = params.getIntegerArgument(0)
                if (seconds <= 0) {
                    player.actionSender.sendChatMessage("Start seconds cannot be negative.")
                    return
                }
                seconds
            } catch (e: NumberFormatException) {
                player.actionSender.sendChatMessage("Must be an integer representing number of seconds.")
                return
            }
        } else 10

        val quality = matchQuality()
        World.getWorld().gameMode.startCommandExecuted = true
        World.getWorld().timedAnnouncer({ secondsLeft ->
            if (World.getWorld().gameMode.startCommandExecuted) {
                "- &aGame will start in $secondsLeft seconds!"
            } else null
        }, seconds) {
            val world = World.getWorld()
            if (!world.gameMode.startCommandExecuted) return@timedAnnouncer

            world.playerList.players
                .filter { it.team != Constants.SPEC_TEAM }
                .forEach(Player::sendToTeamSpawn)

            // Hide other spectators during tournament games for viewability
            for (p in World.getWorld().playerList.players) {
                if (p.team != Constants.SPEC_TEAM) {
                    continue
                }

                for (other in World.getWorld().playerList.players) {
                    // Don't hide self or non-spec players
                    if (other === p || other.team != Constants.SPEC_TEAM) {
                        continue
                    }

                    p.actionSender.sendRemoveEntity(other) // Hide their player entity
                }
            }

            Thread.sleep(250)
            world.gameMode.tournamentGameStarted = true
            world.gameMode.gameStartTime = System.currentTimeMillis()
            world.broadcast("- &aThe game has started!")
        }
        World.getWorld().broadcast("- &aGame is rated. Game quality is $quality%")
    }
}