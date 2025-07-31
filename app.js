// app.js
App({
  onLaunch() {
    /*
    // 登录
    wx.login({
      success: res => {
        // 发送 res.code 到后台换取 openId, sessionKey, unionId
      }
    })
    */
  },
  globalData: {
    openid: '',
    domainWithProtocol: 'https://yaoculture.shenyuantek.com',
    // domainWithProtocol: 'https://testar.shenyuantek.com',
    openIdApi: '/api/guest/openId',
    statisticApi: '/api/guest/statistic',
    historyRecordApi: '/api/guest/historyRecord',
    historyApi: '/api/guest/history',
    collectionUuid: '',
    sceneList: [],
    loadType: 0,
  },
});
