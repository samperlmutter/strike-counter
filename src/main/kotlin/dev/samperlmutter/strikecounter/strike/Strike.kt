package dev.samperlmutter.strikecounter.strike

import dev.samperlmutter.strikecounter.brother.Brother
import javax.persistence.*

@Entity
@Table(name = "strikes")
data class Strike(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    @JoinColumn(name = "brother", nullable = false, insertable = true, updatable = false) @ManyToOne()
    var brother: Brother,
    @Enumerated @Column(name = "offense", nullable = false)
    var offense: Offense,
    @Enumerated @Column(name = "excusability", nullable = false)
    var excusability: Excusability,
    @Column(name = "reason", nullable = false)
    var reason: String
) {
    override fun toString(): String {
        return "${brother.name} has an *${excusability.toString().toLowerCase()} ${offense.toString().toLowerCase()}* " +
                "for reason: *$reason*"
    }
}