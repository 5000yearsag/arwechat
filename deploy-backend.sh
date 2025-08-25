#!/bin/bash

# ARWeb 后端自动化部署脚本
# 使用方法: ./deploy-backend.sh [版本标识]
# 示例: ./deploy-backend.sh v1.3-loadtype-support

set -e  # 遇到错误立即退出

# 配置变量
SERVER_ALIAS="ali_lanyu"
BACKEND_DIR="/home/back"
JAVA_PATH="/home/jdk/jdk-11.0.22/bin/java"
JAR_NAME="vr-platform.jar"
LOG_FILE="platform.log"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 版本标识 (可选参数)
VERSION_TAG="${1:-$(date +%Y%m%d_%H%M%S)}"
BACKUP_JAR="${JAR_NAME%.jar}-backup-${VERSION_TAG}.jar"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}ARWeb 后端自动化部署脚本${NC}"
echo -e "${BLUE}版本标识: ${VERSION_TAG}${NC}"
echo -e "${BLUE}========================================${NC}"

# 步骤1: 检查本地环境
echo -e "\n${YELLOW}[1/8] 检查本地环境...${NC}"
if [ ! -d "ar-platform" ]; then
    echo -e "${RED}错误: ar-platform目录不存在，请在项目根目录运行此脚本${NC}"
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    echo -e "${RED}错误: Maven未安装，请先安装Maven${NC}"
    exit 1
fi

if ! ssh -o ConnectTimeout=10 -q "${SERVER_ALIAS}" exit; then
    echo -e "${RED}错误: 无法连接到服务器 ${SERVER_ALIAS}${NC}"
    exit 1
fi

echo -e "${GREEN}✓ 本地环境检查通过${NC}"

# 步骤2: 构建后端项目
echo -e "\n${YELLOW}[2/8] 构建后端项目...${NC}"
cd ar-platform
echo "运行 Maven 构建..."
mvn clean package -DskipTests -q
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ 后端项目构建成功${NC}"
else
    echo -e "${RED}✗ 后端项目构建失败${NC}"
    exit 1
fi
cd ..

# 步骤3: 检查构建产物
echo -e "\n${YELLOW}[3/8] 检查构建产物...${NC}"
if [ ! -f "ar-platform/target/${JAR_NAME}" ]; then
    echo -e "${RED}错误: JAR文件不存在 ar-platform/target/${JAR_NAME}${NC}"
    exit 1
fi

JAR_SIZE=$(ls -lh "ar-platform/target/${JAR_NAME}" | awk '{print $5}')
echo -e "${GREEN}✓ JAR文件大小: ${JAR_SIZE}${NC}"

# 步骤4: 备份生产环境JAR文件
echo -e "\n${YELLOW}[4/8] 备份生产环境JAR文件...${NC}"
ssh "${SERVER_ALIAS}" "cd ${BACKEND_DIR} && \
    if [ -f ${JAR_NAME} ]; then \
        cp ${JAR_NAME} ${BACKUP_JAR} && \
        echo '✓ 备份文件已创建: ${BACKUP_JAR}'; \
    else \
        echo '⚠ 生产环境JAR文件不存在，跳过备份'; \
    fi"

# 步骤5: 上传新JAR文件
echo -e "\n${YELLOW}[5/8] 上传新JAR文件...${NC}"
scp "ar-platform/target/${JAR_NAME}" "${SERVER_ALIAS}:${BACKEND_DIR}/${JAR_NAME}-new"
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ JAR文件上传成功${NC}"
else
    echo -e "${RED}✗ JAR文件上传失败${NC}"
    exit 1
fi

# 步骤6: 停止现有服务
echo -e "\n${YELLOW}[6/8] 停止现有后端服务...${NC}"
ssh "${SERVER_ALIAS}" "cd ${BACKEND_DIR} && \
    if pgrep -f '${JAR_NAME}' > /dev/null; then \
        echo '正在停止现有服务...'; \
        pkill -f '${JAR_NAME}'; \
        sleep 3; \
        if pgrep -f '${JAR_NAME}' > /dev/null; then \
            echo '强制终止服务...'; \
            pkill -9 -f '${JAR_NAME}'; \
            sleep 2; \
        fi; \
        echo '✓ 服务已停止'; \
    else \
        echo '⚠ 没有运行中的服务'; \
    fi"

