package com.github.mrlanu.springredditclone

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class SpringRedditCloneApplication

fun main(args: Array<String>) {
    runApplication<SpringRedditCloneApplication>(*args)
}
