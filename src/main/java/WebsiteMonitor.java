import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import io.github.cdimascio.dotenv.Dotenv;

public class WebsiteMonitor {
    private static final boolean DEBUG_MODE = true;
    /**
     * The Dotenv.load() call by default looks for a .env file in your working directory.
     * If you’re using a different filename (like .env.local), you can load it like so:
     * {@code private static final Dotenv dotenv = Dotenv.configure().filename(".env.local").load();}
     */
    private static final Dotenv dotenv = Dotenv.load();
    private static final String BOT_TOKEN = dotenv.get("BOT_TOKEN");
    private static final String CHAT_ID = dotenv.get("CHAT_ID");

    // Read websites from the .env file, split by comma, and trim whitespace.
    private static final List<String> WEBSITES = Arrays.stream(dotenv.get("WEBSITES").split(","))
            .map(String::trim)
            .toList();
    private static final Map<String, Integer> failureCounts = new HashMap<>();

    //Check if variables are set
    static {
        if (BOT_TOKEN == null || CHAT_ID == null) {
            System.err.println("Error: BOT_TOKEN or CHAT_ID environment variable is not set.");
            System.exit(1);
        }
    }

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

    /**
     * Replace the SendTelegramAlert() in checkWebsiteAndNotify() with this run tests without a working Telegram connection.
     */
    public static void mockSendTelegramAlert(String message) {
        System.out.println("Mock Alert: " + message);
    }
}