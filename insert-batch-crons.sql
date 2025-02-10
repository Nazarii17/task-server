INSERT INTO task_server_schema.cron_job (job_name, cron_expression) VALUES ('config-batch', '0 0/1 * * * ?');

INSERT INTO task_server_schema.cron_job (job_name, cron_expression) VALUES ('refresh-batch', '0 0/1 * * * ?');
