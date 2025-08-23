const appInstance = getApp();

const py1 = -150; // 左
const py2 = -60; // 右
const py3 = 20; // 上
const py4 = 150; // 下

Component({
  data: {
    assetsLoaded: false,
    sceneList: [],
    timerVideoId: '',
    visibleVideo: false
  },
  detached () {
    this.handleAngleHide(false)
  },
  methods: {
    handleReady: function ({ detail }) {
      this.scene = detail.value;
      this.triggerEvent("arTrackerReady", detail);
      this.handleSceneData();
    },
    handleAssetsLoaded: function () {
      this.setData({
        assetsLoaded: true,
      });
      this.triggerEvent("arAssetsLoaded");
    },
    handleTrackerSwitch: function ({ detail, target }) {
      console.log(1)
      const active = detail.value;
      const { dataset } = target || {};
      const { assetId, assetType } = dataset || {};
      if (assetId && assetType === "video") {
        const xrSystem = wx.getXrFrameSystem();
        let leftTRS = this.scene.getElementById('videoNode').getComponent(xrSystem.Transform);
        let cameraTrs = this.scene.getElementById('camera').getComponent(xrSystem.Transform);
        const video = this.scene.assets.getAsset("video-texture", assetId);
        this.timerVideoId = assetId
        let jd = false
        let raw = cameraTrs.worldPosition.clone().angleTo(leftTRS.worldPosition.clone())._raw
        let radian = Math.atan2(raw[0], raw[1]);
        let an = radian * (180 / Math.PI)
        let radian2 = Math.atan2(cameraTrs.rotation.clone()._raw[0], cameraTrs.rotation.clone()._raw[2]);
        let an2 = radian2 * (180 / Math.PI)
        if (an >= py1 && an <= py2 && an2 >= py3 && an2 <= py4) {
          jd = true
        }
        if (active && !jd) {
          video.stop();
          this.triggerEvent("catchAsset", false);
          this.setData({ visibleVideo: false })
        } else {
          active ? video.play() : video.stop();
          this.triggerEvent("catchAsset", active);
          this.setData({ visibleVideo: active })
        }
        this.handleAngleHide(active)
      }
      this.triggerEvent("catchAsset", active);
    },
    handleSceneData: function () {
      let sceneList = [
        {
          'sceneUuid': '240812182016929',
          'sceneImgUrl': 'https://res.paquapp.com/boxonline/activity/202412_dimoo_ar/cover.jpeg',
          'arResourceUrl': 'https://res.paquapp.com/boxonline/activity/202412_dimoo_ar/video.mp4',
          'type': 'video',
          'videoEffect': 'tsbs',
          'xScale': 1,
          'yScale': 1.2,
          'position': {'x': 0, 'y': 0, 'z': 0},
          'rotation': {'x': 0, 'y': 0, 'z': 0},
          'scale': {'x': 1, 'y': 1, 'z': 1}
        }
      ];
      // let sceneList = [
      //   {
      //     'sceneUuid': '240812182016929',
      //     'sceneImgUrl': 'https://res.paquapp.com/boxonline/activity/202412_dimoo_ar/cover.jpeg',
      //     'arResourceUrl': 'https://res.paquapp.com/boxonline/activity/202412_dimoo_ar/video.mp4',
      //     'type': 'video',
      //     'videoEffect': 'tsbs',
      //     'xScale': 1,
      //     'yScale': 1.325,
      //     'position': {'x': 0, 'y': 0, 'z': 0},
      //     'rotation': {'x': 0, 'y': 0, 'z': 0},
      //     'scale': {'x': 1, 'y': 0.5, 'z': 1}
      //   }
      // ];
      // if (
      //   appInstance &&
      //   appInstance.globalData &&
      //   appInstance.globalData.sceneList
      // ) {
      //   sceneList = appInstance.globalData.sceneList || [];
      // }
      console.log(sceneList)
      this.setData({
        sceneList,
      });
    },
    handleARTrackerState({detail}) {
      const tracker = detail.value;
      const {state, errorMessage} = tracker;
    },
    // active true启动 false关闭
    handleAngleHide (active) {
      this.videoEle = this.scene.assets.getAsset("video-texture", this.timerVideoId);
      this.timer && clearInterval(this.timer)
      if (!active) {
        return
      }
      this.timer = setInterval(() => {
        const xrSystem = wx.getXrFrameSystem();
        let leftTRS = this.scene.getElementById('videoNode').getComponent(xrSystem.Transform);
        let cameraTrs = this.scene.getElementById('camera').getComponent(xrSystem.Transform);
        let jd = false
        let raw = cameraTrs.worldPosition.clone().angleTo(leftTRS.worldPosition.clone())._raw
        let radian = Math.atan2(raw[0], raw[1]);
        let an = radian * (180 / Math.PI)
        let radian2 = Math.atan2(cameraTrs.rotation.clone()._raw[0], cameraTrs.rotation.clone()._raw[2]);
        let an2 = radian2 * (180 / Math.PI)
        if (an >= py1 && an <= py2 && an2 >= py3 && an2 <= py4) {
          jd = true
        }
        if (!jd) {
          this.videoEle.stop();
          this.triggerEvent("catchAsset", false);
          this.setData({ visibleVideo: false })
        } else {
          this.videoEle.play();
          this.triggerEvent("catchAsset", true);
          this.setData({ visibleVideo: true })
        }
      }, 1000)
    }
  },
});
