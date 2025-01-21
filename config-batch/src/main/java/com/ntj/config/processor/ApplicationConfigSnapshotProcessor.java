package com.ntj.config.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntj.domain.entity.AppConfigurationSnapshot;
import com.ntj.domain.record.AppConfigRecord;
import com.ntj.service.TaskService;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class ApplicationConfigSnapshotProcessor implements ItemProcessor<AppConfigRecord, AppConfigurationSnapshot> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TaskService taskService;

    public ApplicationConfigSnapshotProcessor(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public AppConfigurationSnapshot process(final AppConfigRecord record) {
        try {
            final JsonNode root = objectMapper.readTree(record.resources());
            final JsonNode propertySourcesNode = root.get("propertySources");

            final String propertySources = propertySourcesNode != null ? propertySourcesNode.toString() : null;

            final Long taskExecutionId = taskService.getCurrentTaskExecutionId();

            return new AppConfigurationSnapshot(
                    UUID.randomUUID(),
                    record.appName(),
                    record.resources(),
                    propertySources,
                    LocalDateTime.now(),
                    taskExecutionId
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to process record: " + record, e);
        }
    }
}


