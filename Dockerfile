# Use the official MySQL image from Docker Hub
FROM mysql:8.0

# Set environment variables
ENV MYSQL_DATABASE=your_database_name \
    MYSQL_USER=your_user \
    MYSQL_PASSWORD=your_password \
    MYSQL_ROOT_PASSWORD=your_root_password

# Expose the default MySQL port
EXPOSE 3306

