package team.nofantasy.recommendationsystem.catalogservice.dto.controller.material;

import team.nofantasy.recommendationsystem.catalogservice.entity.Material;

import java.util.List;

/**
 * A material as clients see it. For a paid material {@code summary} is a short benefit; {@code url} is the provided link.
 * {@code paid} is true for every material that isn't free; {@code access} is {@code FREE}, {@code SUBSCRIPTION} (part of
 * a subscription) or {@code PURCHASE} (bought separately).
 */
public record MaterialControllerDto(
        String id,
        String title,
        String summary,
        String format,
        Integer durationMinutes,
        String level,
        List<String> topics,
        List<String> businessStages,
        List<String> spheres,
        Boolean paid,
        Material.Access access,
        String url
) {
}
