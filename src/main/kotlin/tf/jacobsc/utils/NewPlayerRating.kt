package tf.jacobsc.utils

import de.gesundkrank.jskills.Rating
import org.opencraft.server.model.Player

data class NewPlayerRating(
    val player: Player,
    val rating: Rating,
)