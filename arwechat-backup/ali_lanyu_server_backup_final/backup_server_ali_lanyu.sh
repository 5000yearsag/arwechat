#!/bin/bash

#==============================================
# 阿里云服务器全量备份脚本
# 服务器：ali_lanyu (47.116.172.28)
# 生成时间：$(date)
# 说明：该脚本将备份服务器上所有重要的配置、数据和应用
#==============================================

# 设置备份配置
BACKUP_DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="./backup_ali_lanyu_${BACKUP_DATE}"
SSH_HOST="ali_lanyu"
LOG_FILE="backup_${BACKUP_DATE}.log"

# 颜色输出函数
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log() {
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "$LOG_FILE"
}

error() {
    echo -e "${RED}[ERROR $(date '+%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "$LOG_FILE"
}

warn() {
    echo -e "${YELLOW}[WARN $(date '+%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "$LOG_FILE"
}

info() {
    echo -e "${BLUE}[INFO $(date '+%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "$LOG_FILE"
}

# 检查SSH连接
check_ssh() {
    log "检查SSH连接到 $SSH_HOST..."
    if ! timeout 10 ssh "$SSH_HOST" "echo '连接成功'" > /dev/null 2>&1; then
        error "无法连接到服务器 $SSH_HOST，请检查SSH配置"
        exit 1
    fi
    log "SSH连接正常"
}

# 安全执行SSH命令（带超时）
safe_ssh() {
    local timeout_duration=${1:-30}
    local ssh_command="$2"
    local description="$3"
    
    info "执行: $description"
    if timeout "$timeout_duration" ssh "$SSH_HOST" "$ssh_command"; then
        return 0
    else
        local exit_code=$?
        if [ $exit_code -eq 124 ]; then
            warn "$description 操作超时 (${timeout_duration}秒)"
        else
            warn "$description 操作失败 (退出码: $exit_code)"
        fi
        return $exit_code
    fi
}

# 备份文件/目录（带进度和超时控制）
backup_with_progress() {
    local source_path="$1"
    local dest_file="$2"
    local timeout_duration="${3:-300}"  # 默认5分钟超时
    local description="$4"
    
    info "开始备份: $description"
    info "源路径: $source_path"
    info "目标文件: $dest_file"
    
    # 先检查源路径是否存在
    if ! safe_ssh 10 "test -e '$source_path'" "检查路径 $source_path"; then
        warn "路径 $source_path 不存在，跳过备份"
        return 1
    fi
    
    # 获取文件大小估算
    local size_info=$(safe_ssh 15 "du -sh '$source_path' 2>/dev/null | cut -f1" "获取 $source_path 大小")
    if [ $? -eq 0 ] && [ -n "$size_info" ]; then
        info "备份大小: $size_info"
    fi
    
    # 执行备份（带进度）
    info "正在传输文件... (超时: ${timeout_duration}秒)"
    if timeout "$timeout_duration" ssh "$SSH_HOST" "tar -czf - '$source_path' 2>/dev/null" > "$dest_file" 2>/dev/null; then
        local backup_size=$(ls -lh "$dest_file" 2>/dev/null | awk '{print $5}')
        log "✓ $description 备份完成 (大小: $backup_size)"
        return 0
    else
        local exit_code=$?
        if [ $exit_code -eq 124 ]; then
            error "✗ $description 备份超时 (${timeout_duration}秒)"
        else
            error "✗ $description 备份失败"
        fi
        rm -f "$dest_file" 2>/dev/null  # 清理不完整的文件
        return $exit_code
    fi
}

# 创建备份目录结构
create_backup_structure() {
    log "创建备份目录结构..."
    mkdir -p "$BACKUP_DIR"/{system,nginx,mysql,java_apps,data,scripts,logs,ssh,docker}
    log "备份目录创建完成：$BACKUP_DIR"
}

