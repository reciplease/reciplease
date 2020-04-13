package org.reciplease.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Profile("local")
@Configuration
@EnableWebSecurity
public class LocalSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(final HttpSecurity security) throws Exception {
        security.httpBasic().disable()
                .csrf().disable();

        security.headers().frameOptions().disable();
    }
}