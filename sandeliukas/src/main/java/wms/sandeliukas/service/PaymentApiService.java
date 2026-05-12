package wms.sandeliukas.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class PaymentApiService {

    private static final int SUCCESS_THRESHOLD = 85;
    private final Random random = new Random();

    public PaymentResult requestServicePurchase(Integer purchaseId, String buyerEmail, double totalAmount) {
        simulateNetworkDelay();

        int roll = random.nextInt(100) + 1;
        boolean success = roll <= SUCCESS_THRESHOLD;

        if (success) {
            return new PaymentResult(
                    true,
                    "PAYMENT_APPROVED",
                    "Mokėjimas patvirtintas. Transakcija: TXN-" + purchaseId + "-" + System.currentTimeMillis() % 100000
            );
        } else {
            return new PaymentResult(false, "PAYMENT_DECLINED", selectFailureReason(roll));
        }
    }

    private String selectFailureReason(int roll) {
        if (roll <= 90) return "Mokėjimas atmestas: nepakankamas sąskaitos likutis.";
        if (roll <= 95) return "Mokėjimas atmestas: banko autorizacijos klaida.";
        return "Mokėjimas atmestas: kortelės duomenys neteisingi.";
    }

    private void simulateNetworkDelay() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static class PaymentResult {
        private final boolean successful;
        private final String code;
        private final String message;

        public PaymentResult(boolean successful, String code, String message) {
            this.successful = successful;
            this.code = code;
            this.message = message;
        }

        public boolean isSuccessful() { return successful; }
        public String getCode()       { return code; }
        public String getMessage()    { return message; }
    }
}
