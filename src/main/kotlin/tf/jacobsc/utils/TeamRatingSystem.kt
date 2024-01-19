package tf.jacobsc.utils

import de.gesundkrank.jskills.Rating
import org.opencraft.server.model.Player

object TeamRatingSystem : RatingSystem {
    override fun getRating(p: Player): Rating = p.teamRating

    override fun setRating(p: Player, r: Rating) {
        p.setRating(RatingType.Team, r)
    }
}