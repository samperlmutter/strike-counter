package dev.samperlmutter.strikecounter.strike

import dev.samperlmutter.strikecounter.brother.Brother
import dev.samperlmutter.strikecounter.brother.BrotherRepository
import dev.samperlmutter.strikecounter.slack.SlackService
import dev.samperlmutter.strikecounter.slack.SlackSlashCommand
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class StrikeService @Autowired constructor(
    private val slackService: SlackService,
    private val brotherRepository: BrotherRepository
) {
    fun strikesHandler(body: SlackSlashCommand): ResponseEntity<Any> {
        val caller = brotherRepository.findBySlackId(body.user_id)
        val params = body.text.split(' ')

        return when (params[0]) {
            "add" -> addStrike(caller, params.subList(1, params.size))
            "remove" -> removeStrike(caller, params.subList(1, params.size))
            "list" -> listStrikes()
            "reset" -> resetStrikes(caller)
            else -> strikesHelp()
        }
    }

    fun addStrike(caller: Brother, params: List<String>): ResponseEntity<Any> {
        return if (caller.canAct) {
            var message = ""
            for (param in params) {
                var brother = brotherRepository.findBySlackId(slackService.parseUser(param))
                brother.strikes++
                brotherRepository.save(brother)
                message += "${brother.name.capitalize()} now has ${brother.strikes} strike${if (brother.strikes == 1) "" else "s"}\n"
            }
            ResponseEntity.ok(slackService.buildResponse(message.trimMargin()))
        } else {
            ResponseEntity.ok(slackService.buildResponse("Sorry, you're not allowed to strike other brothers"))
        }
    }

    fun removeStrike(caller: Brother, params: List<String>): ResponseEntity<Any> {
        return if (caller.canAct) {
            var message = ""
            for (param in params) {
                var brother = brotherRepository.findBySlackId(slackService.parseUser(param))
                brother.strikes -= if (brother.strikes < 1) 0 else 1
                brotherRepository.save(brother)
                message += "${brother.name.capitalize()} now has ${brother.strikes} strike${if (brother.strikes == 1) "" else "s"}\n"
            }
            ResponseEntity.ok(slackService.buildResponse(message.trimMargin()))
        } else {
            ResponseEntity.ok(slackService.buildResponse("Sorry, you're not allowed to strike other brothers"))
        }
    }

    fun listStrikes(): ResponseEntity<Any> {
        var message = ""
        for (brother in brotherRepository.findAll().sortedWith(compareBy({ -it.strikes }, { it.name }))) {
            message += "• ${brother.name.capitalize()} has ${brother.strikes} strike${if (brother.strikes == 1) "" else "s"}\n"
        }
        return ResponseEntity.ok(slackService.buildResponse(message.trimMargin()))
    }

    fun resetStrikes(caller: Brother): ResponseEntity<Any> {
        return if (caller.canReset) {
            for (brother in brotherRepository.findAll()) {
                brother.strikes = 0
                brotherRepository.save(brother)
            }
            ResponseEntity.ok(slackService.buildResponse("Strikes have now been reset"))
        } else {
            ResponseEntity.ok(slackService.buildResponse("Sorry, you're not allowed to reset strikes"))
        }
    }

    fun strikesHelp(): ResponseEntity<Any> {
        val message = """
            *Available commands*:
            >*Add a strike*
            >Type `/strikes add @{name1} @{name2} ...` to add a strike to each user listed

            >*Remove a strike*
            >Type `/strikes remove @{name1} @{name2} ...` to remove a strike from each user listed

            >*List everyone's strikes*
            >Type `/strikes list [alpha | num]` to list how many strikes each user has, sorted alphabetically or numerically
            >Sorts numerically by default

            >*Reset strikes*
            >Type `/strikes reset` to reset everyone's strikes to 0
            >This should only be done at the end of the semester

            >*Help*
            >Type `/strikes help` to display this message
        """.trimIndent()

        return ResponseEntity.ok(slackService.buildResponse(message.trimMargin()))
    }
}