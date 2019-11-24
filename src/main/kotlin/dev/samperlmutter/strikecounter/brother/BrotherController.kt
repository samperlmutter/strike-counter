package dev.samperlmutter.strikecounter.brother

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class BrotherController() {
    private lateinit var brotherRepository: BrotherRepository

    @Autowired
    constructor(brotherRepository: BrotherRepository) : this() {
        this.brotherRepository = brotherRepository
    }
}