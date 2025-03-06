# UptimeCheck

A simple Java application to monitor the availability of specified websites and send Telegram alerts if they become unavailable. This project uses environment variables (via a `.env` file and [dotenv-java](https://github.com/cdimascio/dotenv-java)) to keep sensitive information private.

---

## Table of Contents
1. [Features](#features)
2. [Planned Features](#planned-features)
3. [Requirements](#requirements)
4. [Setup](#setup)
5. [Usage](#usage)
6. [Repository Layout](#repository-layout)
7. [Contributing](#contributing)
8. [License](#license)

---

## Features

- **Periodic Website Checks**: Monitors multiple websites at specified intervals.  
- **Telegram Alerts**: Notifies you via Telegram if a site becomes unreachable.  
- **Environment-Based Configuration**: Keeps sensitive data like tokens and URLs out of source control.

---

## Planned Features

- **Notifications with Steps to Resolve Each Error Code**  
  Provide recommended troubleshooting steps for each specific HTTP or network error, helping users quickly address the underlying issues.

- **Optional Incremental Backoff Retries**  
  Configure the app so that each failure extends the wait time before the next check. This prevents excessive alerts if a website remains offline for a prolonged period and can be toggled in a config file.

- **Offline Duration Tracking**  
  Keep track of how long a website has been offline and include the duration in every alert message.

- **Multiple Alert Channels**  
  Expand beyond Telegram by adding support for Email, Discord, Slack, and SMS notifications.

- **Centralized Configuration**  
  Store intervals, thresholds, backoff options, and other settings in a dedicated configuration file (or `.env`) for easy customization.

- **Response Time Monitoring**  
  Record how quickly each website responds, and optionally send alerts if response times exceed a certain threshold.

- **Historical Data Logging**  
  Persist downtime events and response times to a database or file, enabling later analysis and reporting on long-term trends.

- **Per-Website Failure Thresholds**  
  Allow different websites to have different failure counts before triggering an alert (e.g., critical sites may require immediate alerting, while others might allow more downtime).

- **GUI / Web Dashboard**  
  Provide a user-friendly interface to view current status, response times, and historical outage data, making it easier to track overall performance at a glance.

---

## Requirements

- **Java 17** (or higher)
- **Gradle** (or Maven) for building the project
- A **Telegram Bot token** (obtained from [BotFather](https://core.telegram.org/bots#6-botfather))
- A **Telegram chat ID** (the ID of the bot or group where you want to receive messages)

---

## Setup

1. **Clone or Fork the Repository**  
    ```bash
    git clone https://github.com/your-username/UptimeCheck.git
    cd UptimeCheck
    ```

2. **Add Dependencies**  
   - **Gradle**: Add to `build.gradle`:
     ```groovy
     dependencies {
         implementation 'io.github.cdimascio:dotenv-java:3.2.0'
         // ...other dependencies...
     }
     ```
   - **Maven**: Add to `pom.xml`:
     ```xml
     <dependency>
       <groupId>io.github.cdimascio</groupId>
       <artifactId>dotenv-java</artifactId>
       <version>3.2.0</version>
     </dependency>
     ```

3. **Create a `.env` File**  
   - Copy `.env.example` to a new file named `.env`.
   - Fill in your **Bot token**, **Chat ID**, and **Websites**:
     ```env
     BOT_TOKEN=123456789:ABCdefGHIjklMNOpqrSTUvwxYZ
     CHAT_ID=987654321
     WEBSITES=https://example.com,https://another-website.com
     ```
   - Make sure `.env` is listed in your `.gitignore` so it **won’t** be committed.

4. **Build and Run**
   - **Gradle**:
     ```bash
     gradle clean build
     gradle run
     ```
   - **Maven**:
     ```bash
     mvn clean install
     mvn exec:java -Dexec.mainClass="com.yourpackage.WebsiteMonitor"
     ```
   - **IntelliJ IDEA**:
     1. Open the project.
     2. Ensure the working directory is set to the project root (where `.env` is located).
     3. Click **Run**.

---

## Usage

1. **Startup Test**  
   - The app sends a “Test message from Website Monitor” to your Telegram bot on startup.

2. **Website Checks**  
   - Every minute (by default), it checks each website in the `WEBSITES` list.
   - If a site is down, a failure counter increments.

3. **Alert Threshold**  
   - After **5 consecutive failures**, a Telegram alert is sent.
   - If the site recovers, the failure counter resets to 0.

4. **Adjusting the Interval**  
   - You can change how often checks occur by modifying the `scheduleAtFixedRate()` parameters in `WebsiteMonitor.java`.

---

## Repository Layout

- **.env.example** – A sample environment file (no secrets).  
- **.gitignore** – Ensures your `.env` file and other sensitive files are ignored.  
- **src/** – Java source files.  
- **build.gradle / pom.xml** – Build configuration files for Gradle or Maven.

---

## Contributing

Contributions are welcome! Feel free to:
- **Open issues** for bug reports or feature requests.
- **Submit pull requests** to improve documentation or add new features.

---

## License

This project is released under the [MIT License](LICENSE). You are free to use, modify, and distribute this software as permitted by the license.

---

**Happy monitoring!** If you have any questions or run into issues, please open an [issue](../../issues) on GitHub.