# 备份系统配置文件
backup_system_config() {
    log "开始备份系统配置文件..."
    
    # 重要的系统配置文件
    local config_files=(
        "/etc/passwd"
        "/etc/group" 
        "/etc/shadow"
        "/etc/hosts"
        "/etc/hosts.allow"
        "/etc/hosts.deny"
        "/etc/my.cnf"
        "/etc/crontab"
        "/etc/fstab"
        "/etc/resolv.conf"
        "/etc/sysctl.conf"
        "/etc/security/limits.conf"
    )
    
    for file in "${config_files[@]}"; do
        info "备份 $file"
        ssh "$SSH_HOST" "test -f $file && cat $file" > "$BACKUP_DIR/system/$(basename $file)" 2>/dev/null || warn "$file 不存在或无法访问"
    done
    
    # 备份crontab
    info "备份用户crontab"
    if safe_ssh 15 "crontab -l 2>/dev/null" "读取用户crontab" > "$BACKUP_DIR/system/root_crontab.txt"; then
        log "✓ 用户crontab备份完成"
    else
        warn "✗ 无用户crontab或备份失败"
    fi
    
    # 备份/etc/cron.d目录
    backup_with_progress "/etc/cron.d/" "$BACKUP_DIR/system/cron_d.tar.gz" 60 "/etc/cron.d 目录"
    
    # 备份systemd服务
    info "备份systemd服务配置"
    if timeout 120 ssh "$SSH_HOST" "tar -czf - /etc/systemd/system/ /usr/lib/systemd/system/ 2>/dev/null" > "$BACKUP_DIR/system/systemd_services.tar.gz" 2>/dev/null; then
        log "✓ systemd服务配置备份完成"
    else
        warn "✗ systemd服务配置备份失败"
    fi
    
    log "系统配置文件备份完成"
}

# 备份Nginx配置和网站文件  
backup_nginx() {
    log "开始备份Nginx配置和网站文件..."
    
    # 备份Nginx配置（小文件，短超时）
    backup_with_progress "/usr/local/nginx/conf/" "$BACKUP_DIR/nginx/nginx_config.tar.gz" 60 "Nginx配置文件"
    
    # 备份SSL证书（小文件）
    backup_with_progress "/usr/local/nginx/cert/" "$BACKUP_DIR/nginx/ssl_certs.tar.gz" 30 "SSL证书"
    
    # 备份网站文件（可能较大，增加超时时间）
    backup_with_progress "/home/web/" "$BACKUP_DIR/nginx/home_web.tar.gz" 600 "网站文件 /home/web/"
    
    backup_with_progress "/root/web/" "$BACKUP_DIR/nginx/root_web.tar.gz" 300 "网站文件 /root/web/"
    
    backup_with_progress "/usr/local/www/yaoculture/" "$BACKUP_DIR/nginx/yaoculture.tar.gz" 300 "网站文件 /usr/local/www/yaoculture/"
    
    # 备份Nginx日志（最近7天，使用特殊处理）
    info "备份Nginx日志（最近7天）"
    if safe_ssh 60 "find /usr/local/nginx/logs/ -mtime -7 -name '*.log' -exec tar -czf - {} + 2>/dev/null" "查找并打包Nginx日志" > "$BACKUP_DIR/nginx/nginx_logs.tar.gz"; then
        log "✓ Nginx日志备份完成"
    else
        warn "✗ Nginx日志备份失败"
    fi
    
    log "Nginx配置和网站文件备份完成"
}

