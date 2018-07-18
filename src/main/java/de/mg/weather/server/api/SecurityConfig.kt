package de.mg.weather.server.api

import de.mg.weather.server.conf.WeatherConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.stereotype.Component

@Profile("!test")
@Component
class SecurityConfig : WebSecurityConfigurerAdapter() {

    @Autowired
    private lateinit var config: WeatherConfig

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
                .csrf()
                .disable()
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .httpBasic()
                .realmName("weather")
    }

    @Throws(Exception::class)
    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.inMemoryAuthentication()
                .withUser(config.basicAuthUser).password(config.basicAuthPassword).roles("USER")
    }

}