package team.nofantasy.recommendationsystem.catalogservice.dto.service.importer;

import java.util.List;

/**
 * One material in the import file: a JSON array of these objects. The case-data service of the compose file writes it
 * from the case dump; the mapping lives in {@code case-data/catalog.sql} of the Documentation repository.
 *
 * @param access {@code FREE}, {@code SUBSCRIPTION} or {@code PURCHASE}; when it is missing or unknown, {@code paid}
 *               decides between {@code PURCHASE} and {@code FREE}.
 */
public record MaterialImportDto(
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
        String access,
        String url
) {
}
