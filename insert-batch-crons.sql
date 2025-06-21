INSERT INTO task_server_schema.cron_job (job_name, cron_expression) VALUES ('config-batch', '0 0/1 * * * ?');
INSERT INTO task_server_schema.cron_job (job_name, cron_expression) VALUES ('config-batch', '0 0,30 * * * ?');
INSERT INTO task_server_schema.cron_job (job_name, cron_expression) VALUES ('config-batch', '0 5,10,15,20,25,30,35,40,45,50,55 * * * ?');

INSERT INTO task_server_schema.cron_job (job_name, cron_expression) VALUES ('refresh-batch', '0 0/2 * * * ?');
INSERT INTO task_server_schema.cron_job (job_name, cron_expression) VALUES ('refresh-batch', '0 0/2 * * * ?');
