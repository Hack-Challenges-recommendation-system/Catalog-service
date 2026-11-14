package team.nofantasy.recommendationsystem.catalogservice.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CatalogProperties.class)
public class CatalogConfiguration {
}
