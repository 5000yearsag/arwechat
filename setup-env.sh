#!/bin/bash

# 环境变量设置脚本
# 使用方法: source ./setup-env.sh

echo "设置AR平台环境变量..."

# 检查是否存在 .env 文件
if [ -f "arwechat-backup/ar-platform/.env" ]; then
    echo "从 .env 文件加载环境变量..."
    export $(cat arwechat-backup/ar-platform/.env | grep -v '^#' | xargs)
    echo "✓ 环境变量已加载"
else
    echo "⚠ 未找到 .env 文件，请手动设置以下环境变量："
    echo ""
    echo "必需的环境变量："
    echo "  export ALIYUN_ACCESS_KEY_ID='你的AccessKeyId'"
    echo "  export ALIYUN_ACCESS_KEY_SECRET='你的AccessKeySecret'"
    echo ""
    echo "可选的环境变量："
    echo "  export ALIYUN_OSS_ENDPOINT='oss-cn-beijing-internal.aliyuncs.com'"
    echo "  export ALIYUN_OSS_BUCKET_NAME='beijingxr'"
    echo "  export ALIYUN_OSS_URL_PREFIX='https://beijingxr.oss-cn-beijing.aliyuncs.com/'"
    echo ""
    echo "或者复制示例文件："
    echo "  cp arwechat-backup/ar-platform/.env.example arwechat-backup/ar-platform/.env"
    echo "  然后编辑 .env 文件填入实际值"
fi

# 验证必需的环境变量
if [ -z "$ALIYUN_ACCESS_KEY_ID" ] || [ -z "$ALIYUN_ACCESS_KEY_SECRET" ]; then
    echo ""
    echo "❌ 错误: 必需的环境变量未设置"
    echo "请设置 ALIYUN_ACCESS_KEY_ID 和 ALIYUN_ACCESS_KEY_SECRET"
    return 1 2>/dev/null || exit 1
else
    echo ""
    echo "✅ 必需的环境变量已设置"
    echo "   ALIYUN_ACCESS_KEY_ID: ${ALIYUN_ACCESS_KEY_ID:0:8}..."
    echo "   ALIYUN_ACCESS_KEY_SECRET: ${ALIYUN_ACCESS_KEY_SECRET:0:8}..."
fi