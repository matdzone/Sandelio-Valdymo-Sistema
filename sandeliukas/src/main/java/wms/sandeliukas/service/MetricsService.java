package wms.sandeliukas.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.SpringBootVersion;
import org.springframework.stereotype.Service;
import wms.sandeliukas.repositories.CommentRepository;
import wms.sandeliukas.repositories.ProductRepository;
import wms.sandeliukas.repositories.PurchaseRepository;
import wms.sandeliukas.repositories.ShoppingCartItemRepository;
import wms.sandeliukas.repositories.UserRepository;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.TimeUnit;

@Service
public class MetricsService {

    private final MeterRegistry              meterRegistry;
    private final MemoryMXBean               memoryMXBean;
    private final RuntimeMXBean              runtimeMXBean;
    private final ProductRepository          productRepository;
    private final UserRepository             userRepository;
    private final PurchaseRepository         purchaseRepository;
    private final CommentRepository          commentRepository;
    private final ShoppingCartItemRepository cartItemRepository;

    public MetricsService(MeterRegistry meterRegistry,
                          ProductRepository productRepository,
                          UserRepository userRepository,
                          PurchaseRepository purchaseRepository,
                          CommentRepository commentRepository,
                          ShoppingCartItemRepository cartItemRepository) {
        this.meterRegistry       = meterRegistry;
        this.productRepository   = productRepository;
        this.userRepository      = userRepository;
        this.purchaseRepository  = purchaseRepository;
        this.commentRepository   = commentRepository;
        this.cartItemRepository  = cartItemRepository;
        this.memoryMXBean        = ManagementFactory.getMemoryMXBean();
        this.runtimeMXBean       = ManagementFactory.getRuntimeMXBean();
    }

    // ── Atmintis ─────────────────────────────────────────────────────────────

    public long getHeapUsedMb() {
        return memoryMXBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
    }

    public long getHeapMaxMb() {
        return memoryMXBean.getHeapMemoryUsage().getMax() / (1024 * 1024);
    }

    public long getNonHeapUsedMb() {
        return memoryMXBean.getNonHeapMemoryUsage().getUsed() / (1024 * 1024);
    }

    public int getHeapUsagePercent() {
        long used = memoryMXBean.getHeapMemoryUsage().getUsed();
        long max  = memoryMXBean.getHeapMemoryUsage().getMax();
        if (max <= 0) return 0;
        return (int) (used * 100 / max);
    }

    // ── Greitaveika ──────────────────────────────────────────────────────────

    public String getUptimeFormatted() {
        long s   = runtimeMXBean.getUptime() / 1000;
        long h   = s / 3600;
        long m   = (s % 3600) / 60;
        long sec = s % 60;
        return String.format("%dh %dm %ds", h, m, sec);
    }

    public long getTotalHttpRequests() {
        try {
            return (long) meterRegistry.find("http.server.requests")
                    .timers().stream()
                    .mapToDouble(Timer::count).sum();
        } catch (Exception e) { return -1; }
    }

    public double getAvgHttpRequestMs() {
        try {
            return meterRegistry.find("http.server.requests")
                    .timers().stream()
                    .filter(t -> t.count() > 0)
                    .mapToDouble(t -> t.mean(TimeUnit.MILLISECONDS))
                    .average().orElse(0.0);
        } catch (Exception e) { return -1; }
    }

    public long getErrorHttpRequests() {
        try {
            return (long) meterRegistry.find("http.server.requests")
                    .tag("status", "500")
                    .timers().stream()
                    .mapToDouble(Timer::count).sum();
        } catch (Exception e) { return -1; }
    }

    public int getThreadCount() {
        try { return ManagementFactory.getThreadMXBean().getThreadCount(); }
        catch (Exception e) { return -1; }
    }

    public int getAvailableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

    // ── Duomenu kiekis (DB) ───────────────────────────────────────────────────

    public long getProductCount() {
        try { return productRepository.count(); }
        catch (Exception e) { return -1; }
    }

    public long getUserCount() {
        try { return userRepository.count(); }
        catch (Exception e) { return -1; }
    }

    public long getPurchaseCount() {
        try { return purchaseRepository.count(); }
        catch (Exception e) { return -1; }
    }

    public long getCommentCount() {
        try { return commentRepository.count(); }
        catch (Exception e) { return -1; }
    }

    public long getCartItemCount() {
        try { return cartItemRepository.count(); }
        catch (Exception e) { return -1; }
    }

    // ── Atnaujinimo galimybes ────────────────────────────────────────────────

    public String getSpringBootVersion() {
        return SpringBootVersion.getVersion();
    }

    public String getJavaVersion() {
        return System.getProperty("java.version");
    }

    public String getJavaVendor() {
        return System.getProperty("java.vendor");
    }

    public String getBuildTool() {
        return "Apache Maven";
    }

    /** Siulomos atnaujinimo priemones */
    public String getUpdateStrategy() {
        return "mvn versions:display-dependency-updates";
    }

    // ── Saugumas ─────────────────────────────────────────────────────────────

    /** Apsaugotu marsrutu kiekis (sesija tikrinama rankiniu budu) */
    public int getProtectedRouteCount() {
        // /customer/**, /admin/**, /warehouse/** = ~15+ marsrutu
        return 15;
    }

    public String getAuthMechanism() {
        return "HttpSession (sesiju autentifikacija)";
    }

    public String getSessionStrategy() {
        return "El. pastas saugomas sesijoje, tikrinamas kiekviename valdiklyje";
    }

    public String getPasswordStorage() {
        return "Slaptazodis tikrinamas per AuthService (DB)";
    }
}
