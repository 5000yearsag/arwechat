const appInstance = getApp();

const IDLE_LABEL = "轻触拍照 长按摄影";
const IDLE_CAPTURE_LABEL = "轻触拍照";
const IDLE_RECORD_LABEL = "轻触摄影";
const CAPTURING_LABEL = "拍照中...";
const START_RECORDING_LABEL = "准备中...";
const RECORDING_LABEL = "结束摄影";
const HOLDING_RECORDING_LABEL = "松手结束";
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
    holding: {
      type: Boolean,
      value: false,
    },
  },
  data: {
    status: IDLE_STATUS,
    label: IDLE_LABEL,
  },
  observers: {
    "status, type, holding": function (_status, _type, _holding) {
      let label = LABEL_MAP[_status] || "";
      if (_status === IDLE_STATUS) {
        if (_type === "capture") {
          label = IDLE_CAPTURE_LABEL;
        } else if (_type === "record") {
          label = IDLE_RECORD_LABEL;
        }
      } else if (_status === RECORDING_STATUS && _holding) {
        label = HOLDING_RECORDING_LABEL;
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
      } else if (
        !this.properties.holding &&
        this.data.status === RECORDING_STATUS
      ) {
        this.handleRecordStop(scene);
      }
    },
    // 截屏并分享
    async handleCapture(scene) {
      console.log("截屏并分享");
      if (scene && scene.share && scene.share.supported) {
        if (this.data.status !== IDLE_STATUS) return;
        this.setData({
          status: CAPTURING_STATUS,
        });

        // 默认的截屏配置
        const ShareCaptureDefaultOptions = {
          type: "jpg",
          quality: 0.8,
        };

        await scene.share
          .captureToFriends({
            ...ShareCaptureDefaultOptions,
          })
          .then(() => {
            // 分享成功后添加统计埋点
            wx.request({
              url: `${appInstance.globalData.domainWithProtocol}${appInstance.globalData.statisticApi}?collectionUuid=${appInstance.globalData.collectionUuid}&type=click3Count`,
              method: "GET",
              header: { "content-type": "application/json" },
              success: (res) => {
                console.log("拍照分享统计记录成功");
              },
              fail: (err) => {
                console.error("拍照分享统计记录失败:", err);
              }
            });
          })
          .catch((error) => {
            console.error("截屏分享失败:", error);
          })
          .finally(() => {
            this.setData({
              status: IDLE_STATUS,
            });
          });
      }
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
            this.triggerEvent("recordStarted");
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
          this.triggerEvent("recordFinished", filePath);
        }
      }
    },
  },
});
