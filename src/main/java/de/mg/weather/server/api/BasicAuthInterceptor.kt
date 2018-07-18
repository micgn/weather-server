package de.mg.weather.server.api

import de.mg.weather.server.conf.WeatherConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import java.lang.Exception
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Configuration
open class BasicAuthInterceptor : WebMvcConfigurerAdapter() {

    @Autowired
    private lateinit var config: WeatherConfig

    override fun addInterceptors(registry: InterceptorRegistry) {
        val basicAuthEncoded = config.basicAuthEncoded()
        if (basicAuthEncoded != null) registry.addInterceptor(AuthHandler(basicAuthEncoded))
    }


    private class AuthHandler(val basicAuthEncoded: String) : HandlerInterceptor {

        override fun preHandle(req: HttpServletRequest?, res: HttpServletResponse?, handler: Any?): Boolean {
            val authorized = req!!.getHeader("Authorization") == "Basic $basicAuthEncoded"
            if (!authorized) res!!.sendError(HttpStatus.UNAUTHORIZED.value())
            return authorized
        }


        override fun postHandle(req: HttpServletRequest?, res: HttpServletResponse?, handler: Any?, modelAndView: ModelAndView?) {
            // nothing to do
        }

        override fun afterCompletion(req: HttpServletRequest?, res: HttpServletResponse?, handler: Any?, e: Exception?) {
            // nothing to do
        }

    }
}