# 备份MySQL数据库
backup_mysql() {
    log "开始备份MySQL数据库..."
    
    # 备份MySQL配置（小文件）
    info "备份MySQL配置文件"
    if safe_ssh 15 "cat /etc/my.cnf 2>/dev/null" "读取MySQL配置" > "$BACKUP_DIR/mysql/my.cnf"; then
        log "✓ MySQL配置文件备份完成"
    else
        warn "✗ MySQL配置文件备份失败"
    fi
    
    # 备份数据目录（排除大型日志文件，这个可能很大）
    info "备份MySQL数据目录 (排除日志文件)"
    info "注意：此操作可能需要较长时间，请耐心等待..."
    if timeout 1200 ssh "$SSH_HOST" "tar --exclude='*.log' --exclude='ib_logfile*' --exclude='mysql-bin.*' -czf - /www/server/data/ 2>/dev/null" > "$BACKUP_DIR/mysql/mysql_data.tar.gz" 2>/dev/null; then
        local backup_size=$(ls -lh "$BACKUP_DIR/mysql/mysql_data.tar.gz" 2>/dev/null | awk '{print $5}')
        log "✓ MySQL数据目录备份完成 (大小: $backup_size)"
    else
        error "✗ MySQL数据目录备份失败或超时 (20分钟)"
        rm -f "$BACKUP_DIR/mysql/mysql_data.tar.gz" 2>/dev/null
    fi
    
    # 导出数据库（使用提供的密码）
    local mysql_password="4305865740bf490f"
    info "导出数据库结构和数据 (使用root密码)"
    info "注意：数据库导出可能需要几分钟时间..."
    if timeout 600 ssh "$SSH_HOST" "mysqldump -u root -p'$mysql_password' --single-transaction --routines --triggers --all-databases 2>/dev/null" > "$BACKUP_DIR/mysql/all_databases.sql" 2>/dev/null; then
        local backup_size=$(ls -lh "$BACKUP_DIR/mysql/all_databases.sql" 2>/dev/null | awk '{print $5}')
        log "✓ 完整数据库导出完成 (大小: $backup_size)"
    else
        warn "✗ 完整数据库导出失败，尝试仅导出结构"
        # 尝试只导出结构
        if timeout 180 ssh "$SSH_HOST" "mysqldump -u root -p'$mysql_password' --no-data --routines --triggers --all-databases 2>/dev/null" > "$BACKUP_DIR/mysql/database_structure.sql" 2>/dev/null; then
            local structure_size=$(ls -lh "$BACKUP_DIR/mysql/database_structure.sql" 2>/dev/null | awk '{print $5}')
            log "✓ 数据库结构导出完成 (大小: $structure_size)"
        else
            error "✗ 数据库结构导出也失败，可能密码错误"
        fi
    fi
    
    # 导出数据库列表和用户信息
    info "导出数据库基本信息"
    if safe_ssh 30 "mysql -u root -p'$mysql_password' -e 'SHOW DATABASES;' 2>/dev/null" "获取数据库列表" > "$BACKUP_DIR/mysql/database_list.txt"; then
        log "✓ 数据库列表导出完成"
    fi
    
    if safe_ssh 30 "mysql -u root -p'$mysql_password' -e 'SELECT User, Host FROM mysql.user;' 2>/dev/null" "获取用户列表" > "$BACKUP_DIR/mysql/mysql_users.txt"; then
        log "✓ MySQL用户列表导出完成"
    fi
    
    # 备份MySQL错误日志
    info "备份MySQL错误日志 (最后10000行)"
    if safe_ssh 60 "tail -n 10000 /www/server/data/iZuf6fgf6jvctohpgejvvnZ.err 2>/dev/null" "读取MySQL错误日志" > "$BACKUP_DIR/mysql/mysql_error.log"; then
        log "✓ MySQL错误日志备份完成"
    else
        warn "✗ MySQL错误日志备份失败"
    fi
    
    log "MySQL数据库备份完成"
}

# 备份Java应用
backup_java_apps() {
    log "开始备份Java应用..."
    
    # 备份Java应用目录（包含jar文件，可能较大）
    backup_with_progress "/home/back/" "$BACKUP_DIR/java_apps/home_back.tar.gz" 600 "Java应用目录 /home/back/"
    
    # 备份JDK（排除压缩包，但仍然很大）
    info "备份JDK /home/jdk/ (排除压缩包)"
    info "注意：JDK备份可能需要几分钟时间..."
    if timeout 900 ssh "$SSH_HOST" "tar --exclude='*.zip' --exclude='*.tar.gz' --exclude='*.deb' --exclude='*.rpm' -czf - /home/jdk/ 2>/dev/null" > "$BACKUP_DIR/java_apps/jdk.tar.gz" 2>/dev/null; then
        local backup_size=$(ls -lh "$BACKUP_DIR/java_apps/jdk.tar.gz" 2>/dev/null | awk '{print $5}')
        log "✓ JDK备份完成 (大小: $backup_size)"
    else
        warn "✗ JDK备份失败或超时 (15分钟)"
        rm -f "$BACKUP_DIR/java_apps/jdk.tar.gz" 2>/dev/null
    fi
    
    # 备份应用日志（最近部分）
    info "备份Java应用日志 (最后50000行)"
    if safe_ssh 60 "tail -n 50000 /home/back/platform.log 2>/dev/null" "读取Java应用日志" > "$BACKUP_DIR/java_apps/platform.log"; then
        log "✓ Java应用日志备份完成"
    else
        warn "✗ Java应用日志备份失败"
    fi
    
    log "Java应用备份完成"
}

