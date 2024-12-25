import "../../assets/effect/effect-tsbs";

// pages/preview/scan.js
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

    showPoster: false,
    posterPath: "",
  },
  handleShowPoster(e) {
    console.log('handleShowPoster', e.detail);
    this.setData({
      posterPath: e.detail,
      showPoster: true,
    })
  },
  handleMaskTappedPoster(e) {
    this.setData({
      posterPath: "",
      showPoster: false,
    })
  },
  handleShare(e) {
    console.log('handleShare', this.data.posterPath);

    wx.showShareImageMenu({
      path: this.data.posterPath,
      success: (e) => {
        
      },
      fail: (e) => {
        console.log(e,  '========>>');
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

  handleArTrackerReady({ detail }) {
    this.scene = detail.value;
    this.setData({
      arTrackerReady: true,
    });
  },

  handleArAssetsLoaded() {
    this.selectComponent('.sprite-progress').dismiss(() => {
      this.setData({
        arAssetsLoaded: true,
      });
    })
  },

  handleCatchAsset({ detail }) {
    this.setData({
      catchAsset: detail,
    });
  },

  handleRecord() {
    if (this.scene) {
      const recorderEl = this.selectComponent("#ar-scan-recorder");
      if (recorderEl.__data__.hideBtn) {
        return
      }
      if (recorderEl && recorderEl.handleRecord) {
        recorderEl.handleRecord(this.scene);
      }
    }
  },

  handleRecordStart() {
    // if (this.scene) {
    //   const recorderEl = this.selectComponent("#ar-scan-recorder");
    //   if (recorderEl && recorderEl.handleRecord) {
    //     recorderEl.handleRecordStart(this.scene);
    //   }
    // }
  },

  openShareVideoDialog({ detail }) {
    const tempVideoFilePath = detail;
    this.setData({
      showShareVideoDialog: true,
      shareVideoFilePath: tempVideoFilePath,
    });
  },
  hideShareVideoDialog() {
    this.setData({
      showShareVideoDialog: false,
    });
  },
  shareVideoToMessage() {
    this.hideShareVideoDialog();
    if (this.scene) {
      const recorderEl = this.selectComponent("#ar-scan-recorder");
      if (
        recorderEl &&
        recorderEl.shareVideoToMessage &&
        this.data.shareVideoFilePath
      ) {
        recorderEl.shareVideoToMessage(this.data.shareVideoFilePath);
      }
    }
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad() {
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
  },
});
