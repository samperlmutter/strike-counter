package dev.samperlmutter.strikecounter.strike

import dev.samperlmutter.strikecounter.brother.Brother
import org.springframework.data.repository.CrudRepository

interface StrikeRepository : CrudRepository<Strike, Int> {
    fun findAllByBrother(brother: Brother): List<Strike>
}