import "../../assets/effect/effect-tsbs";
const appInstance = getApp();

Page({
  moveTimes: 0,
  data: {
    left: 0,
    top: 0,
    width: 0,
    height: 0,
    renderWidth: 0,
    renderHeight: 0,
    windowHeight: 1000
  },
  handleShare(e) {
    // 埋点统计
    wx.request({
      url: `${appInstance.globalData.domainWithProtocol}${appInstance.globalData.statisticApi}?collectionUuid=${appInstance.globalData.collectionUuid}&type=click3Count`,
      method: "GET",
      header: { "content-type": "application/json" },
      success: (res) => {}
    })
    const recorderEl = this.selectComponent("#main-frame");
    recorderEl.handleShare()
  },
  handleArTrackerReady({ detail }) {
    this.scene = detail.value;
    this.setData({
      arTrackerReady: true,
    });
  },
  resetModel() {
    const recorderEl = this.selectComponent("#main-frame");
    recorderEl.resetModel()
  },
  onLoad() {
    const info = wx.getSystemInfoSync();
    const width = info.windowWidth;
    const height = info.windowHeight;
    const dpi = info.pixelRatio;
    this.setData({
      width,
      height,
      renderWidth: width * dpi,
      renderHeight: height * dpi
    });
  },

});