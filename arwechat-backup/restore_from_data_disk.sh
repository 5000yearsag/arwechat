#!/bin/bash

# 阿里云服务器数据盘恢复脚本
# 从数据盘 /mnt 恢复所有服务和配置到新系统

set -e

# 日志函数
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

info() {
    echo -e "\e[32m[INFO]\e[0m $(date '+%H:%M:%S') $1"
}

warn() {
    echo -e "\e[33m[WARN]\e[0m $(date '+%H:%M:%S') $1"
}

error() {
    echo -e "\e[31m[ERROR]\e[0m $(date '+%H:%M:%S') $1"
}

# 检查是否以root身份运行
if [ "$EUID" -ne 0 ]; then
    error "请以root身份运行此脚本"
    exit 1
fi

# 检查数据盘是否已挂载
if [ ! -d "/mnt/home" ] || [ ! -d "/mnt/data" ]; then
    error "数据盘未正确挂载到 /mnt，请先挂载数据盘"
    exit 1
fi

info "=== 开始从数据盘恢复系统 ==="

# 1. 安装必要的软件包
info "1. 安装必要的软件包..."
yum update -y
yum install -y wget gcc gcc-c++ make pcre-devel zlib-devel openssl-devel
yum install -y mysql mysql-server mysql-devel

# 2. 恢复Java环境
info "2. 恢复Java环境..."
if [ -d "/mnt/home/jdk" ]; then
    mkdir -p /home
    cp -r /mnt/home/jdk /home/
    info "✓ JDK已恢复到 /home/jdk/"
    
    # 设置Java环境变量
    echo 'export JAVA_HOME=/home/jdk/jdk-11.0.22' >> /etc/profile
    echo 'export PATH=$JAVA_HOME/bin:$PATH' >> /etc/profile
    source /etc/profile
    info "✓ Java环境变量已设置"
else
    warn "未找到JDK目录"
fi

# 3. 恢复Java应用
info "3. 恢复Java应用..."
if [ -d "/mnt/home/back" ]; then
    mkdir -p /home
    cp -r /mnt/home/back /home/
    chmod +x /home/back/start.sh
    info "✓ Java应用已恢复到 /home/back/"
else
    warn "未找到Java应用目录"
fi

# 4. 恢复网站文件
info "4. 恢复网站文件..."
if [ -d "/mnt/home/web" ]; then
    mkdir -p /home
    cp -r /mnt/home/web /home/
    info "✓ 网站文件已恢复到 /home/web/"
fi

if [ -d "/mnt/www" ]; then
    cp -r /mnt/www /
    info "✓ WWW目录已恢复到 /www/"
fi

# 5. 安装和配置Nginx
info "5. 安装和配置Nginx..."
# 编译安装Nginx (保持与原系统一致)
cd /tmp
wget http://nginx.org/download/nginx-1.20.2.tar.gz
tar -zxf nginx-1.20.2.tar.gz
cd nginx-1.20.2
./configure --prefix=/usr/local/nginx \
    --with-http_ssl_module \
    --with-http_realip_module \
    --with-http_addition_module \
    --with-http_sub_module \
    --with-http_dav_module \
    --with-http_flv_module \
    --with-http_mp4_module \
    --with-http_gunzip_module \
    --with-http_gzip_static_module \
    --with-http_random_index_module \
    --with-http_secure_link_module \
    --with-http_stub_status_module \
    --with-http_auth_request_module \
    --with-threads \
    --with-stream \
    --with-stream_ssl_module \
    --with-http_slice_module
make && make install
cd /

# 恢复Nginx配置
if [ -d "/mnt/usr/local/nginx/conf" ]; then
    cp -r /mnt/usr/local/nginx/conf/* /usr/local/nginx/conf/
    info "✓ Nginx配置已恢复"
fi

# 创建Nginx日志目录
mkdir -p /usr/local/nginx/logs
mkdir -p /usr/local/www/yaoculture/file

# 6. 恢复SSL证书
info "6. 恢复SSL证书..."
if [ -d "/mnt/data/ssl" ]; then
    mkdir -p /data
    cp -r /mnt/data/ssl /data/
    info "✓ SSL证书已恢复到 /data/ssl/"
fi

# 7. 恢复MySQL配置和数据
info "7. 恢复MySQL配置和数据..."
# 停止MySQL服务
systemctl stop mysqld 2>/dev/null || true

# 恢复MySQL配置
if [ -f "/mnt/etc/my.cnf" ]; then
    cp /mnt/etc/my.cnf /etc/
    info "✓ MySQL配置已恢复"
fi

# 创建数据目录
mkdir -p /www/server
if [ -d "/mnt/data/mysql" ]; then
    cp -r /mnt/data/mysql /www/server/data
    chown -R mysql:mysql /www/server/data
    info "✓ MySQL数据已恢复到 /www/server/data/"
fi

# 8. 恢复运维脚本
info "8. 恢复运维脚本..."
if [ -d "/mnt/data" ]; then
    mkdir -p /data
    # 复制脚本文件
    cp /mnt/data/*.sh /data/ 2>/dev/null || true
    cp /mnt/data/*.py /data/ 2>/dev/null || true
    cp /mnt/data/docker-compose.yml /data/ 2>/dev/null || true
    chmod +x /data/*.sh 2>/dev/null || true
    info "✓ 运维脚本已恢复到 /data/"
fi

# 9. 恢复定时任务
info "9. 恢复定时任务..."
if [ -f "/mnt/var/spool/cron/root" ]; then
    cp /mnt/var/spool/cron/root /var/spool/cron/root
    chmod 600 /var/spool/cron/root
    info "✓ Root用户定时任务已恢复"
fi

# 10. 创建系统服务文件
info "10. 创建系统服务文件..."

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
Type=forking
User=root
WorkingDirectory=/home/back
ExecStart=/home/back/start.sh
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# 重新加载systemd
systemctl daemon-reload

info "11. 设置服务自启动..."
systemctl enable mysqld
systemctl enable nginx
systemctl enable vrplatform

info "=== 数据恢复完成！==="
info "接下来请手动执行以下步骤："
info "1. 启动MySQL: systemctl start mysqld"
info "2. 启动Nginx: systemctl start nginx"
info "3. 启动Java应用: systemctl start vrplatform"
info "4. 检查服务状态: systemctl status mysqld nginx vrplatform"

log "恢复脚本执行完成"