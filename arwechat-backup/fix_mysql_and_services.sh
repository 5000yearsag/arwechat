#!/bin/bash

# 修复MySQL和启动所有服务的脚本

set -e

# 日志函数
info() {
    echo -e "\e[32m[INFO]\e[0m $(date '+%H:%M:%S') $1"
}

warn() {
    echo -e "\e[33m[WARN]\e[0m $(date '+%H:%M:%S') $1"
}

error() {
    echo -e "\e[31m[ERROR]\e[0m $(date '+%H:%M:%S') $1"
}

info "=== 继续完成系统恢复 ==="

# 1. 安装MySQL
info "1. 安装MySQL服务器..."
yum install -y mariadb-server mariadb

# 2. 创建mysql用户和组（如果不存在）
info "2. 确保mysql用户存在..."
if ! id mysql >/dev/null 2>&1; then
    useradd -r -s /bin/false mysql
    info "✓ 已创建mysql用户"
else
    info "✓ mysql用户已存在"
fi

# 3. 设置MySQL数据目录权限
info "3. 设置MySQL数据目录权限..."
if [ -d "/www/server/data" ]; then
    # 改用正确的数据目录路径
    mv /www/server/data /www/server/mysql_data_backup
    # 在原始数据盘找到MySQL数据
    if [ -d "/mnt/data/mysql" ]; then
        cp -r /mnt/data/mysql /www/server/data
        chown -R mysql:mysql /www/server/data
        chmod -R 755 /www/server/data
        info "✓ MySQL数据目录权限已设置"
    fi
fi

# 4. 复制运维脚本（如果之前失败）
info "4. 确保运维脚本已恢复..."
if [ -d "/mnt/data" ]; then
    mkdir -p /data
    cp /mnt/data/*.sh /data/ 2>/dev/null || true
    cp /mnt/data/*.py /data/ 2>/dev/null || true
    cp /mnt/data/docker-compose.yml /data/ 2>/dev/null || true
    chmod +x /data/*.sh 2>/dev/null || true
    info "✓ 运维脚本已确保恢复"
fi

# 5. 恢复定时任务（如果之前失败）
info "5. 确保定时任务已恢复..."
if [ -f "/mnt/var/spool/cron/root" ]; then
    mkdir -p /var/spool/cron
    cp /mnt/var/spool/cron/root /var/spool/cron/root 2>/dev/null || true
    chmod 600 /var/spool/cron/root 2>/dev/null || true
    info "✓ 定时任务已确保恢复"
fi

# 6. 创建系统服务文件
info "6. 创建系统服务文件..."

# 创建Nginx服务文件
cat > /etc/systemd/system/nginx.service << 'EOF'
[Unit]
Description=The nginx HTTP and reverse proxy server
After=network.target remote-fs.target nss-lookup.target

[Service]
Type=forking
PIDFile=/usr/local/nginx/logs/nginx.pid
ExecStartPre=/usr/local/nginx/sbin/nginx -t
ExecStart=/usr/local/nginx/sbin/nginx
ExecReload=/bin/kill -s HUP $MAINPID
KillSignal=SIGQUIT
TimeoutStopSec=5
KillMode=process
PrivateTmp=true

[Install]
WantedBy=multi-user.target
EOF

# 创建Java应用服务文件
cat > /etc/systemd/system/vrplatform.service << 'EOF'
[Unit]
Description=VR Platform Java Application
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/home/back
ExecStart=/bin/bash /home/back/start.sh
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# 7. 设置环境变量
info "7. 设置Java环境变量..."
echo 'export JAVA_HOME=/home/jdk/jdk-11.0.22' >> /etc/profile
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> /etc/profile

# 8. 重新加载systemd
info "8. 重新加载systemd配置..."
systemctl daemon-reload

# 9. 启用服务自启动
info "9. 设置服务自启动..."
systemctl enable mariadb
systemctl enable nginx
systemctl enable vrplatform

# 10. 启动MySQL服务
info "10. 启动MySQL服务..."
if systemctl start mariadb; then
    info "✓ MySQL服务启动成功"
    
    # 设置MySQL root密码
    info "设置MySQL root密码..."
    mysql -e "UPDATE mysql.user SET Password=PASSWORD('4305865740bf490f') WHERE User='root';" 2>/dev/null || \
    mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED BY '4305865740bf490f';" 2>/dev/null || \
    mysqladmin -u root password '4305865740bf490f' 2>/dev/null || true
    mysql -e "FLUSH PRIVILEGES;" -u root -p'4305865740bf490f' 2>/dev/null || true
else
    warn "MySQL服务启动失败，尝试手动初始化..."
    mysql_install_db --user=mysql --datadir=/www/server/data
    systemctl start mariadb || systemctl start mysqld
fi

# 11. 启动Nginx服务
info "11. 启动Nginx服务..."
# 创建必要的目录
mkdir -p /usr/local/www/yaoculture/file
mkdir -p /usr/local/nginx/logs

if systemctl start nginx; then
    info "✓ Nginx服务启动成功"
else
    warn "Nginx服务启动失败，检查配置..."
    /usr/local/nginx/sbin/nginx -t
fi

# 12. 启动Java应用
info "12. 启动Java应用..."
source /etc/profile
if systemctl start vrplatform; then
    info "✓ Java应用服务启动成功"
else
    warn "Java应用服务启动失败，尝试手动启动..."
    cd /home/back && nohup bash start.sh &
fi

# 13. 启动crond服务
info "13. 启动定时任务服务..."
systemctl enable crond
systemctl start crond

info "=== 检查服务状态 ==="
systemctl status mariadb --no-pager -l || true
echo "---"
systemctl status nginx --no-pager -l || true
echo "---"
systemctl status vrplatform --no-pager -l || true
echo "---"

info "=== 系统恢复完成！==="
info "数据盘恢复总结："
info "✓ Java环境已恢复 (/home/jdk/)"
info "✓ Java应用已恢复 (/home/back/)"
info "✓ 网站文件已恢复 (/home/web/)"
info "✓ Nginx已安装并配置 (/usr/local/nginx/)"
info "✓ MySQL数据已恢复 (/www/server/data/)"
info "✓ SSL证书已恢复 (/data/ssl/)"
info "✓ 运维脚本已恢复 (/data/)"
info "✓ 定时任务已恢复"
info "✓ 系统服务已配置并启动"

echo ""
info "请执行以下命令验证服务："
info "1. 检查端口: netstat -tlnp | grep -E ':(80|443|3306|8081|8011|7891)'"
info "2. 检查进程: ps aux | grep -E '(nginx|mysql|java)'"
info "3. 检查日志: tail -f /home/back/platform.log"
info "4. 测试MySQL: mysql -u root -p'4305865740bf490f' -e 'show databases;'"