package dev.samperlmutter.strikecounter.brother

import org.springframework.data.repository.CrudRepository

interface BrotherRepository : CrudRepository<Brother, String> {
    fun findBySlackId(slackId: String): Brother
}