package com.beta.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
@Profile("!test")
public class GcsConfig {

    @Value("${spring.cloud.gcp.storage.credentials.location}")
    private String credentialsPath;

    @Value("${spring.cloud.gcp.storage.project-id}")
    private String projectId;

    @Bean
    public Storage storage() throws IOException {
        StorageOptions.Builder builder = StorageOptions.newBuilder().setProjectId(projectId);
        if (credentialsPath != null && credentialsPath.startsWith("file:")) { // 로컬 개발 환경에서 실행 시
            String cleanPath = credentialsPath.replace("file:", "");
            builder.setCredentials(GoogleCredentials.fromStream(new FileInputStream(cleanPath)));
        } else {  // VM 내부 실행 시
            builder.setCredentials(GoogleCredentials.getApplicationDefault());
        }
        return builder.build().getService();
    }
}
