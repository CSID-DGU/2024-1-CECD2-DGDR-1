package dgdr.server.vonage.global.config;

import dgdr.server.vonage.global.security.JwtTokenAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final JwtTokenAuthFilter jwtTokenAuthFilter;

    public SecurityConfig(JwtTokenAuthFilter jwtTokenAuthFilter) {
        this.jwtTokenAuthFilter = jwtTokenAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtTokenAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(headerConfig ->
                headerConfig.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
            );
        return http.build();
    }
}
