package tf.jacobsc.utils

import org.opencraft.server.model.Player

fun Player.abandonDuel() {
    if (duelPlayer == null) return
    // calculate a duel loss for them if they abandon
    RatingSystem(RatingType.Duel).setRatings(duelPlayer, this)
    duelPlayer.duelPlayer = null
    duelPlayer = null
}
