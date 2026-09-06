package com.chrome_history_dashboard.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Bind via application.yml:
 *
 * chrome:
 *   history:
 *     path: "C:/Users/<you>/AppData/Local/Google/Chrome/User Data/Default/History"
 *
 * If left blank, ChromeHistoryReaderService auto-detects the default path for the
 * OS Spring Boot is running on.
 */


@Component
@ConfigurationProperties(prefix = "chrome.history")
@Data
public class ChromeHistoryProperties {
	

    private String path;

}
