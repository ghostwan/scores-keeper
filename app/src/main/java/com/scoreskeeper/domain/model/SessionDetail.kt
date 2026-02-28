package com.scoreskeeper.domain.model

/**
 * Full details of a session, including players, rounds, and ranking.
 */
data class SessionDetail(
    val session: Session,
    val players: List<Player>,
    val rounds: List<RoundScore>,
    val lowestScoreWins: Boolean,
) {
    /** Total cumulative score per player. */
    val totals: Map<Long, Int> by lazy {
        players.associate { player ->
            player.id to rounds.filter { it.playerId == player.id }.sumOf { it.points }
        }
    }

    /** Current round number (last recorded + 1). */
    val currentRound: Int by lazy {
        (rounds.maxOfOrNull { it.round } ?: 0) + 1
    }

    /** Ranked list of players (best first). */
    val ranking: List<Pair<Player, Int>> by lazy {
        players
            .map { it to (totals[it.id] ?: 0) }
            .sortedWith(
                if (lowestScoreWins) compareBy { it.second }
                else compareByDescending { it.second }
            )
    }

    /** The winner(s) — null if session is still in progress. */
    val winners: List<Player>? by lazy {
        if (session.status == SessionStatus.FINISHED) {
            val bestScore = ranking.firstOrNull()?.second ?: return@lazy null
            ranking.filter { it.second == bestScore }.map { it.first }
        } else null
    }
}
