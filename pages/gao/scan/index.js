import "../../../assets/effect/effect-tsbs";

const useXRComponentLoad = require("../../../hooks/useXRComponentLoad");
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
  data: {
    width: 300,
    height: 300,
    renderWidth: 300,
    renderHeight: 300,

    sceneReady: false,
    arAssetsLoaded: false,
    assetsLoadPercent: "00.00",
    catchAsset: false,

    showShareVideoDialog: false,
    shareVideoFilePath: "",

    navTitle: "",
    congratulation: "",
  },

  recordTimer: null,

  onLoad() {
    const { width, height, renderWidth, renderHeight } = useXRComponentLoad();
    this.setData({
      width,
      height,
      renderWidth,
      renderHeight,
    });
  },

  onUnload() {
    this.scene = null;
    this.recordTimer = null;
  },

  handleSceneReady({ detail }) {
    this.scene = detail.value;
    this.setData({
      sceneReady: true,
    });
  },

  handleArAssetsLoaded() {
    this.setData({
      arAssetsLoaded: true,
    });
  },

  handleArAssetsProgress({ detail }) {
    const { progress } = detail || {};
    this.setData({
      assetsLoadPercent: (progress * 100).toFixed(2),
    });
  },

  handleCatchAsset({ detail }) {
    // 埋点统计 - 识别成功播放
    if (detail) {
      wx.request({
        url: `${appInstance.globalData.domainWithProtocol}${appInstance.globalData.statisticApi}?collectionUuid=${appInstance.globalData.collectionUuid}&type=click2Count`,
        method: "GET",
        header: { "content-type": "application/json" },
        success: (res) => {}
      })
    }

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
  hideShareVideoDialog() {
    this.setData({
      showShareVideoDialog: false,
    });
  },
  shareVideoToMessage() {
    // 埋点统计 - 录像分享
    wx.request({
      url: `${appInstance.globalData.domainWithProtocol}${appInstance.globalData.statisticApi}?collectionUuid=${appInstance.globalData.collectionUuid}&type=click4Count`,
      method: "GET",
      header: { "content-type": "application/json" },
      success: (res) => {}
    })

    this.hideShareVideoDialog();
    if (this.data.shareVideoFilePath) {
      wx.shareVideoMessage({
        videoPath: this.data.shareVideoFilePath,
      });
    }
  },
});