# 步骤7: 替换JAR文件并启动服务
echo -e "\n${YELLOW}[7/8] 替换JAR文件并启动服务...${NC}"
ssh "${SERVER_ALIAS}" "cd ${BACKEND_DIR} && \
    mv ${JAR_NAME}-new ${JAR_NAME} && \
    echo '✓ JAR文件已替换' && \
    echo '正在启动新服务...' && \
    nohup ${JAVA_PATH} -jar ${JAR_NAME} \
        -Xmx1500M -Xms1024M \
        --spring.profiles.active=prod \
        --server.port=8081 > ${LOG_FILE} 2>&1 &
    NEW_PID=\$! && \
    echo \"✓ 服务已启动，PID: \${NEW_PID}\""

# 步骤8: 验证部署结果
echo -e "\n${YELLOW}[8/8] 验证部署结果...${NC}"
echo "等待服务启动完成..."
sleep 15

# 检查进程状态
PROCESS_CHECK=$(ssh "${SERVER_ALIAS}" "pgrep -f '${JAR_NAME}' | wc -l")
if [ "${PROCESS_CHECK}" -gt 0 ]; then
    echo -e "${GREEN}✓ 后端进程运行正常${NC}"
    
    # 获取进程信息
    ssh "${SERVER_ALIAS}" "ps aux | grep '${JAR_NAME}' | grep -v grep | head -1" | \
    awk '{printf "  PID: %s, CPU: %s%%, MEM: %s%%\n", $2, $3, $4}'
else
    echo -e "${RED}✗ 后端进程未运行${NC}"
    echo "检查启动日志:"
    ssh "${SERVER_ALIAS}" "cd ${BACKEND_DIR} && tail -20 ${LOG_FILE}"
    exit 1
fi

# 检查端口监听
PORT_CHECK=$(ssh "${SERVER_ALIAS}" "ss -tlnp | grep ':8081' | wc -l")
if [ "${PORT_CHECK}" -gt 0 ]; then
    echo -e "${GREEN}✓ 端口8081监听正常${NC}"
else
    echo -e "${RED}✗ 端口8081未监听${NC}"
fi

# 检查API响应
echo "测试API连接..."
API_TEST=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 10 \
    "http://app.lanyuxr.com:8081/api/guest/statistic?collectionUuid=test&type=pvCount" 2>/dev/null)

if [ "${API_TEST}" = "200" ]; then
    echo -e "${GREEN}✓ API响应正常 (HTTP ${API_TEST})${NC}"
else
    echo -e "${YELLOW}⚠ API响应异常 (HTTP ${API_TEST})，服务可能仍在启动中${NC}"
fi

# 显示日志摘要
echo -e "\n${YELLOW}最新日志摘要:${NC}"
ssh "${SERVER_ALIAS}" "cd ${BACKEND_DIR} && tail -5 ${LOG_FILE}" | sed 's/^/  /'

# 部署完成总结
echo -e "\n${BLUE}========================================${NC}"
echo -e "${GREEN}🎉 后端部署完成!${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "📦 版本标识: ${GREEN}${VERSION_TAG}${NC}"
echo -e "🔄 备份文件: ${GREEN}${BACKUP_JAR}${NC}"
echo -e "🔗 API地址: ${GREEN}http://app.lanyuxr.com:8081${NC}"
echo -e "📋 管理面板: ${GREEN}https://app.lanyuxr.com${NC}"

echo -e "\n${YELLOW}管理命令:${NC}"
echo -e "  查看日志: ${BLUE}ssh ${SERVER_ALIAS} \"tail -f ${BACKEND_DIR}/${LOG_FILE}\"${NC}"
echo -e "  检查状态: ${BLUE}ssh ${SERVER_ALIAS} \"ps aux | grep ${JAR_NAME}\"${NC}"
echo -e "  重启服务: ${BLUE}ssh ${SERVER_ALIAS} \"cd ${BACKEND_DIR} && pkill -f ${JAR_NAME} && nohup ${JAVA_PATH} -jar ${JAR_NAME} --spring.profiles.active=prod --server.port=8081 > ${LOG_FILE} 2>&1 &\"${NC}"

echo -e "\n${GREEN}部署完成时间: $(date '+%Y-%m-%d %H:%M:%S')${NC}"