# 备份数据目录
backup_data_directory() {
    log "开始备份数据目录..."
    
    # 备份/data目录（排除大型日志文件和MySQL目录）
    info "备份 /data 目录 (排除日志和mysql目录)"
    if timeout 600 ssh "$SSH_HOST" "tar --exclude='*.log' --exclude='logs/' --exclude='mysql/' --exclude='check.log' --exclude='update.log' -czf - /data/ 2>/dev/null" > "$BACKUP_DIR/data/data_directory.tar.gz" 2>/dev/null; then
        local backup_size=$(ls -lh "$BACKUP_DIR/data/data_directory.tar.gz" 2>/dev/null | awk '{print $5}')
        log "✓ /data目录备份完成 (大小: $backup_size)"
    else
        warn "✗ /data目录备份失败或超时 (10分钟)"
        rm -f "$BACKUP_DIR/data/data_directory.tar.gz" 2>/dev/null
    fi
    
    # 单独备份重要脚本（确保不丢失）
    info "单独备份重要脚本"
    local scripts=(
        "/data/checkpy.sh"
        "/data/ip.sh" 
        "/data/auto-del-7-days-ago-log.sh"
        "/data/start.sh"
        "/data/restart.sh"
        "/data/recover.sh"
        "/data/startcms.sh"
        "/data/startpy.sh"
        "/data/update.sh"
        "/data/docker-compose.yml"
    )
    
    for script in "${scripts[@]}"; do
        local script_name=$(basename "$script")
        if safe_ssh 10 "test -f $script && cat $script" "读取脚本 $script" > "$BACKUP_DIR/scripts/$script_name"; then
            log "✓ $script 备份完成"
        else
            warn "✗ $script 不存在或备份失败"
        fi
    done
    
    log "数据目录备份完成"
}

# 备份SSH配置
backup_ssh_config() {
    log "开始备份SSH配置..."
    
    # 备份SSH配置文件
    info "备份SSH配置文件"
    ssh "$SSH_HOST" "tar -czf - /etc/ssh/ 2>/dev/null" > "$BACKUP_DIR/ssh/ssh_config.tar.gz" || warn "SSH配置备份失败"
    
    # 备份SSH密钥
    info "备份SSH密钥"
    ssh "$SSH_HOST" "tar -czf - /root/.ssh/ 2>/dev/null" > "$BACKUP_DIR/ssh/ssh_keys.tar.gz" || warn "SSH密钥备份失败"
    
    log "SSH配置备份完成"
}

# 备份Docker配置
backup_docker() {
    log "开始备份Docker配置..."
    
    # 备份docker-compose文件
    info "备份docker-compose配置"
    ssh "$SSH_HOST" "test -f /data/docker-compose.yml && cat /data/docker-compose.yml" > "$BACKUP_DIR/docker/docker-compose.yml" 2>/dev/null || warn "docker-compose.yml不存在"
    
    # 备份Docker配置
    info "备份Docker daemon配置"
    ssh "$SSH_HOST" "tar -czf - /etc/docker/ 2>/dev/null" > "$BACKUP_DIR/docker/docker_config.tar.gz" || warn "Docker配置备份失败"
    
    log "Docker配置备份完成"
}

