package tf.jacobsc.utils

import de.gesundkrank.jskills.Rating
import org.opencraft.server.model.Player

object DuelRatingSystem : RatingSystem {
    override fun getRating(p: Player): Rating = p.duelRating

    override fun setRating(p: Player, r: Rating) {
        p.duelRating = r
    }
}