#!/bin/bash

# ARWeb 前端自动化部署脚本
# 使用方法: ./deploy-frontend.sh [版本标识] [--use-existing]
# 示例: ./deploy-frontend.sh v1.4-loadtype-ui
# 示例: ./deploy-frontend.sh --use-existing  (使用现有dist文件)

set -e  # 遇到错误立即退出

# 配置变量
SERVER_ALIAS="ali_lanyu"
WEB_DIR="/home/web"
NODE_VERSION="18"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 解析参数
USE_EXISTING_BUILD=false
VERSION_TAG=""

for arg in "$@"; do
    case $arg in
        --use-existing)
            USE_EXISTING_BUILD=true
            ;;
        *)
            if [[ -z "$VERSION_TAG" ]]; then
                VERSION_TAG="$arg"
            fi
            ;;
    esac
done

# 如果没有指定版本标识，使用时间戳
if [[ -z "$VERSION_TAG" ]]; then
    VERSION_TAG=$(date +%Y%m%d_%H%M%S)
fi

PACKAGE_NAME="arweb-dist-${VERSION_TAG}.tar.gz"
BACKUP_DIR="web-backup-${VERSION_TAG}"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}ARWeb 前端自动化部署脚本${NC}"
echo -e "${BLUE}版本标识: ${VERSION_TAG}${NC}"
echo -e "${BLUE}使用现有构建: ${USE_EXISTING_BUILD}${NC}"
echo -e "${BLUE}========================================${NC}"

# 步骤1: 检查本地环境
echo -e "\n${YELLOW}[1/8] 检查本地环境...${NC}"
if [ ! -d "arweb" ]; then
    echo -e "${RED}错误: arweb目录不存在，请在项目根目录运行此脚本${NC}"
    exit 1
fi

if ! ssh -o ConnectTimeout=10 -q "${SERVER_ALIAS}" exit; then
    echo -e "${RED}错误: 无法连接到服务器 ${SERVER_ALIAS}${NC}"
    exit 1
fi

echo -e "${GREEN}✓ 本地环境检查通过${NC}"

# 步骤2: Node.js环境准备和项目构建
if [ "$USE_EXISTING_BUILD" = false ]; then
    echo -e "\n${YELLOW}[2/8] 准备Node.js环境并构建项目...${NC}"
    
    # 备份.npmrc并设置Node.js环境
    mv ~/.npmrc ~/.npmrc.backup 2>/dev/null || true
    
    # 设置Node.js环境
    export NVM_DIR="$HOME/.nvm"
    if [ -s "$NVM_DIR/nvm.sh" ]; then
        source "$NVM_DIR/nvm.sh"
        echo "切换到 Node.js ${NODE_VERSION}..."
        nvm use ${NODE_VERSION} || {
            echo -e "${RED}错误: Node.js ${NODE_VERSION} 未安装，请先安装${NC}"
            exit 1
        }
    else
        echo -e "${RED}错误: NVM 未安装，请先安装 NVM${NC}"
        exit 1
    fi
    
    cd arweb
    
    # 尝试构建项目
    echo "开始构建前端项目..."
    if npm run build; then
        echo -e "${GREEN}✓ 前端项目构建成功${NC}"
    else
        echo -e "${YELLOW}⚠ 构建失败，将使用现有dist目录${NC}"
        if [ ! -d "dist" ]; then
            echo -e "${RED}错误: dist目录不存在且构建失败${NC}"
            exit 1
        fi
        echo -e "${GREEN}✓ 使用现有dist目录${NC}"
    fi
    
    cd ..
    
    # 恢复.npmrc
    mv ~/.npmrc.backup ~/.npmrc 2>/dev/null || true
else
    echo -e "\n${YELLOW}[2/8] 跳过构建，使用现有dist目录...${NC}"
    if [ ! -d "arweb/dist" ]; then
        echo -e "${RED}错误: arweb/dist目录不存在${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ 确认现有dist目录存在${NC}"
fi

# 步骤3: 检查构建产物
echo -e "\n${YELLOW}[3/8] 检查构建产物...${NC}"
if [ ! -f "arweb/dist/index.html" ]; then
    echo -e "${RED}错误: index.html文件不存在${NC}"
    exit 1
fi

# 计算文件数量和大小
FILE_COUNT=$(find arweb/dist -type f | wc -l)
DIST_SIZE=$(du -sh arweb/dist | awk '{print $1}')
echo -e "${GREEN}✓ 构建产物包含 ${FILE_COUNT} 个文件，总大小: ${DIST_SIZE}${NC}"

# 步骤4: 打包构建文件
echo -e "\n${YELLOW}[4/8] 打包构建文件...${NC}"
tar -czf "${PACKAGE_NAME}" -C arweb/dist .
if [ $? -eq 0 ]; then
    PACKAGE_SIZE=$(ls -lh "${PACKAGE_NAME}" | awk '{print $5}')
    echo -e "${GREEN}✓ 打包完成: ${PACKAGE_NAME} (${PACKAGE_SIZE})${NC}"
else
    echo -e "${RED}✗ 打包失败${NC}"
    exit 1
