package tf.jacobsc.ctf.server

import org.opencraft.server.model.World
import org.opencraft.server.persistence.SavePersistenceRequest
import java.io.IOException

fun savePlayerStats(world: World) {
    for (p in world.playerList.players) {
        try {
            SavePersistenceRequest(p).perform()
        } catch (ex: IOException) {
        }
    }
}
