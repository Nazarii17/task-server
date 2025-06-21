package com.ntj.config;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class DependencyTreeInfoContributor implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        try {
            String tree = Files.readString(Paths.get("target/classes/META-INF/dependency-tree.txt"));
            builder.withDetail("dependencyTree", tree);
        } catch (IOException e) {
            builder.withDetail("dependencyTree", "Dependency tree file not found.");
        }
    }
}
