package team.nofantasy.recommendationsystem.catalogservice.importer;

import org.junit.jupiter.api.Test;
import team.nofantasy.recommendationsystem.catalogservice.dto.service.importer.MaterialImportDto;
import team.nofantasy.recommendationsystem.catalogservice.entity.Material;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogImporterTest {
    private static MaterialImportDto row(String summary, Boolean paid) {
        return row(summary, paid, null);
    }

    private static MaterialImportDto row(String summary, Boolean paid, String access) {
        return new MaterialImportDto(
                " m-1 ", " Title ", summary, "VIDEO", 10, "BEGINNER", null, null, null, paid, access,
                " https://example.com/m-1 "
        );
    }

    @Test
    void cutsSummaryOfPaidMaterialToShortBenefit() {
        Material material = CatalogImporter.toMaterial(row("а".repeat(1000), true));

        assertThat(material.getSummary()).hasSize(CatalogImporter.PAID_SUMMARY_LIMIT).endsWith("…");
        assertThat(material.getPaid()).isTrue();
    }

    @Test
    void keepsFullSummaryOfFreeMaterial() {
        Material material = CatalogImporter.toMaterial(row("а".repeat(1000), false));

        assertThat(material.getSummary()).hasSize(1000);
        assertThat(material.getAccess()).isEqualTo(Material.Access.FREE);
    }

    @Test
    void trimsTextAndTreatsMissingValuesAsEmpty() {
        Material material = CatalogImporter.toMaterial(row("short", null));

        assertThat(material.getId()).isEqualTo("m-1");
        assertThat(material.getTitle()).isEqualTo("Title");
        assertThat(material.getUrl()).isEqualTo("https://example.com/m-1");
        assertThat(material.getPaid()).as("missing paid flag means free").isFalse();
        assertThat(material.getAccess()).isEqualTo(Material.Access.FREE);
        assertThat(material.getTopics()).isEmpty();
        assertThat(material.getBusinessStages()).isEmpty();
        assertThat(material.getSpheres()).isEmpty();
    }

    @Test
    void subscriptionMaterialIsPaidAndShowsOnlyShortBenefit() {
        Material material = CatalogImporter.toMaterial(row("а".repeat(1000), false, " subscription "));

        assertThat(material.getAccess()).isEqualTo(Material.Access.SUBSCRIPTION);
        assertThat(material.getPaid()).as("access wins over the paid flag").isTrue();
        assertThat(material.getSummary()).hasSize(CatalogImporter.PAID_SUMMARY_LIMIT);
    }

    @Test
    void paidMaterialWithoutKnownAccessIsBoughtSeparately() {
        assertThat(CatalogImporter.toMaterial(row("short", true)).getAccess()).isEqualTo(Material.Access.PURCHASE);
        assertThat(CatalogImporter.toMaterial(row("short", true, "LIFETIME")).getAccess())
                .isEqualTo(Material.Access.PURCHASE);
        assertThat(CatalogImporter.toMaterial(row("short", false, "free")).getAccess())
                .isEqualTo(Material.Access.FREE);
    }
}
