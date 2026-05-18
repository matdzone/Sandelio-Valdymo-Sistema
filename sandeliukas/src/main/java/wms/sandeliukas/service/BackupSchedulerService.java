package wms.sandeliukas.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Rezervinio kopijavimo servisas.
 *
 * Kasdien 02:00 vykdo mysqldump į /backups/ katalogą.
 * Aktyvuojamas per @EnableScheduling (SandeliukasApplication).
 */
@Service
public class BackupSchedulerService {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String BACKUP_DIR  = "/backups/";
    private static final String DB_HOST     = "stud.if.ktu.lt";
    private static final String DB_PORT     = "20001";
    private static final String DB_USER     = "armarl";
    private static final String DB_PASSWORD = "armarl";
    private static final String DB_NAME     = "armarl";

    /**
     * Paleidžiama kiekvieną dieną 02:00.
     * Cron formatas: sekundė minutė valanda dienaMėn mėnuo dienaSav
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
     * Vykdo mysqldump komandą ir išsaugo .sql failą į BACKUP_DIR.
     * Prieš paleidžiant įsitikinkite, kad mysqldump įdiegtas serveryje
     * ir /backups/ katalogas egzistuoja bei yra įrašomas.
     */
    private void runBackup() throws Exception {
        new File(BACKUP_DIR).mkdirs();

        String filename = BACKUP_DIR + DB_NAME + "_" + LocalDate.now() + ".sql";

        ProcessBuilder pb = new ProcessBuilder(
                "mysqldump",
                "-h", DB_HOST,
                "-P", DB_PORT,
                "-u", DB_USER,
                "-p" + DB_PASSWORD,
                DB_NAME,
                "-r", filename
        );

        pb.redirectErrorStream(true);

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("mysqldump baigėsi su klaidos kodu: " + exitCode);
        }

        System.out.printf("[BACKUP] Failas išsaugotas: %s%n", filename);
    }
}
