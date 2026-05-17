package wms.sandeliukas.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MetricsService integraciniai testai.
 *
 * Naudoja H2 in-memory duomenų bazę (application-test.properties).
 * Patikrina, kad visi metrics metodai grąžina tinkamas reikšmes.
 *
 * Paleidimas: mvn test  arba  mvn test -Dspring.profiles.active=test
 */
@SpringBootTest
@ActiveProfiles("test")
class MetricsServiceTest {

    @Autowired
    private MetricsService metricsService;

    // ── Atminties testai ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Heap naudojimas turi būti teigiamas")
    void heapUsedMbIsPositive() {
        assertThat(metricsService.getHeapUsedMb()).isPositive();
    }

    @Test
    @DisplayName("Heap maksimumas turi būti didesnis už naudojamą")
    void heapMaxIsGreaterThanUsed() {
        assertThat(metricsService.getHeapMaxMb()).isGreaterThanOrEqualTo(metricsService.getHeapUsedMb());
    }

    @Test
    @DisplayName("Heap naudojimo procentas turi būti 0–100")
    void heapUsagePercentInRange() {
        int pct = metricsService.getHeapUsagePercent();
        assertThat(pct).isBetween(0, 100);
    }

    @Test
    @DisplayName("Non-Heap naudojimas turi būti teigiamas")
    void nonHeapUsedMbIsPositive() {
        assertThat(metricsService.getNonHeapUsedMb()).isPositive();
    }

    // ── Greitaveikos testai ───────────────────────────────────────────────────

    @Test
    @DisplayName("Veikimo laikas neturi būti tuščias")
    void uptimeFormattedNotBlank() {
        assertThat(metricsService.getUptimeFormatted()).isNotBlank();
    }

    @Test
    @DisplayName("Gijų skaičius turi būti teigiamas")
    void threadCountIsPositive() {
        assertThat(metricsService.getThreadCount()).isPositive();
    }

    @Test
    @DisplayName("CPU branduolių skaičius turi būti teigiamas")
    void processorsIsPositive() {
        assertThat(metricsService.getAvailableProcessors()).isPositive();
    }

    // ── Sistemos informacijos testai ──────────────────────────────────────────

    @Test
    @DisplayName("Java versija neturi būti tuščia")
    void javaVersionNotBlank() {
        assertThat(metricsService.getJavaVersion()).isNotBlank();
    }

    @Test
    @DisplayName("Spring Boot versija neturi būti tuščia")
    void springBootVersionNotBlank() {
        assertThat(metricsService.getSpringBootVersion()).isNotBlank();
    }

    @Test
    @DisplayName("Build įrankis turi būti Apache Maven")
    void buildToolIsMaven() {
        assertThat(metricsService.getBuildTool()).isEqualTo("Apache Maven");
    }

    // ── DB skaičių testai (su H2 tuščia DB) ──────────────────────────────────

    @Test
    @DisplayName("Produktų skaičius turi būti >= 0")
    void productCountNonNegative() {
        assertThat(metricsService.getProductCount()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("Vartotojų skaičius turi būti >= 0")
    void userCountNonNegative() {
        assertThat(metricsService.getUserCount()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("Pirkimų skaičius turi būti >= 0")
    void purchaseCountNonNegative() {
        assertThat(metricsService.getPurchaseCount()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("Komentarų skaičius turi būti >= 0")
    void commentCountNonNegative() {
        assertThat(metricsService.getCommentCount()).isGreaterThanOrEqualTo(0);
    }

    // ── Saugumo testai ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Apsaugotų maršrutų skaičius turi būti > 0")
    void protectedRoutesPositive() {
        assertThat(metricsService.getProtectedRouteCount()).isPositive();
    }

    @Test
    @DisplayName("Autentifikacijos mechanizmas neturi būti tuščias")
    void authMechanismNotBlank() {
        assertThat(metricsService.getAuthMechanism()).isNotBlank();
    }
}
