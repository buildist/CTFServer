package tf.jacobsc.ctf.server.commands

import org.opencraft.server.Constants
import org.opencraft.server.cmd.Command
import org.opencraft.server.cmd.CommandParameters
import org.opencraft.server.game.impl.GameSettings
import org.opencraft.server.model.Player
import org.opencraft.server.model.World

object TeamsCommand : Command {
    private var red: List<String> = emptyList()
    private var blue: List<String> = emptyList()

    override fun execute(player: Player, params: CommandParameters) {
        if (!teamStateCheck(player)) return

        if (params.argumentCount != 1) {
            player.actionSender.sendChatMessage("/teams save/restore/switch")
            return
        }
        val command = params.getStringArgument(0)

        when (command) {
            "save" -> {
                save()
                player.actionSender.sendChatMessage("Teams were saved.")
                player.actionSender.sendChatMessage("Red: ${red.joinToString()}")
                player.actionSender.sendChatMessage("Blue: ${blue.joinToString()}")
            }
            "restore" -> restore(player)
            "switch" -> switch()
            else -> player.actionSender.sendChatMessage("/teams save/restore/switch")
        }
    }

    private fun teamStateCheck(player: Player): Boolean {
        if (!player.isOp && !player.isVIP) {
            player.actionSender.sendChatMessage("You must be OP or VIP to do that!")
            return false
        }

        if (!GameSettings.getBoolean("Tournament")) {
            player.actionSender.sendChatMessage("Must be in tournament mode for that.")
            return false
        }

        return true
    }

    private fun save() {
        val players = World.getWorld().playerList.players
        save(players)
    }

    private fun restore(player: Player) {
        players().forEach { (name, team) ->
            val other = Player.getPlayer(name, player.actionSender) ?: return@forEach
            other.joinTeam(team)
        }
    }

    private fun switch() {
        val players = World.getWorld().playerList.players
        players.forEach { other ->
            when (other.team) {
                Constants.RED_TEAM -> other.joinTeam("blue")
                Constants.BLUE_TEAM -> other.joinTeam("red")
            }
        }
        save()
    }

    private fun players(): List<Pair<String, String>> = (red.map { it to "red" } + blue.map { it to "blue" })

    @Synchronized
    fun save(players: List<Player>) {
        red = players.filter { it.team == Constants.RED_TEAM }.map { it.name }
        blue = players.filter { it.team == Constants.BLUE_TEAM }.map { it.name }
    }
}
