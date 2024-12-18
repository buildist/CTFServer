package tf.jacobsc.ctf.server

import org.opencraft.server.game.impl.CTFGameMode
import org.opencraft.server.model.World
import kotlin.concurrent.thread

fun staleMateThread(world: World, seconds: Int) = thread {
    try {
        var remainingTime = seconds.toLong()

        while (remainingTime >= 40) {
            world.broadcast("- &e$remainingTime seconds remaining until the stalemate ends!")
            remainingTime -= 30
            Thread.sleep(30 * 1000)
        }

        world.broadcast("- &e$remainingTime seconds remaining until the stalemate ends!")
        val toSleep = (remainingTime - 10).coerceAtLeast(0)
        remainingTime -= toSleep
        if (toSleep > 0) {
            Thread.sleep(toSleep * 1000)
            world.broadcast("- &e$remainingTime seconds remaining until the stalemate ends!")
        }
        Thread.sleep(remainingTime.coerceAtLeast(0) * 1000)

        if (!Thread.currentThread().isInterrupted) {
            val gameMode = world.gameMode as CTFGameMode
            world.playerList.players.forEach { player ->
                if (player.hasFlag) {
                    gameMode.dropFlag(player, true, false)
                }
            }
            gameMode.returnDroppedRedFlag()
            gameMode.returnDroppedBlueFlag()
        }
    } catch (_: InterruptedException) {}
}
