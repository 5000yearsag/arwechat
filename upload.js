/**
 * 微信小程序上传和预览脚本
 * @author Lu Che
 */

const ci = require('miniprogram-ci')
const path = require('path')
const fs = require('fs')

// 从命令行参数获取配置
const version = process.argv[2] || '1.0.0'
const desc = process.argv[3] || `版本${version}自动上传`
const env = process.argv[4] || 'development' // development | trial | release

// 检查私钥文件是否存在
const privateKeyPath = path.resolve('./private.key')
if (!fs.existsSync(privateKeyPath)) {
  console.error('错误: 私钥文件 private.key 不存在')
  console.error('请从微信公众平台下载代码上传密钥，并保存为 private.key')
  process.exit(1)
}

const project = new ci.Project({
  appid: 'wx360d6d845e60562e', // 屿岚XR的appid
  type: 'miniProgram',
  projectPath: path.resolve('./'),
  privateKeyPath: privateKeyPath,
  ignores: [
    'node_modules/**/*', 
    '.git/**/*', 
    'upload.js', 
    'private.key',
    'arweb/**/*',
    'ar-platform/**/*',
    'arwechat-backup/**/*',
    'docs/**/*',
    'example-assets/**/*',
    '*.tar.gz',
    '*.zip',
    '*.log',
    '*.md',
    'dist-*.tar.gz',
    'README-CI.md',
    'CLAUDE.md',
    'DEPLOYMENT.md'
  ]
})

async function upload() {
  try {
    console.log(`开始上传小程序...`)
    console.log(`版本号: ${version}`)
    console.log(`描述: ${desc}`)
    console.log(`环境: ${env}`)
    
    const uploadResult = await ci.upload({
      project,
      version: version,
      desc: desc,
      setting: {
        es6: true,
        es7: true,
        minify: true,
        codeProtect: false,
        minifyJS: true,
        minifyWXML: true,
        minifyWXSS: true,
        autoPrefixWXSS: true,
      },
      onProgressUpdate: console.log,
    })
    
    console.log('上传成功!')
    console.log('上传结果:', uploadResult)
    
  } catch (error) {
    console.error('上传失败:', error)
    process.exit(1)
  }
}

// 预览功能
async function preview() {
  try {
    console.log('生成预览二维码...')
    
    const previewResult = await ci.preview({
      project,
      desc: desc,
      setting: {
        es6: true,
        es7: true,
        minify: true,
        codeProtect: false,
      },
      qrcodeFormat: 'image',
      qrcodeOutputDest: path.resolve('./preview.jpg'),
      onProgressUpdate: console.log,
    })
    
    console.log('预览二维码生成成功:', previewResult)
    console.log('二维码保存路径: ./preview.jpg')
    
  } catch (error) {
    console.error('预览生成失败:', error)
    process.exit(1)
  }
}

// 根据第一个参数决定执行上传还是预览
const action = process.argv[2]
if (action === 'preview') {
  preview()
} else {
  upload()
}