fi

# 步骤5: 上传构建包
echo -e "\n${YELLOW}[5/8] 上传构建包到服务器...${NC}"
scp "${PACKAGE_NAME}" "${SERVER_ALIAS}:/tmp/"
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ 构建包上传成功${NC}"
else
    echo -e "${RED}✗ 构建包上传失败${NC}"
    exit 1
fi

# 步骤6: 备份生产环境文件
echo -e "\n${YELLOW}[6/8] 备份生产环境文件...${NC}"
ssh "${SERVER_ALIAS}" "cd ${WEB_DIR} && \
    if [ \$(find . -maxdepth 1 -name '*.js' -o -name '*.css' -o -name '*.html' | wc -l) -gt 0 ]; then \
        mkdir -p ${BACKUP_DIR} && \
        cp -r * ${BACKUP_DIR}/ 2>/dev/null || true && \
        echo '✓ 备份已创建: ${BACKUP_DIR}'; \
    else \
        echo '⚠ 生产环境为空，跳过备份'; \
    fi"

# 步骤7: 部署新版本
echo -e "\n${YELLOW}[7/8] 部署新版本...${NC}"
ssh "${SERVER_ALIAS}" "cd ${WEB_DIR} && \
    echo '清理旧文件...' && \
    rm -f *.js *.css *.html *.svg *.ico *.gz *.png *.jpg *.woff *.woff2 *.ttf *.eot 2>/dev/null || true && \
    rm -rf scripts icons static assets 2>/dev/null || true && \
    echo '解压新版本...' && \
    tar -xzf /tmp/${PACKAGE_NAME} && \
    echo '设置文件权限...' && \
    chown -R root:root * && \
    chmod -R 644 * && \
    find . -type d -exec chmod 755 {} \; && \
    echo '✓ 新版本部署完成'"

# 步骤8: 验证部署结果
echo -e "\n${YELLOW}[8/8] 验证部署结果...${NC}"

# 检查文件数量
DEPLOYED_FILES=$(ssh "${SERVER_ALIAS}" "find ${WEB_DIR} -maxdepth 1 -type f | wc -l")
echo -e "${GREEN}✓ 部署文件数量: ${DEPLOYED_FILES}${NC}"

# 测试HTTPS访问
echo "测试HTTPS访问..."
HTTPS_TEST=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 10 "https://app.lanyuxr.com" 2>/dev/null)
if [ "${HTTPS_TEST}" = "200" ]; then
    echo -e "${GREEN}✓ HTTPS访问正常 (${HTTPS_TEST})${NC}"
else
    echo -e "${YELLOW}⚠ HTTPS访问异常 (${HTTPS_TEST})${NC}"
fi

# 测试HTTP访问 (端口7891)
echo "测试HTTP访问..."
HTTP_TEST=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 10 "http://app.lanyuxr.com:7891" 2>/dev/null)
if [ "${HTTP_TEST}" = "200" ]; then
    echo -e "${GREEN}✓ HTTP访问正常 (${HTTP_TEST})${NC}"
else
    echo -e "${YELLOW}⚠ HTTP访问异常 (${HTTP_TEST})，可能需要检查Nginx配置${NC}"
fi

# 检查关键文件
echo "检查关键文件..."
KEY_FILES_CHECK=$(ssh "${SERVER_ALIAS}" "cd ${WEB_DIR} && ls -la index.html umi.*.js umi.*.css 2>/dev/null | wc -l")
if [ "${KEY_FILES_CHECK}" -ge 3 ]; then
    echo -e "${GREEN}✓ 关键文件存在 (index.html, umi.js, umi.css)${NC}"
else
    echo -e "${YELLOW}⚠ 部分关键文件可能缺失${NC}"
fi

# 清理本地临时文件
echo -e "\n${YELLOW}清理临时文件...${NC}"
rm -f "${PACKAGE_NAME}"
ssh "${SERVER_ALIAS}" "rm -f /tmp/${PACKAGE_NAME}"

# 部署完成总结
echo -e "\n${BLUE}========================================${NC}"
echo -e "${GREEN}🎉 前端部署完成!${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "📦 版本标识: ${GREEN}${VERSION_TAG}${NC}"
echo -e "🔄 备份目录: ${GREEN}${BACKUP_DIR}${NC}"
echo -e "🌐 HTTPS地址: ${GREEN}https://app.lanyuxr.com${NC}"
echo -e "🔧 HTTP测试: ${GREEN}http://app.lanyuxr.com:7891${NC}"

echo -e "\n${YELLOW}管理命令:${NC}"
echo -e "  查看文件: ${BLUE}ssh ${SERVER_ALIAS} \"ls -la ${WEB_DIR}/\"${NC}"
echo -e "  查看备份: ${BLUE}ssh ${SERVER_ALIAS} \"ls -la ${WEB_DIR}/${BACKUP_DIR}/\"${NC}"
echo -e "  测试访问: ${BLUE}curl -I https://app.lanyuxr.com${NC}"

echo -e "\n${GREEN}部署完成时间: $(date '+%Y-%m-%d %H:%M:%S')${NC}"