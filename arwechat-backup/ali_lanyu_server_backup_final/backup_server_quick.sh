#!/bin/bash

#==============================================
# 阿里云服务器快速备份脚本 (关键数据优先)
# 服务器：ali_lanyu (47.116.172.28)
# 生成时间：$(date)
# 说明：优先备份配置文件和关键数据，跳过大文件
#==============================================

# 设置备份配置
BACKUP_DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="./backup_ali_lanyu_quick_${BACKUP_DATE}"
SSH_HOST="ali_lanyu"
LOG_FILE="backup_quick_${BACKUP_DATE}.log"
MYSQL_PASSWORD="4305865740bf490f"

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

# 安全执行SSH命令
safe_ssh() {
    local timeout_duration=${1:-30}
    local ssh_command="$2"
    local description="$3"
    
    if timeout "$timeout_duration" ssh "$SSH_HOST" "$ssh_command" 2>/dev/null; then
        return 0
    else
        warn "$description 失败或超时"
        return 1
    fi
}

# 快速备份
quick_backup() {
    log "=========================================="
    log "开始快速备份关键配置和数据"
    log "=========================================="
    
    # 创建目录结构
    mkdir -p "$BACKUP_DIR"/{system,nginx,mysql,java_apps,scripts}
    
    # 1. 系统关键配置
    log "备份系统关键配置文件..."
    local config_files=(
        "/etc/passwd:/etc/group:/etc/shadow:/etc/hosts"
        "/etc/my.cnf:/etc/crontab:/etc/fstab"
        "/etc/ssh/sshd_config"
    )
    
    info "备份系统配置文件"
    ssh "$SSH_HOST" "tar -czf - /etc/passwd /etc/group /etc/shadow /etc/hosts /etc/my.cnf /etc/crontab /etc/fstab /etc/ssh/ /root/.ssh/ 2>/dev/null" > "$BACKUP_DIR/system/system_configs.tar.gz"
    
    # 2. Nginx配置
    log "备份Nginx配置..."
    ssh "$SSH_HOST" "tar -czf - /usr/local/nginx/conf/ /usr/local/nginx/cert/ 2>/dev/null" > "$BACKUP_DIR/nginx/nginx_configs.tar.gz"
    
    # 3. MySQL数据库导出
    log "导出MySQL数据库..."
    info "导出完整数据库 (包含数据)"
    timeout 300 ssh "$SSH_HOST" "mysqldump -u root -p'$MYSQL_PASSWORD' --single-transaction --routines --triggers --all-databases 2>/dev/null" > "$BACKUP_DIR/mysql/all_databases.sql" && log "✓ 数据库完整导出成功" || warn "✗ 数据库导出失败"
    
    # 导出数据库信息
    ssh "$SSH_HOST" "mysql -u root -p'$MYSQL_PASSWORD' -e 'SHOW DATABASES;' 2>/dev/null" > "$BACKUP_DIR/mysql/database_list.txt"
    ssh "$SSH_HOST" "mysql -u root -p'$MYSQL_PASSWORD' -e 'SELECT User, Host FROM mysql.user;' 2>/dev/null" > "$BACKUP_DIR/mysql/mysql_users.txt"
    
    # 4. Java应用核心文件
    log "备份Java应用..."
    ssh "$SSH_HOST" "tar -czf - /home/back/*.jar /home/back/*.sh 2>/dev/null" > "$BACKUP_DIR/java_apps/java_app_core.tar.gz"
    
    # 5. 重要脚本
    log "备份重要脚本..."
    local scripts=(
        "/data/checkpy.sh"
        "/data/ip.sh"
        "/data/auto-del-7-days-ago-log.sh"
        "/data/start.sh"
        "/data/restart.sh"
        "/data/recover.sh"
        "/data/docker-compose.yml"
    )
    
    for script in "${scripts[@]}"; do
        local script_name=$(basename "$script")
        ssh "$SSH_HOST" "test -f $script && cat $script" > "$BACKUP_DIR/scripts/$script_name" 2>/dev/null && log "✓ $script" || warn "✗ $script"
    done
    
    # 6. 收集系统信息
    log "收集系统信息..."
    {
        echo "=== 系统信息 ==="
        ssh "$SSH_HOST" "uname -a"
        echo ""
        echo "=== 磁盘使用 ==="
        ssh "$SSH_HOST" "df -h"
        echo ""
        echo "=== 内存信息 ==="
        ssh "$SSH_HOST" "free -h"
        echo ""
        echo "=== 运行进程 ==="
        ssh "$SSH_HOST" "ps aux | grep -E '(nginx|java|mysql)'"
        echo ""
        echo "=== 网络端口 ==="
        ssh "$SSH_HOST" "netstat -tulpn | grep -E '(80|443|3306|8081)'"
        echo ""
        echo "=== 定时任务 ==="
        ssh "$SSH_HOST" "crontab -l"
    } > "$BACKUP_DIR/system_info.txt"
    
    # 创建备份摘要
    local total_size=$(du -sh "$BACKUP_DIR" | cut -f1)
    {
        echo "=== 快速备份摘要 ==="
        echo "备份时间: $(date)"
        echo "备份大小: $total_size"
        echo ""
        echo "=== 备份内容 ==="
        echo "✓ 系统配置文件 (用户、主机、SSH等)"
        echo "✓ Nginx配置和SSL证书"
        echo "✓ MySQL完整数据库导出"
        echo "✓ Java应用核心文件"
        echo "✓ 重要运维脚本"
        echo "✓ 系统信息报告"
        echo ""
        echo "=== 未包含内容 ==="
        echo "- 大型网站文件 (/home/web/, /root/web/)"
        echo "- JDK安装包"
        echo "- MySQL原始数据文件"
        echo "- 日志文件"
        echo ""
        echo "=== 备份文件清单 ==="
        find "$BACKUP_DIR" -type f -exec ls -lh {} \;
    } > "$BACKUP_DIR/backup_summary.txt"
    
    log "=========================================="
    log "快速备份完成！"
    log "备份位置: $BACKUP_DIR"
    log "备份大小: $total_size"
    log "=========================================="
    
    echo ""
    echo "建议打包命令:"
    echo "tar -czf backup_ali_lanyu_quick_${BACKUP_DATE}.tar.gz $BACKUP_DIR"
}

# 执行快速备份
check_ssh
quick_backup