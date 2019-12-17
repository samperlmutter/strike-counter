package dev.samperlmutter.strikecounter.brother

import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "brothers")
class Brother(
    @Id @Column(name = "id", nullable = false, length = 200) @NotNull
    var slackId: String,
    @Column(name = "name", nullable = false, length = 200) @NotNull
    var name: String,
    @Column(name = "can_strike", columnDefinition = "TINYINT(1) NOT NULL DEFAULT '0'") @NotNull
    var canAct: Boolean,
    @Column(name = "can_reset", columnDefinition = "TINYINT(1) NOT NULL DEFAULT '0'") @NotNull
    var canReset: Boolean,
    @Column(name = "strikes", nullable = false) @NotNull
    var strikes: Int,
    @Column(name = "points", nullable = false) @NotNull
    var points: Int
)
