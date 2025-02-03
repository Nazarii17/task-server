package com.ntj.util;

import com.ntj.constant.Status;
import com.ntj.domain.entity.AppConfigurationSnapshot;
import org.springframework.batch.item.ExecutionContext;

import java.util.Collection;
import java.util.Objects;

import static com.ntj.constant.Status.CONFIGS_CHANGED;
import static com.ntj.constant.Status.CONFIGS_NOT_CHANGED;

public final class JobUtil {

    private JobUtil() {}

    public static Status getStatus(final String currentPropertySources,
                             final String lastPropertySources,
                             final AppConfigurationSnapshot lastAppConfigurationSnapshot) {
        final Status status;
        if (Objects.nonNull(currentPropertySources) && Objects.nonNull(lastPropertySources)) {
            // Both are non-null, so compare them
            if (currentPropertySources.equals(lastPropertySources)) {
                status = CONFIGS_NOT_CHANGED;
            } else {
                status = CONFIGS_CHANGED;
            }
        } else if (Objects.isNull(lastAppConfigurationSnapshot) || Objects.isNull(lastPropertySources)) {
            // Either lastAppConfigurationSnapshot or lastPropertySources is null
            status = CONFIGS_NOT_CHANGED;
        } else {
            // Fallback: If neither condition was met, assume no change
            status = CONFIGS_NOT_CHANGED;
        }
        return status;
    }

    public static void putStatusToExecutionContext(final Collection<String> statuses,
                                             final ExecutionContext executionContext) {
        if (statuses.contains(CONFIGS_CHANGED.getValue())) {
            executionContext
                    .put("isAppsChanged", true);
            executionContext.put("simpleStepStatus", CONFIGS_CHANGED.getValue());
        } else {
            executionContext
                    .put("isAppsChanged", false);
            executionContext.put("simpleStepStatus", CONFIGS_NOT_CHANGED.getValue());
        }
    }
}
