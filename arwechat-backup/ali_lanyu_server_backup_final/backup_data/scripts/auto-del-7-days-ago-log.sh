
find /data/logs -mtime +7 -name "*.log" -exec rm -rf {} \;

find /data/logs/nginx/ -size +10M -name "*.log" -exec truncate -s 0 {} \;

find /var/lib/docker/containers/ -size +10M -name "*-json.log" -exec truncate -s 0 {} \;
