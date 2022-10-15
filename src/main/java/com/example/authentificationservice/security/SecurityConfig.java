package com.example.authentificationservice.security;


import com.example.authentificationservice.filter.CustomAuthenticationFilter;
import com.example.authentificationservice.filter.CustomAuthorizationFilter;
import com.example.authentificationservice.service.UserDetailsServiceImp;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration @EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final UserDetailsServiceImp userDetailsService;

    public SecurityConfig(UserDetailsServiceImp userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(authenticationManagerBean());
        customAuthenticationFilter.setFilterProcessesUrl("/api/login");
        http.csrf().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//        http.authorizeRequests().antMatchers(HttpMethod.GET, "/api/login/**", "/api/token/refresh/**").permitAll();
//        http.authorizeRequests().antMatchers(HttpMethod.GET, "/api/roles/**").hasAnyAuthority("ROLE_ADMIN");
//        http.authorizeRequests().antMatchers(HttpMethod.POST, "/api/roles/**").hasAnyAuthority("ROLE_ADMIN");
//        http.authorizeRequests().antMatchers(HttpMethod.GET, "/api/users/**").hasAnyAuthority("ROLE_ADMIN");
//        http.authorizeRequests().antMatchers(HttpMethod.POST, "/api/users/**").hasAnyAuthority("ROLE_ADMIN");
//        http.authorizeRequests().antMatchers(HttpMethod.DELETE, "/api/users/**").hasAnyAuthority("ROLE_LIBRARY_OWNER", "ROLE_ADMIN");
//        http.authorizeRequests().antMatchers(HttpMethod.PATCH, "/api/users/**").hasAnyAuthority("ROLE_LIBRARY_OWNER", "ROLE_ADMIN");
        http.authorizeRequests().anyRequest().authenticated();
        http.addFilter(customAuthenticationFilter);
        http.addFilterBefore(new CustomAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
