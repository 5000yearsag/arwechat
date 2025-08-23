const appInstance = getApp();
Page({
  data: {
    tableData: [], // 表格数据
    array: [],
  },
  onLoad (options) {},
  onShow () {
    this.getTableData()
  },
  // 获取数据
  async getTableData () {
    wx.request({
      url: `${appInstance.globalData.domainWithProtocol}${appInstance.globalData.historyApi}?openId=${appInstance.globalData.openid}`,
      method: "GET",
      header: { "content-type": "application/json" },
      success: (res) => {
        const names = res.data.data.map(item => item.collectionName)
        this.setData({
          array: names,
          tableData: res.data.data || []
        })
      } 
    })
  },
  handleFh (e) {
    console.log(e.currentTarget.dataset.collectionuuid)
    appInstance.globalData.collectionUuid = e.currentTarget.dataset.collectionuuid

    // const pages = getCurrentPages();
    // const prevPage = pages[pages.length - 2]; // 获取上一个页面实例
    // const params = {
    //   key: 'value' // 要传递的参数
    // };
    // // 修改上一个页面的数据
    // prevPage.setData({
    //   returnedParams: params
    // });
    wx.navigateBack(); // 返回上一个页面
  }
});
