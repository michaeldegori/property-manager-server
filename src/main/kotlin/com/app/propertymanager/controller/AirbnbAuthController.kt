package com.app.propertymanager.controller

import com.app.propertymanager.service.AirbnbAuthService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/airbnb/auth")
class AirbnbAuthController(
    private val airbnbAuthService: AirbnbAuthService
) {
    @GetMapping("/login")
    suspend fun login(): String {
        return try {
            airbnbAuthService.performLogin()
        } catch (e: Exception) {
            "Error during login: ${e.message}"
        }
    }
}
