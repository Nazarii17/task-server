package com.ntj.config.writer;

import com.ntj.domain.entity.AppConfigurationSnapshot;
import com.ntj.repository.AppConfigurationSnapshotRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class AppConfigSnapshotWriter implements ItemWriter<AppConfigurationSnapshot> {

    private final AppConfigurationSnapshotRepository repository;

    public AppConfigSnapshotWriter(AppConfigurationSnapshotRepository repository) {
        this.repository = repository;
    }

    @Override
    public void write(Chunk<? extends AppConfigurationSnapshot> chunk) {
        repository.saveAll(chunk);
        System.out.println("Successfully written to the database: " + chunk.size() + " items.");
    }
}

