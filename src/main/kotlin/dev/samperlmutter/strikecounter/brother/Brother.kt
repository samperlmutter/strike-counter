package dev.samperlmutter.strikecounter.brother

import dev.samperlmutter.strikecounter.strike.Strike
import javax.persistence.*

@Entity
@Table(name = "brothers")
data class Brother(
    @Id @Column(name = "id", nullable = false, length = 50)
    var slackId: String? = null,
    @Column(name = "name", nullable = false, length = 200)
    var name: String,
    @Column(name = "can_strike", columnDefinition = "TINYINT(1) NOT NULL DEFAULT '0'")
    var canAct: Boolean,
    @Column(name = "can_reset", columnDefinition = "TINYINT(1) NOT NULL DEFAULT '0'")
    var canReset: Boolean,
    @OneToMany(fetch = FetchType.EAGER) @JoinColumn(name = "brother")
    var strikes: List<Strike>,
    @Column(name = "points", nullable = false)
    var points: Int
)
