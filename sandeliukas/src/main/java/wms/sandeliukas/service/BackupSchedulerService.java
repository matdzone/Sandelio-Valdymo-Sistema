package wms.sandeliukas.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Rezervinio kopijavimo servisas.
 *
 * Kasdien 02:00 registruoja backup įvykį.
 * Produkcijoje šis metodas turėtų iškviesti išorinę mysqldump komandą
 * arba perduoti užduotį debesų saugyklos API (pvz., S3 / universiteto NAS).
 *
 * Aktyvuojamas per @EnableScheduling (SandeliukasApplication).
 */
@Service
public class BackupSchedulerService {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Paleisti kiekvieną dieną 02:00.
     * Cron formatas: sekunde minutė valanda dienaMen mėnuo dienaSav
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void scheduledBackup() {
        String ts = LocalDateTime.now().format(FMT);
        System.out.printf("[BACKUP] %s — Rezervinio kopijavimo procesas pradėtas.%n", ts);

        try {
            runBackup();
            System.out.printf("[BACKUP] %s — Kopija sėkmingai sukurta.%n", ts);
        } catch (Exception e) {
            System.err.printf("[BACKUP] %s — KLAIDA kuriant kopiją: %s%n", ts, e.getMessage());
        }
    }

    /**
     * Vykdo backup logiką.
     * Keitimuisi į produkciją: pakeisti System.out į realią mysqldump komandą.
     *
     * Pavyzdys (Production):
     *   Runtime.getRuntime().exec(new String[]{
     *       "mysqldump", "-h", "stud.if.ktu.lt", "-P", "20001",
     *       "-u", "armarl", "-parmarl", "armarl",
     *       "-r", "/backups/armarl_" + LocalDate.now() + ".sql"
     *   });
     */
    private void runBackup() {
        // Demonstracijai – išveda žinutę.
        // Tikroje aplinkoje: iškviesti mysqldump arba siųsti į NAS/S3.
        System.out.println("[BACKUP] (Demo) Duomenų bazės kopija įrašyta į /backups/");
    }
}
