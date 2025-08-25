import "../../../assets/effect/effect-tsbs";

const appInstance = getApp();

const timeForamter = (time) => {
  let hour = 0;
  let min = 0;
  let sec = 0;
  let remainder = time;
  if (remainder >= 3600) {
    hour = Math.floor(remainder / 3600);
    remainder = remainder % 3600;
  }
  if (remainder >= 60) {
    min = Math.floor(remainder / 60);
    remainder = remainder % 60;
  }
  sec = Math.floor(remainder);
  return `${String(hour).padStart(2, "0")}:${String(min).padStart(
    2,
    "0"
  )}:${String(sec).padStart(2, "0")}`;
};

Page({
  /**
   * 页面的初始数据
   */
  data: {
    pageLoading: true,
    arTrackerReady: false,
    arAssetsLoaded: false,
    catchAsset: false,
    width: 300,
    height: 300,
    renderWidth: 300,
    renderHeight: 300,

    showShareVideoDialog: false,
    shareVideoFilePath: "",
  },

  recordTimer: null,
  onLoad() {
    // 显示原生加载提示
    wx.showLoading({
      title: '正在加载数据...',
      mask: true
    });
    
    const info = wx.getSystemInfoSync();
    const width = info.windowWidth;
    const height = info.windowHeight;
    const dpi = info.pixelRatio;
    this.setData({
      pageLoading: false,
      width,
      height,
      renderWidth: width * dpi,
      renderHeight: height * dpi,
    });
    
  },

  onUnload() {
    this.scene = null;
    this.recordTimer = null;
  },

  handleArTrackerReady({ detail }) {
    this.scene = detail.value;
    this.setData({
      arTrackerReady: true,
    });
    
    const appInstance = getApp();
    const loadType = appInstance.globalData.loadType || 0;
    
    // 普通加载模式：AR框架准备完成后显示资源加载提示
    if (loadType === 0) {
      wx.showLoading({
        title: 'AR资源加载中...',
        mask: true
      });
    } else {
      // 分段加载模式：AR框架准备完成后隐藏初始加载提示
      wx.hideLoading();
    }
  },

  handleArAssetsLoaded() {
    this.setData({
      arAssetsLoaded: true,
    });
    
    // 普通加载模式下，资源加载完成后隐藏加载提示
    const appInstance = getApp();
    const loadType = appInstance.globalData.loadType || 0;
    if (loadType === 0) {
      wx.hideLoading();
    }
  },

  handleCatchAsset({ detail }) {
    this.setData({
      catchAsset: detail,
    });
  },

  handleRecord() {
    if (this.scene) {
      const recorderEl = this.selectComponent("#ar-scan-recorder");
      if (recorderEl && recorderEl.handleRecord) {
        recorderEl.handleRecord(this.scene);
      }
    }
  },

  recordFinished({ detail } = {}) {
    clearInterval(this.recordTimer);
    this.setData({
      navTitle: "",
    });
    
    const tempVideoFilePath = detail;
    if (tempVideoFilePath) {
      this.setData({
        showShareVideoDialog: true,
        shareVideoFilePath: tempVideoFilePath,
      });
    }
  },

  handleRecordStart() {
    if (this.scene) {
      const recorderEl = this.selectComponent("#ar-scan-recorder");
      if (recorderEl && recorderEl.handleRecord) {
        recorderEl.handleRecordStart(this.scene);
      }
    }
  },

  handleRecordStop() {
    if (this.scene) {
      const recorderEl = this.selectComponent("#ar-scan-recorder");
      if (recorderEl && recorderEl.handleRecordStop) {
        recorderEl.handleRecordStop(this.scene);
      }
    }
  },

  hideShareVideoDialog() {
    this.setData({
      showShareVideoDialog: false,
    });
  },
  
  shareVideoToMessage() {
    this.hideShareVideoDialog();
    if (this.data.shareVideoFilePath) {
      wx.shareVideoMessage({
        videoPath: this.data.shareVideoFilePath,
        success: () => {
          // 分享成功后记录统计
          wx.request({
            url: `${appInstance.globalData.domainWithProtocol}${appInstance.globalData.statisticApi}?collectionUuid=${appInstance.globalData.collectionUuid}&type=click4Count`,
            method: "GET",
            header: { "content-type": "application/json" },
            success: (res) => {},
            fail: (err) => {}
          });
        },
        fail: (e) => {},
      });
    }
  },

  recordStarted() {
    clearInterval(this.recordTimer);
    const startTs = new Date().getTime();
    this.recordTimer = setInterval(() => {
      const elapsed = (new Date().getTime() - startTs) / 1000; // 秒
      const counterLabel = timeForamter(elapsed);
      this.setData({ navTitle: counterLabel });
    }, 1000);
  },

  recordFinished({ detail } = {}) {
    clearInterval(this.recordTimer);
    const tempVideoFilePath = detail;
    if (tempVideoFilePath) {
      this.setData({
        showShareVideoDialog: true,
        shareVideoFilePath: tempVideoFilePath,
      });
    }
  },
  hideShareVideoDialog() {
    this.setData({
      showShareVideoDialog: false,
    });
  },
  shareVideoToMessage() {
    this.hideShareVideoDialog();
    
    // 记录录屏分享统计
    wx.request({
      url: `${appInstance.globalData.domainWithProtocol}${appInstance.globalData.statisticApi}?collectionUuid=${appInstance.globalData.collectionUuid}&type=click4Count`,
      method: "GET",
      header: { "content-type": "application/json" },
      success: (res) => {},
      fail: (err) => {}
    });
    
    if (this.data.shareVideoFilePath) {
      wx.shareVideoMessage({
        videoPath: this.data.shareVideoFilePath,
        success: () => {},
        fail: (e) => {},
        complete: () => {}
      });
    }
  },

});