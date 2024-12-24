package tf.jacobsc.ctf.server

import org.opencraft.server.game.impl.CTFGameMode
import org.opencraft.server.model.World
import kotlin.concurrent.thread

fun staleMateThread(world: World, seconds: Int) =
    world.timedAnnouncer({ "- &e$it seconds remaining until the stalemate ends!" }, seconds) {
        val gameMode = world.gameMode as CTFGameMode
        world.playerList.players.forEach { player ->
            if (player.hasFlag) {
                gameMode.dropFlag(player, true, false)
            }
        }
        gameMode.returnDroppedRedFlag()
        gameMode.returnDroppedBlueFlag()
    }

fun World.timedAnnouncer(announcement: (Long) -> String?, seconds: Int, action: () -> Unit) = thread {
    try {
        var remainingTime = seconds.toLong()

        while (remainingTime >= 40) {
            broadcast(announcement(remainingTime) ?: return@thread)
            remainingTime -= 30
            Thread.sleep(30 * 1000)
        }

        broadcast(announcement(remainingTime) ?: return@thread)
        val toSleep = (remainingTime - 10).coerceAtLeast(0)
        remainingTime -= toSleep
        if (toSleep > 0) {
            Thread.sleep(toSleep * 1000)
            broadcast(announcement(remainingTime) ?: return@thread)
        }
        Thread.sleep(remainingTime.coerceAtLeast(0) * 1000)

        if (!Thread.currentThread().isInterrupted) {
            action()
        }
    } catch (_: InterruptedException) {
    }
}
