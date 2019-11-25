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

    @RequestMapping(method = [RequestMethod.POST], name = "/strokes")
    fun cmdHandler(keyVals: HashMap<String, Any>) {
        val brother = brotherRepository.findBySlackId(keyVals["user_id"] as String)
        val params = (keyVals["text"] as String).split(' ')

        when (keyVals["command"]) {
            "/stroke" -> strike(brother, params)
            "/unstroke" -> unstrike(brother, params)
            "/strokes" -> strikes()
        }
    }

    private fun strike(caller: Brother, params: List<String>) {
        if (caller.canStrike == 1) {
            for (param in params) {
                var brother = brotherRepository.findBySlackId(param)
                brother.strikes++
                brotherRepository.save(brother)
            }
            // TODO: return a happy message
        } else {
            // TODO: inform user they don't have access
        }
    }

    private fun unstrike(caller: Brother, params: List<String>) {
        if (caller.canStrike == 1) {
            for (param in params) {
                var brother = brotherRepository.findBySlackId(param)
                brother.strikes--
                brotherRepository.save(brother)
            }
            // TODO: return a happy message
        } else {
            // TODO: inform user they don't have access
        }
    }

    private fun strikes() {
        val brothers = brotherRepository.findAll()
        // TODO: return info
    }
}