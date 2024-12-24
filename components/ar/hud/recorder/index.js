const appInstance = getApp();

const IDLE_LABEL = "轻触拍照";
const IDLE_CAPTURE_LABEL = "轻触拍照";
const IDLE_RECORD_LABEL = "轻触摄影";
const CAPTURING_LABEL = "拍照中...";
const START_RECORDING_LABEL = "准备中...";
const RECORDING_LABEL = "结束摄影";
const STOP_RECORDING_LABEL = "正在结束...";

const IDLE_STATUS = 0;
const CAPTURING_STATUS = 1;
const START_RECORDING_STATUS = 2;
const RECORDING_STATUS = 3;
const STOP_RECORDING_STATUS = 4;

const LABEL_MAP = [
  IDLE_LABEL,
  CAPTURING_LABEL,
  START_RECORDING_LABEL,
  RECORDING_LABEL,
  STOP_RECORDING_LABEL,
];

Component({
  properties: {
    type: {
      type: String,
      value: "all",
    },
  },
  data: {
    status: IDLE_STATUS,
    label: IDLE_LABEL,
    openShareMenu: false,
  },
  observers: {
    "status, type": function (_status, _type) {
      let label = LABEL_MAP[_status] || "";
      if (_status === IDLE_STATUS) {
        if (_type === "capture") {
          label = IDLE_CAPTURE_LABEL;
        } else if (_type === "record") {
          label = IDLE_RECORD_LABEL;
        }
      }
      this.setData({
        label,
      });
    },
  },
  methods: {
    // tap事件入口，分别处理截屏和结束录屏的情况
    handleRecord(scene) {
      if (this.data.status === IDLE_STATUS) {
        if (this.properties.type === "record") {
          this.handleRecordStart(scene);
        } else {
          this.handleCapture(scene);
        }
      } else if (this.data.status === RECORDING_STATUS) {
        this.handleRecordStop(scene);
      }
    },
    // 截屏并分享
    async handleCapture(scene) {
      // if (scene && scene.share && scene.share.supported) {
      //   if (this.data.status !== IDLE_STATUS) return;
      //   this.setData({
      //     status: CAPTURING_STATUS,
      //   });

      //   // 默认的截屏配置
      //   const ShareCaptureDefaultOptions = {
      //     type: "jpg",
      //     quality: 0.8,
      //   };

      //   await scene.share
      //     .captureToFriends({
      //       ...ShareCaptureDefaultOptions,
      //     })
      //     .finally(() => {
      //       this.setData({
      //         status: IDLE_STATUS,
      //       });
      //     });
      // }
      wx.getSetting({
        success: async res => {
          if (!res.authSetting['scope.camera']) {
            wx.showModal({
              cancelText: '取消',
              content: `需要您授权相机权限`,
              confirmText: '去设置',
              success(res) {
                if (res.confirm) {
                  wx.openSetting({
                    success(res) {
                      if (res.authSetting['scope.camera']) {
                        wx.redirectTo({
                          url: `/pages/preview/scan`
                        });
                      }
                    }
                  });
                }
              }
            });
          } else {
            await this.doCapture(scene);
          }
        }
      });
    },
    async doCapture(scene) {
      console.log("截屏并分享");
      if (scene && scene.share && scene.share.supported) {
        if (this.data.status !== IDLE_STATUS) return;
        this.setData({
          status: CAPTURING_STATUS,
        });

        // 默认的截屏配置
        const ShareCaptureDefaultOptions = {
          type: "jpg"
        };

        wx.showLoading({
          mask: true,
        })
        try {
          let path = await scene.share.captureToDataURLAsync(ShareCaptureDefaultOptions)

          const { canvas, width, height } = await this.getCanvas()
          const ctx = canvas.getContext('2d')
          canvas.width = width
          canvas.height = height
          const image = await this.createImage(canvas, appInstance.globalData.shareImgUrl)
          ctx.drawImage(image, 0, 0, width, height)
          const overlapWidth = width - 24 * 3;
          const overlapHeight = overlapWidth / 2 * 3;
          const overlap = await this.createImage(canvas, path);
          const canvasAspect = 2 / 3;
          const imageAspect = overlap.width / overlap.height;
          let sourceWidth,
            sourceHeight,
            offsetX = 0,
            offsetY = 0;
          if (imageAspect > canvasAspect) {
            sourceHeight = overlap.height;
            sourceWidth = overlap.height * canvasAspect;
            offsetX = (overlap.width - sourceWidth) / 2;
          } else {
            sourceWidth = overlap.width;
            sourceHeight = overlap.width / canvasAspect;
            offsetY = (overlap.height - sourceHeight) / 2;
          }
          const x = 12 * 3;
          const y = 20 * 3;
          const radius = 8 * 3;
          ctx.beginPath();
          ctx.moveTo(x + radius, y);
          ctx.arcTo(x + overlapWidth, y, x + overlapWidth, y + overlapHeight, radius);
          ctx.arcTo(x + overlapWidth, y + overlapHeight, x, y + overlapHeight, radius);
          ctx.arcTo(x, y + overlapHeight, x, y, radius);
          ctx.arcTo(x, y, x + overlapWidth, y, radius);
          ctx.closePath();
          ctx.clip();


          ctx.drawImage(overlap, offsetX, offsetY, sourceWidth, sourceHeight,
            x, y, overlapWidth, overlapHeight)

          const merged = await wx.canvasToTempFilePath({
            canvas: canvas,
          })
          wx.hideLoading()
          this.triggerEvent('showPoster', merged.tempFilePath);
        } catch (error) {
          wx.hideLoading();
          console.log(error);
          if (error.errno === 103) {
            this.triggerEvent('shareError', error);
          }
        }
        this.setData({
          status: IDLE_STATUS,
        });
      }
    },
    createImage(canvas, src) {
      return new Promise((resolve, reject) => {
        const image = canvas.createImage()
        image.onload = () => {
          resolve(image)
        }
        image.src = src
      })
    },
    getCanvas() {
      return new Promise((resolve, reject) => {
        this.createSelectorQuery()
          .select('#myCanvas')
          .fields({ node: true, size: true })
          .exec((res) => {
            resolve({
              ...res[0],
              canvas: res[0].node,
            })
          })
      })
    },
    // 开始录屏
    async handleRecordStart(scene) {
      console.log("开始录屏");
      if (scene && scene.share && scene.share.supported) {
        if (this.data.status !== IDLE_STATUS) return;
        this.setData({
          status: START_RECORDING_STATUS,
        });

        if (scene.share.recordState !== 0) return;

        // 默认的录屏配置
        const ShareRecordDefaultOptions = {
          fps: 30,
          width: scene.width,
          height: scene.height,
          videoBitsPerSecond: 3000,
        };

        await scene.share
          .recordStart({
            ...ShareRecordDefaultOptions,
          })
          .then(() => {
            this.setData({
              status: RECORDING_STATUS,
            });
          })
          .catch((e) => {
            this.setData({
              status: IDLE_STATUS,
            });
            console.log("start record error:", e);
          });
      }
    },
    // 停止录屏并保存相册
    async handleRecordStop(scene) {
      console.log("停止录屏并分享");
      if (this.data.status === RECORDING_STATUS) {
        this.setData({
          status: STOP_RECORDING_STATUS,
        });
        const filePath = await scene.share
          .recordFinishToTempFile()
          .finally(() => {
            this.setData({
              status: IDLE_STATUS,
            });
          });
        if (filePath) {
          this.triggerEvent("openShareVideoDialog", filePath);
        }
      }
    },

    // 分享视频
    shareVideoToMessage(filePath) {
      wx.shareVideoMessage({
        videoPath: filePath,
        success: () => {
          console.log("share video to messge success");
        },
        fail: (e) => {
          console.log(e);
        },
      });
    },
  },
});
