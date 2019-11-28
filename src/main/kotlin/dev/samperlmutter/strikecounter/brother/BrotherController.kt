package dev.samperlmutter.strikecounter.brother

import com.fasterxml.jackson.databind.JsonNode
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

    @RequestMapping(method = [RequestMethod.POST], value = ["/strikes"])
    fun cmdHandler(keyVals: HashMap<String, Any>) {
        val brother = brotherRepository.findBySlackId(keyVals["user_id"] as String)
        val params = (keyVals["text"] as String).split(' ')
        val url = keyVals["response_url"] as String

        when (keyVals["command"]) {
            "/stroke" -> strike(brother, params, url)
            "/unstroke" -> unstrike(brother, params, url)
            "/strokes" -> strikes(url)
        }
    }

    @RequestMapping(method = [RequestMethod.POST], value = ["/test"], consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun test(body: SlackSlashCommand): ResponseEntity<Any> {
        println(body.text)
        return ResponseEntity.ok().body(body)
    }

    private fun strike(caller: Brother, params: List<String>, url: String) {
        if (caller.canStrike) {
            var brothers = Array(params.size) { i -> brotherRepository.findBySlackId(params[i])}
            for (param in params) {
                var brother = brotherRepository.findBySlackId(param)
                brother.strikes++
                brotherRepository.save(brother)
            }
            // TODO: return a happy message
            respond(url, "${brothers[0].fullName} has ${brothers[0].strikes} strikes.")
        } else {
            // TODO: inform user they don't have access
        }
    }

    private fun unstrike(caller: Brother, params: List<String>, url: String) {
        if (caller.canStrike) {
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

    private fun strikes(url: String) {
        val brothers = brotherRepository.findAll()
        // TODO: return info
    }

    private fun respond(url: String, message: String) {
        val restTemplate = RestTemplate()
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        var map = HashMap<String, String>()
        map.put("text", message)
        val json = JSONObject(map)

        val request = HttpEntity(json.toString(), headers)
        val result = restTemplate.postForObject(url, request, String::class.java)
    }
}