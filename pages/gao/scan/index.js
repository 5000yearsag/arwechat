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
    catchAsset: false,

    showShareVideoDialog: false,
    shareVideoFilePath: "",

    showPoster: false,
    posterPath: "",

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

  handleMaskTappedPoster(e) {
    this.setData({
      posterPath: "",
      showPoster: false,
    });
  },

  handleShare(e) {
    console.log('handleShare', this.data.posterPath);

    // 埋点统计 - 拍照分享
    wx.request({
      url: `${appInstance.globalData.domainWithProtocol}${appInstance.globalData.statisticApi}?collectionUuid=${appInstance.globalData.collectionUuid}&type=click3Count`,
      method: "GET",
      header: { "content-type": "application/json" },
      success: (res) => {}
    });

    wx.showShareImageMenu({
      path: this.data.posterPath,
      success: (e) => {
        
      },
      fail: (e) => {
        console.log(e, '========>>');
        this.handleShareError(e);
      }
    });
  },

  handleShareError(e) {
    wx.getSetting({
      success(res) {
        if (!res.authSetting['scope.writePhotosAlbum']) {
          wx.showModal({
            cancelText: '取消',
            content: `需要您授权相册权限`,
            confirmText: '去设置',
            success(res) {
              if (res.confirm) {
                wx.openSetting({
                  success(res) {}
                });
              }
            }
          });
        }
      }
    });
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
    console.log("开始分享录屏视频，路径:", this.data.shareVideoFilePath);
    console.log("当前collectionUuid:", appInstance.globalData.collectionUuid);
    
    this.hideShareVideoDialog();
    
    // 直接记录统计，不依赖分享结果
    console.log("记录录屏分享统计（不依赖分享结果）");
    wx.request({
      url: `${appInstance.globalData.domainWithProtocol}${appInstance.globalData.statisticApi}?collectionUuid=${appInstance.globalData.collectionUuid}&type=click4Count`,
      method: "GET",
      header: { "content-type": "application/json" },
      success: (res) => {
        console.log("录像分享统计记录成功:", res);
      },
      fail: (err) => {
        console.error("录像分享统计记录失败:", err);
      }
    });
    
    if (this.data.shareVideoFilePath) {
      wx.shareVideoMessage({
        videoPath: this.data.shareVideoFilePath,
        success: () => {
          console.log("录屏视频分享成功回调执行");
        },
        fail: (e) => {
          console.log("录屏视频分享失败:", e);
        },
        complete: () => {
          console.log("录屏视频分享完成（无论成功失败）");
        }
      });
    } else {
      console.log("没有录屏文件路径，无法分享");
    }
  },
});
