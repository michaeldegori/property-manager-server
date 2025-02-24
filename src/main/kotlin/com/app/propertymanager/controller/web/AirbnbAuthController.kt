package com.app.propertymanager.controller.web

import com.app.propertymanager.service.AirbnbAuthService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("airbnb/auth")
class AirbnbAuthController(
    private val airbnbAuthService: AirbnbAuthService
) {
    @GetMapping("/connect")
    fun showLoginFor(): String {
        return "airbnb/connectionForm"
    }

    @PostMapping("/connect")
    suspend fun processLogin(
        @RequestParam email: String,
        @RequestParam password: String,
        model: Model
    ): String {
        return try {
            val response = airbnbAuthService.performLogin(email, password)
            model.addAttribute("airbnbResponse", response)
            "connectionSuccess"
        } catch (e: Exception) {
            model.addAttribute("errorMsg", e.message)
            "airbnb/connectionForm"
        }
    }
}
