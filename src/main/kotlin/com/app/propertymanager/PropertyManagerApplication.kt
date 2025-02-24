package com.app.propertymanager

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PropertyManagerApplication

fun main(args: Array<String>) {
    runApplication<PropertyManagerApplication>(*args)
}
