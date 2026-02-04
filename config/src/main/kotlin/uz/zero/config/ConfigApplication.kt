package uz.zero.config

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.config.server.EnableConfigServer

@SpringBootApplication
@EnableConfigServer
open class ConfigApplication

fun main(args: Array<String>) {
    runApplication<ConfigApplication>(*args)
}
