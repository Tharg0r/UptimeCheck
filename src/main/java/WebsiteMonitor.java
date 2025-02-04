import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WebsiteMonitor {
    private static final boolean DEBUG_MODE = true;
    private static final String BOT_TOKEN = "REMOVED";
    private static final String CHAT_ID = "REMOVED";
    private static final List<String> WEBSITES = List.of(
            "REMOVED",
            "REMOVED",
            "REMOVED",
            "REMOVED"
    );
    private static final Map<String, Integer> failureCounts = new HashMap<>();

    public static void main(String[] args) {
        testTelegramConnection();

        System.out.println("\nMonitoring websites ...");

        WEBSITES.forEach(site -> failureCounts.put(site, 0));

        var scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            WEBSITES.parallelStream().forEach(website -> {
                try {
                    checkWebsiteAndNotify(website);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendTelegramAlert("Caught Exception at checkWebsiteAndNotify(): " + e.getMessage());
                }
            });
        }, 0, 1, TimeUnit.MINUTES);
    }

    protected static void checkWebsiteAndNotify(String website) {
        int code = -1; // Default value for when no code is available
        try {
            code = getResponseCode(website);
            if (code < 200 || code >= 400) {
                int failures = failureCounts.getOrDefault(website, 0) + 1;
                failureCounts.put(website, failures);
                System.out.println("[\033[31mE\033[0m] " + website + " is down with code: " + code + ". Failure Count: " + failures);
                if (failures >= 5) {
                    sendTelegramAlert(website + " is down with code: " + code);
                    //failureCounts.put(website, 0);
                }
            } else {
                failureCounts.put(website, 0);
                System.out.println(website + " is up with code: " + code);
            }
        } catch (java.net.SocketTimeoutException e) {
            int failures = failureCounts.getOrDefault(website, 0) + 1;
            failureCounts.put(website, failures);
            if (failures >= 5) {
                sendTelegramAlert(website + " is down. Connection timed out after " + failures + " attempts.");
            }
        } catch (Exception e) {
            int failures = failureCounts.getOrDefault(website, 0) + 1;
            failureCounts.put(website, failures);
            if (failures >= 5) {
                sendTelegramAlert(website + " is down. Error: " + e.getMessage() + ". Failed " + failures + " times.");
            }
        }
    }

    protected static int getResponseCode(String website) throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(website);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.connect();
            return conn.getResponseCode();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static boolean sendTelegramAlert(String message) {
        HttpURLConnection conn = null;
        try {
            String urlStr = "https://api.telegram.org/bot" + BOT_TOKEN
                    + "/sendMessage?chat_id=" + CHAT_ID
                    + "&text=" + URLEncoder.encode(message, "UTF-8");
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("Failed to send Telegram alert. Response code: " + responseCode);
                try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getErrorStream()))) {
                    String line;
                    StringBuilder response = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    System.err.println("Error response: " + response.toString());
                }
                return false;
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error sending Telegram alert: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static void logDebug(String message) {
        if (DEBUG_MODE) {
            System.out.println("[DEBUG] " + message);
        }
    }

    public static void testTelegramConnection() {
        System.out.println("Testing Telegram connection...");
        if (sendTelegramAlert("Test message from Website Monitor")) {
            System.out.println("[\033[32m✓\033[0m] Telegram connection test successful!");
        } else {
            System.out.println("[\033[31m✗\033[0m] Telegram connection test failed!");
            System.exit(1); // Optional: exit if Telegram connection fails
        }
    }

    //replace in checkWebsiteAndNotify to see output in tests.
    public static void mockSendTelegramAlert(String message) {
        System.out.println("Mock Alert: " + message);
    }
}