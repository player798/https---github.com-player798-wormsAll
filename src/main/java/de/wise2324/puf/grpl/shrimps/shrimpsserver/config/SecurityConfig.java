package de.wise2324.puf.grpl.shrimps.shrimpsserver.config;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.service.PlayerDetails;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.service.PlayerDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private PlayerDetailsService userDetailsService;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http)
                                        throws Exception{
        http
                .cors().and()
                .csrf().disable()
                // .cors(AbstractHttpConfigurer::disable)
                // .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/authenticate/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login.html")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler( (request, response, authentication) -> {
                            PlayerDetails playerDetails = (PlayerDetails) authentication.getPrincipal();
                            userDetailsService.loginUser(playerDetails.getUsername());
                        })
                        .defaultSuccessUrl("/index_choice.html", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessHandler((request, response, authentication) -> {
                            PlayerDetails playerDetails = (PlayerDetails) authentication.getPrincipal();
                            userDetailsService.logoutUser(playerDetails.getUsername());
                        })
                        .permitAll()
                );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("http://127.0.0.1:5173")); // Frontend URL
        configuration.setAllowedOriginPatterns(Arrays.asList("*[*]")); // Frontend URL
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("content-type", "authorization","application/json","text/plain", "text/html","*"));
        configuration.setAllowCredentials(true); // Enable credentials support
        configuration.setExposedHeaders(Collections.singletonList("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return PasswordEncoderFactories
                .createDelegatingPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }
    
    @Bean
    protected AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
        throws Exception {

        return authConfig.getAuthenticationManager();
    }
}