# 收集系统信息
collect_system_info() {
    log "收集系统信息..."
    
    local info_file="$BACKUP_DIR/system_info.txt"
    
    {
        echo "=== 服务器信息收集报告 ==="
        echo "备份时间: $(date)"
        echo "服务器: $SSH_HOST"
        echo ""
        
        echo "=== 系统信息 ==="
        ssh "$SSH_HOST" "uname -a"
        echo ""
        
        echo "=== 操作系统版本 ==="
        ssh "$SSH_HOST" "cat /etc/os-release"
        echo ""
        
        echo "=== 磁盘使用情况 ==="
        ssh "$SSH_HOST" "df -h"
        echo ""
        
        echo "=== 内存信息 ==="
        ssh "$SSH_HOST" "free -h"
        echo ""
        
        echo "=== 网络配置 ==="
        ssh "$SSH_HOST" "ip addr show"
        echo ""
        
        echo "=== 运行中的服务 ==="
        ssh "$SSH_HOST" "systemctl list-units --type=service --state=running"
        echo ""
        
        echo "=== 运行中的进程 ==="
        ssh "$SSH_HOST" "ps aux"
        echo ""
        
        echo "=== 网络连接 ==="
        ssh "$SSH_HOST" "netstat -tulpn"
        echo ""
        
        echo "=== 定时任务 ==="
        ssh "$SSH_HOST" "crontab -l 2>/dev/null || echo 'No crontab'"
        echo ""
        
        echo "=== Docker容器 ==="
        ssh "$SSH_HOST" "docker ps -a 2>/dev/null || echo 'Docker不可用'"
        echo ""
        
        echo "=== 包管理信息 ==="
        ssh "$SSH_HOST" "rpm -qa | sort" 2>/dev/null || echo "RPM包列表获取失败"
        
    } > "$info_file"
    
    log "系统信息收集完成"
}

# 创建备份摘要
create_backup_summary() {
    log "创建备份摘要..."
    
    local summary_file="$BACKUP_DIR/backup_summary.txt"
    local total_size=$(du -sh "$BACKUP_DIR" | cut -f1)
    
    {
        echo "=== 阿里云服务器备份摘要 ==="
        echo "服务器: $SSH_HOST (47.116.172.28)"
        echo "备份时间: $(date)"
        echo "备份目录: $BACKUP_DIR"
        echo "总大小: $total_size"
        echo ""
        
        echo "=== 备份内容清单 ==="
        echo "1. 系统配置文件 (/etc/passwd, /etc/hosts, /etc/my.cnf 等)"
        echo "2. Nginx配置和网站文件 (/usr/local/nginx/, /home/web/, /root/web/)"
        echo "3. MySQL数据库 (/www/server/data/)"
        echo "4. Java应用 (/home/back/vr-platform.jar, JDK等)"
        echo "5. 重要脚本和数据 (/data/目录)"
        echo "6. SSH配置和密钥 (/etc/ssh/, /root/.ssh/)"
        echo "7. Docker配置文件"
        echo "8. 系统信息报告"
        echo ""
        
        echo "=== 备份文件详情 ==="
        find "$BACKUP_DIR" -type f -exec ls -lh {} \; | sort -k5 -hr
        echo ""
        
        echo "=== 重要提醒 ==="
        echo "1. 请妥善保管备份文件，包含敏感信息"
        echo "2. MySQL数据库可能需要单独的密码导出"
        echo "3. 建议定期验证备份文件的完整性"
        echo "4. 迁移时请注意服务器环境差异"
        
    } > "$summary_file"
    
    log "备份摘要创建完成"
}

# 主执行函数
main() {
    log "=========================================="
    log "开始阿里云服务器 $SSH_HOST 全量备份"
    log "=========================================="
    
    # 执行备份步骤
    check_ssh
    create_backup_structure
    collect_system_info
    backup_system_config
    backup_nginx
    backup_mysql
    backup_java_apps
    backup_data_directory
    backup_ssh_config
    backup_docker
    create_backup_summary
    
    log "=========================================="
    log "备份完成！"
    log "备份位置: $BACKUP_DIR"
    log "备份大小: $(du -sh "$BACKUP_DIR" | cut -f1)"
    log "日志文件: $LOG_FILE"
    log "=========================================="
    
    # 建议打包
    info "建议将备份打包："
    echo "tar -czf backup_ali_lanyu_${BACKUP_DATE}.tar.gz $BACKUP_DIR"
}

# 执行主函数
main "$@"