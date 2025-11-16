package tf.jacobsc.ctf.server

import org.opencraft.server.model.Player

class FlameTickRecord(
    private val players: MutableList<Pair<Player, Player>> = mutableListOf(),
) {
    fun addFlameKill(killer: Player, killed: Player) {
        players.add(Pair(killer, killed))
    }

    fun getFlameKills(): List<Pair<Player, Player>> {
        return players
    }

    fun clear() {
        players.clear()
    }
}
