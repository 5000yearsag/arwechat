const axios = require('axios')

const APPID = 'wx360d6d845e60562e'
const APPSECRET = '1c6324d57b647323f7d5317f316bdb81'

async function getAccessToken() {
  const url = `https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=${APPID}&secret=${APPSECRET}`
  
  try {
    const response = await axios.get(url)
    const data = response.data
    
    if (data.access_token) {
      return data.access_token
    } else {
      throw new Error(`Failed to get access token: ${data.errmsg}`)
    }
  } catch (error) {
    console.error('Error getting access token:', error)
    throw error
  }
}

async function queryScheme(scheme) {
  try {
    const accessToken = await getAccessToken()
    const url = `https://api.weixin.qq.com/wxa/queryscheme?access_token=${accessToken}`
    
    const requestBody = {
      scheme: scheme
    }
    
    const response = await axios.post(url, requestBody)
    return response.data
  } catch (error) {
    console.error('Error querying URL scheme:', error)
    throw error
  }
}

async function generateUrlScheme(jumpWxa) {
  try {
    const accessToken = await getAccessToken()
    const url = `https://api.weixin.qq.com/wxa/generatescheme?access_token=${accessToken}`
    
    const requestBody = {
      jump_wxa: jumpWxa,
      is_expire: false
    }
    
    const response = await axios.post(url, requestBody)
    const data = response.data
    
    if (data.errcode === 0) {
      return data.openlink
    } else {
      throw new Error(`Failed to generate URL scheme: ${data.errmsg}`)
    }
  } catch (error) {
    console.error('Error generating URL scheme:', error)
    throw error
  }
}

function convertToCorrectFormat(pathWithQuery) {
  const [path, queryString] = pathWithQuery.split('?')
  const params = new URLSearchParams(queryString)
  const collectionUuid = params.get('collectionUuid')
  
  if (!collectionUuid) {
    throw new Error('collectionUuid not found in query string')
  }
  
  const apiUrl = `https://app.lanyuxr.com/api/guest/getAllSceneByCollection?collectionUuid=${collectionUuid}`
  const encodedUrl = encodeURIComponent(apiUrl)
  
  return {
    path: path,
    query: `url=${encodedUrl}`,
    env_version: 'release'
  }
}

async function generateAndVerifyScheme(jumpWxa) {
  try {
    console.log('Generating URL scheme...')
    const urlScheme = await generateUrlScheme(jumpWxa)
    console.log('Generated:', urlScheme)
    
    console.log('Verifying URL scheme...')
    const verifyResult = await queryScheme(urlScheme)
    
    if (verifyResult.errcode === 0) {
      console.log('✅ URL scheme verified successfully!')
      console.log('Scheme info:', JSON.stringify(verifyResult.scheme_info, null, 2))
      return {
        urlScheme,
        verified: true,
        schemeInfo: verifyResult.scheme_info
      }
    } else {
      console.log('❌ Verification failed:', verifyResult.errmsg)
      return {
        urlScheme,
        verified: false,
        error: verifyResult.errmsg
      }
    }
  } catch (error) {
    console.error('Error:', error)
    throw error
  }
}

async function generateFromPathQuery(pathWithQuery) {
  const jumpWxa = convertToCorrectFormat(pathWithQuery)
  console.log('Converted format:', JSON.stringify(jumpWxa, null, 2))
  return await generateAndVerifyScheme(jumpWxa)
}

async function generateNFCUrlScheme() {
  const jumpWxa = {
    path: "/pages/gao/intro/index",
    query: "collectionUuid=250620222857518",
    env_version: "release"
  }
  
  try {
    const urlScheme = await generateUrlScheme(jumpWxa)
    console.log('Generated URL Scheme:', urlScheme)
    return urlScheme
  } catch (error) {
    console.error('Error:', error)
    throw error
  }
}

module.exports = {
  getAccessToken,
  generateUrlScheme,
  queryScheme,
  convertToCorrectFormat,
  generateAndVerifyScheme,
  generateFromPathQuery,
  generateNFCUrlScheme
}
