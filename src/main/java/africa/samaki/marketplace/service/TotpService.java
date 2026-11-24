package africa.samaki.marketplace.service;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Service
public class TotpService {

    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int TIME_STEP_SECONDS = 30;
    private static final int CODE_DIGITS = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a 20-byte random secret key encoded in Base32.
     */
    public String generateSecret() {
        byte[] buffer = new byte[20];
        RANDOM.nextBytes(buffer);
        return encodeBase32(buffer);
    }

    /**
     * Generates a list of one-time backup recovery codes.
     */
    public List<String> generateBackupCodes(int count) {
        List<String> codes = new ArrayList<>(count);
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        for (int i = 0; i < count; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < 8; j++) {
                if (j == 4) sb.append('-');
                sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
            }
            codes.add(sb.toString());
        }
        return codes;
    }

    /**
     * Verifies a 6-digit code against the TOTP secret key, with a 1-step clock drift allowance.
     */
    public boolean verifyCode(String secret, String inputCode) {
        if (secret == null || inputCode == null || inputCode.trim().length() != CODE_DIGITS) {
            return false;
        }

        try {
            byte[] keyBytes = decodeBase32(secret.trim().toUpperCase());
            long currentWindow = System.currentTimeMillis() / 1000L / TIME_STEP_SECONDS;

            // Check current window, -1 window (30s past), +1 window (30s future)
            for (int i = -1; i <= 1; i++) {
                long window = currentWindow + i;
                String expected = generateTotpForWindow(keyBytes, window);
                if (expected.equals(inputCode.trim())) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * Constructs the standard otpauth URL for Google Authenticator / Microsoft Authenticator / Authy.
     */
    public String getTotpUri(String email, String secret) {
        return String.format("otpauth://totp/SamakiAfrica:%s?secret=%s&issuer=SamakiAfrica&digits=6&period=30",
                email, secret);
    }

    private String generateTotpForWindow(byte[] keyBytes, long window) throws Exception {
        byte[] data = ByteBuffer.allocate(8).putLong(window).array();
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(keyBytes, "HmacSHA1"));
        byte[] hash = mac.doFinal(data);

        int offset = hash[hash.length - 1] & 0x0F;
        int binary = ((hash[offset] & 0x7F) << 24) |
                     ((hash[offset + 1] & 0xFF) << 16) |
                     ((hash[offset + 2] & 0xFF) << 8) |
                     (hash[offset + 3] & 0xFF);

        int otp = binary % 1000000;
        return String.format("%06d", otp);
    }

    private String encodeBase32(byte[] data) {
        StringBuilder result = new StringBuilder((data.length * 8 + 4) / 5);
        int buffer = 0;
        int next = 0;
        int bitsLeft = 0;
        while (next < data.length) {
            buffer <<= 8;
            buffer |= (data[next++] & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                bitsLeft -= 5;
                result.append(BASE32_CHARS.charAt((buffer >> bitsLeft) & 0x1F));
            }
        }
        if (bitsLeft > 0) {
            buffer <<= (5 - bitsLeft);
            result.append(BASE32_CHARS.charAt(buffer & 0x1F));
        }
        return result.toString();
    }

    private byte[] decodeBase32(String base32) {
        String clean = base32.replaceAll("[^A-Z2-7]", "");
        ByteBuffer bytes = ByteBuffer.allocate((clean.length() * 5) / 8);
        int buffer = 0;
        int bitsLeft = 0;
        for (char c : clean.toCharArray()) {
            int val = BASE32_CHARS.indexOf(c);
            if (val < 0) continue;
            buffer <<= 5;
            buffer |= (val & 0x1F);
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                bitsLeft -= 8;
                bytes.put((byte) ((buffer >> bitsLeft) & 0xFF));
            }
        }
        return bytes.array();
    }
}
