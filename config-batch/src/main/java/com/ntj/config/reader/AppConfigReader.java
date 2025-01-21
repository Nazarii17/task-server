package com.ntj.config.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntj.config.ApplicationProperties;
import com.ntj.domain.record.AppConfigRecord;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AppConfigReader implements ItemReader<AppConfigRecord> {

    private final ListItemReader<AppConfigRecord> delegate;

    public AppConfigReader(ApplicationProperties applicationProperties,
                           RestTemplate restTemplate) {
        this.delegate = new ListItemReader<>(fetchRecords(applicationProperties, restTemplate));
    }

    @Override
    public AppConfigRecord read() {
        return delegate.read();
    }

    private List<AppConfigRecord> fetchRecords(final ApplicationProperties applicationProperties,
                                               final RestTemplate restTemplate) {
        final List<AppConfigRecord> fetchedRecords = new ArrayList<>();
        final ObjectMapper objectMapper = new ObjectMapper();

        for (String url : applicationProperties.getAppConfigs().values()) {
            try {
                final Map<String, Object> responseBody = restTemplate.getForObject(url, Map.class);
                if (responseBody != null) {
                    final String appName = (String) responseBody.get("name");
                    final String resources = objectMapper.writeValueAsString(responseBody);
                    fetchedRecords.add(new AppConfigRecord(appName, resources));
                }
            } catch (Exception e) {
                System.err.println("Failed to fetch data from " + url + ": " + e.getMessage());
            }
        }
        return fetchedRecords;
    }
}

