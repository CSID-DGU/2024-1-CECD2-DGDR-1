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
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers("/api/v1/user/auth/**", "/ws/*").permitAll() // '/api/v1/user/auth' 하위 경로 허용
                                .anyRequest().authenticated() // 다른 모든 요청은 인증 필요
                )
                .addFilterBefore(jwtTokenAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headerConfig ->
                        headerConfig.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
                );
        return http.build();
    }
}
