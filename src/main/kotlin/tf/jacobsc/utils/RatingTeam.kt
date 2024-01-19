package tf.jacobsc.utils

import de.gesundkrank.jskills.IPlayer
import de.gesundkrank.jskills.ITeam
import de.gesundkrank.jskills.Rating
import org.opencraft.server.model.Player

class RatingTeam(
    private val players: MutableMap<IPlayer, Rating>,
) : ITeam, MutableMap<IPlayer, Rating> by players {
    constructor(players: List<Player>, rating: (Player) -> Rating) : this(
        players
            .associateWith(rating)
            .toMutableMap()
    )
}