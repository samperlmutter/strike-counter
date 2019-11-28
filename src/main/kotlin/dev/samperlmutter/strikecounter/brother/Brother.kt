package dev.samperlmutter.strikecounter.brother

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotNull

@Entity
@Table(name = "brothers")
class Brother(
    @Id @Column(name = "id", nullable = false, length = 200) @NotNull
    var slackId: String,
    @Column(name = "full_name", nullable = false, length = 200) @NotNull
    var fullName: String,
    @Column(name = "can_strike", nullable = false, length = 1) @NotNull
    var canStrike: Boolean,
    @Column(name = "strikes")
    var strikes: Int
)
