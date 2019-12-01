package dev.samperlmutter.strikecounter.brother

import dev.samperlmutter.strikecounter.slack.SlackSlashCommand
import net.minidev.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate

@RestController
class BrotherController() {
    private lateinit var brotherRepository: BrotherRepository
    private lateinit var restTemplateBuilder: RestTemplateBuilder

    @Autowired
    constructor(brotherRepository: BrotherRepository, restTemplateBuilder: RestTemplateBuilder) : this() {
        this.brotherRepository = brotherRepository
        this.restTemplateBuilder = restTemplateBuilder
    }

    @RequestMapping(method = [RequestMethod.POST], value = ["/strikes"], consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun cmdHandler(body: SlackSlashCommand): ResponseEntity<Any> {
        val caller = brotherRepository.findBySlackId(parseUser(body.user_id))
        val params = body.text.split(' ')
        val url = body.response_url
        val regex = "(?<=@)([A-Z0-9])\\w+(?=\\||>)".toRegex()


        when (params[0]) {
            "add" -> addStrike(caller, params.subList(1, params.size), url)
            "remove" -> removeStrike(caller, params.subList(1, params.size), url)
            "list" -> listStrikes(url)
            else -> {

            }
        }

        return ResponseEntity.ok().build()
    }

    @RequestMapping(method = [RequestMethod.POST], value = ["/test"], consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun test(body: SlackSlashCommand): ResponseEntity<Any> {
        println(body.text)
        sendMessage(body.response_url, "you got it")
        return ResponseEntity.ok().body(body)
    }

    private fun addStrike(caller: Brother, params: List<String>, url: String) {
        if (caller.canStrike) {
            var message = ""
            for (param in params) {
                var brother = brotherRepository.findBySlackId(param)
                brother.strikes++
                brotherRepository.save(brother)
                message += "${brother.name.capitalize()} has now been stroked ${brother.strikes} times\n"
            }
            sendMessage(url, message.trimMargin())
        } else {
            sendMessage(url, "Sorry, you're not allowed to stroke other brothers")
        }
    }

    private fun removeStrike(caller: Brother, params: List<String>, url: String) {
        if (caller.canStrike) {
            var message = ""
            for (param in params) {
                var brother = brotherRepository.findBySlackId(param)
                brother.strikes--
                brotherRepository.save(brother)
                message += "${brother.name.capitalize()} has now only been stroked ${brother.strikes} times\n"
            }
            sendMessage(url, message.trimMargin())
        } else {
            sendMessage(url, "Sorry, you're not allowed to stroke other brothers")
        }
    }

    private fun listStrikes(url: String) {
        var message = ""
        for (brother in brotherRepository.findAll()) {
            message += "${brother.name.capitalize()} has now only been stroked ${brother.strikes} times\n"
        }
        sendMessage(url, message.trimMargin())
    }

    private fun sendMessage(url: String, message: String, ephemeral: Boolean = false) {
        val restTemplate = RestTemplate()
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        var map = HashMap<String, String>()
        map.put("text", message)
        map.put("response_type", "ephemeral")
        val json = JSONObject(map)

        val request = HttpEntity(json.toString(), headers)

        restTemplate.postForObject(url, request, String::class.java)
    }

    private fun parseUser(user: String): String {
        val regex = "(?<=@)([A-Z0-9])\\w+(?=\\||>)".toRegex()
        return regex.find(user)!!.value
    }
}