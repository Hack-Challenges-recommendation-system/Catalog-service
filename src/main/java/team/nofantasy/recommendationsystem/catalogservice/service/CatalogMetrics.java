package team.nofantasy.recommendationsystem.catalogservice.service;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import team.nofantasy.recommendationsystem.catalogservice.repository.MaterialRepository;

@Component
public class CatalogMetrics {
    public CatalogMetrics(MeterRegistry registry, MaterialRepository repository) {
        Gauge.builder("catalog.materials", repository, MaterialRepository::count)
                .description("Number of imported catalog materials")
                .register(registry);
    }
}
