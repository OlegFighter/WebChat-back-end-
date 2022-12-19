package webchat.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import webchat.AuthenticationService.JPAUserDetailsService;

import java.util.List;

import static org.springframework.boot.context.properties.bind.Bindable.listOf;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties
public class WebConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    JPAUserDetailsService userDetailsService;
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                //Cors configuration
                .cors().and()
                .csrf().disable().httpBasic()
                .and().sessionManagement().disable()

        //authentication configuration
        .authorizeRequests().antMatchers("/sign_up").permitAll()
        .anyRequest().authenticated();
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> simpleCorsFilter() {
        var config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(
                List.of(

                        "http://localhost:3000",
                        "http://localhost:3001",
                        "http://localhost:8080",
                        "http://www.rebol.net/"
                )
        );
        config.setAllowedMethods(List.of("GET", "HEAD", "POST", "PUT", "DELETE", "PATCH"));
        config.setAllowedHeaders(List.of("Content-Types", "Content-Type", "authorization", "x-auth-token"));
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        var bean = new FilterRegistrationBean<CorsFilter>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(AuthenticationManagerBuilder builder)
            throws Exception {
        builder.userDetailsService(userDetailsService);
    }
}
