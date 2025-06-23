package com.ntj.model.taskserver.dto;

public record CronJobDTO(Long id, String jobName, String cronExpression) {

}
