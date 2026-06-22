package vn.ezisolutions.cloud.facebook_service.services.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenCryptoServiceTest {

    @Test
    void encryptAndDecryptRoundTrip() {
        TokenCryptoService service = new TokenCryptoService("test-secret");

        String encrypted = service.encrypt("facebook-token");
        String decrypted = service.decrypt(encrypted);

        assertNotEquals("facebook-token", encrypted);
        assertEquals("facebook-token", decrypted);
    }

    @Test
    void blankTokenIsReturnedAsIs() {
        TokenCryptoService service = new TokenCryptoService("test-secret");

        assertEquals("", service.encrypt(""));
        assertNull(service.decrypt(null));
    }
}
