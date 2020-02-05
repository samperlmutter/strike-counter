package dev.samperlmutter.strikecounter.strike

import dev.samperlmutter.strikecounter.brother.Brother
import dev.samperlmutter.strikecounter.brother.BrotherRepository
import dev.samperlmutter.strikecounter.slack.SlackService
import dev.samperlmutter.strikecounter.slack.SlackSlashCommand
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException

@Service
class StrikeService @Autowired constructor(
    private val slackService: SlackService,
    private val brotherRepository: BrotherRepository,
    private val strikeRepository: StrikeRepository
) {
    fun strikesHandler(body: SlackSlashCommand): ResponseEntity<Any> {
        val caller = brotherRepository.findBySlackId(body.user_id)
        val params = body.text.split(' ')

        return when (params[0]) {
            "add" -> addStrike(caller, params.subList(1, params.size))
            "remove" -> removeStrike(caller, params.subList(1, params.size))
            "list" -> listStrikes(caller, params)
            "reset" -> resetStrikes(caller)
            else -> strikesHelp()
        }
    }

    private fun addStrike(caller: Brother, params: List<String>): ResponseEntity<Any> {
        return if (caller.canAct) {
            return if (params.size >= 4) {
                val brother = brotherRepository.findBySlackId(slackService.parseUser(params[0]))
                val offense = try { Offense.valueOf(params[1].toUpperCase()) } catch (e: IllegalArgumentException) { return ResponseEntity.ok(slackService.buildResponse("Invalid offense. Valid options are `tardy` and `absent`")) }
                val excusability = try { Excusability.valueOf(params[2].toUpperCase()) } catch (e: IllegalArgumentException) { return ResponseEntity.ok(slackService.buildResponse("Invalid excuse. Valid options are `excused` and `unexcused`")) }
                val reason = params.subList(3, params.size).joinToString(" ")

                val strike = Strike(
                    brother = brother,
                    offense = offense,
                    excusability = excusability,
                    reason = reason
                )

                strikeRepository.save(strike)

                val strikes = strikeRepository.findAllByBrother(brother)
                val message = "${brother.name.capitalize()} now has ${strikes.size} strike${if (strikes.size == 1) "" else "s"}\n"
                ResponseEntity.ok(slackService.buildResponse(message.trimMargin()))
            } else {
                ResponseEntity.ok(slackService.buildResponse("Invalid number of arguments"))
            }
        } else {
            ResponseEntity.ok(slackService.buildResponse("Sorry, you're not allowed to manage strikes"))
        }
    }

    private fun removeStrike(caller: Brother, params: List<String>): ResponseEntity<Any> {
        return if (caller.canAct) {
            return if (params.size == 2) {
                val brother = brotherRepository.findBySlackId(slackService.parseUser(params[0]))
                val strikeId = params[1].toInt()
                val strikes = strikeRepository.findAllByBrother(brother)
                if (strikeId < 1 || strikeId > strikes.size) {
                    return ResponseEntity.ok(slackService.buildResponse("Invalid strike identifier"))
                }
                val strike = strikeRepository.findAllByBrother(brother)[strikeId - 1]
                strikeRepository.delete(strike)
                ResponseEntity.ok(slackService.buildResponse("${brother.name} now has ${strikes.size - 1} strike${if (strikes.size == 1) "" else "s"}"))
            } else {
                ResponseEntity.ok(slackService.buildResponse("Invalid number of arguments"))
            }
        } else {
            ResponseEntity.ok(slackService.buildResponse("Sorry, you're not allowed to manage strikes"))
        }
    }

    private fun listStrikes(caller: Brother, params: List<String>): ResponseEntity<Any> {
        return if (caller.canAct) {
            var message = ""
            return when {
                params.size == 2 -> {
                    val brother = brotherRepository.findBySlackId(slackService.parseUser(params[1]))
                    val strikes = brother.strikes
                    if (strikes.isNotEmpty()) {
                        for (i in strikes.indices) {
                            message += "${i + 1}. ${strikes[i]}\n"
                        }
                    } else {
                        message = "${brother.name.capitalize()} has 0 strikes"
                    }
                    ResponseEntity.ok(slackService.buildResponse(message.trimMargin()))
                }
                params.size < 2 -> {
                    for (brother in brotherRepository.findAll().sortedWith(compareBy({ -it.strikes.size }, { it.name }))) {
                        message += "• ${brother.name.capitalize()} has ${brother.strikes.size} strike${if (brother.strikes.size == 1) "" else "s"}\n"
                    }
                    ResponseEntity.ok(slackService.buildResponse(message.trimMargin()))
                }
                else -> {
                    ResponseEntity.ok(slackService.buildResponse("Invalid number of arguments"))
                }
            }
        } else {
            ResponseEntity.ok(slackService.buildResponse("Sorry, you're not allowed to manage strikes"))
        }
    }

    private fun resetStrikes(caller: Brother): ResponseEntity<Any> {
        return if (caller.canReset) {
            strikeRepository.deleteAll()
            ResponseEntity.ok(slackService.buildResponse("Strikes have now been reset"))
        } else {
            ResponseEntity.ok(slackService.buildResponse("Sorry, you're not allowed to manage strikes"))
        }
    }

    private fun strikesHelp(): ResponseEntity<Any> {
        val message = """
            *Available commands*:
            >*Add a strike*
            >Type `/strikes add @{name} {tardy | absent} {excused | unexcused} {reason}` to add a strike to the specified user

            >*Remove a strike*
            >Type `/strikes remove @{name} {strikeNumber}` to remove the specified strike from the specified

            >*List everyone's strikes*
            >Type `/strikes list [@{name}]` to list how many strikes each user has, sorted numerically
            >Optionally mention a user to list information about their strikes

            >*Reset strikes*
            >Type `/strikes reset` to reset everyone's strikes to 0
            >This should only be done at the end of the semester

            >*Help*
            >Type `/strikes help` to display this message
        """.trimIndent()

        return ResponseEntity.ok(slackService.buildResponse(message.trimMargin()))
    }
}