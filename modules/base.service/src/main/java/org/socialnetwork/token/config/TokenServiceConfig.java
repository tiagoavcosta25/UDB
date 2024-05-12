package org.socialnetwork.token.config;

import org.socialnetwork.token.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TokenServiceConfig {
    private final ApplicationContext context;
    private final String tokenServiceBean;

    @Autowired
    public TokenServiceConfig(ApplicationContext context, @Value("${token.service.bean}") String tokenServiceBean) {
        this.context = context;
        this.tokenServiceBean = tokenServiceBean;
    }

    @Bean
    @Primary
    public TokenService tokenService() {
        return (TokenService) context.getBean(tokenServiceBean);
    }
}
