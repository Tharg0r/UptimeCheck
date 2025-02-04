import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Tests {

    @Test
    void testGetResponseCode_withValidWebsite() {
        try {
            int code = WebsiteMonitor.getResponseCode("https://www.example.com");
            assertTrue(code >= 200 && code < 400, "Expected a valid HTTP status code.");
        } catch (Exception e) {
            fail("Exception should not have occurred for a valid website: " + e.getMessage());
        }
    }

    @Test
    void testGetResponseCode_withInvalidWebsite() {
        assertThrows(Exception.class, () -> {
            WebsiteMonitor.getResponseCode("https://invalid-url");
        }, "Expected an exception for an invalid URL.");
    }

    @Test
    void testCheckWebsiteAndNotify_withFailingWebsite() {
        String failingWebsite = "https://invalid-url";
        WebsiteMonitor.checkWebsiteAndNotify(failingWebsite);
        // Verify output to confirm alert was sent
    }

    @Test
    void testCheckWebsiteAndNotify_withHealthyWebsite() {
        String healthyWebsite = "https://www.example.com";
        WebsiteMonitor.checkWebsiteAndNotify(healthyWebsite);
        // Verify no alerts sent
    }
}