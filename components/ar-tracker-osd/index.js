const appInstance = getApp();

Component({
  data: {
    // py1: -120, // 左
    // py2: -50, // 右
    // py3: 50, // 上
    // py4: 120, // 下
    py1: -420, // 左
    py2: 450, // 右
    py3: -350, // 上
    py4: 420, // 下
    assetsLoaded: false,
    sceneList: [],
    timer: null,
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
    handleAssetsLoaded: function (a) {
      // console.log('ok')
      // console.log(a)
      // const innerAudioContext = wx.createInnerAudioContext({
      //   useWebAudioImplement: false // 是否使用 WebAudio 作为底层音频驱动，默认关闭。对于短音频、播放频繁的音频建议开启此选项，开启后将获得更优的性能表现。由于开启此选项后也会带来一定的内存增长，因此对于长音频建议关闭此选项
      // })
      // innerAudioContext.src = 'http://music.163.com/song/media/outer/url?id=447925558.mp3'
      // innerAudioContext.play() // 播放
      // innerAudioContext.pause() // 暂停
      // innerAudioContext.stop() // 停止
      // innerAudioContext.destroy() // 释放音频资源
      this.setData({
        assetsLoaded: true,
      });
      this.triggerEvent("arAssetsLoaded");
    },
    handleTrackerSwitch: function ({ detail, target }) {
      console.log('handleTrackerSwitch-----')
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
        if (an >= this.py1 && an <= this.py2 && an2 >= this.py3 && an2 <= this.py4) {
          jd = true
        }
        console.log(active)
        active ? video.play() : video.stop();
        this.triggerEvent("catchAsset", active);
        this.setData({ visibleVideo: active })
        // if (active && !jd) {
        //   console.log('a1')
        //   video.stop();
        //   this.triggerEvent("catchAsset", false);
        //   this.setData({ visibleVideo: false })
        // } else {
        //   console.log('a2')
        //   active ? video.play() : video.stop();
        //   this.triggerEvent("catchAsset", active);
        //   this.setData({ visibleVideo: active })
        // }
        // this.handleAngleHide(active)
      }
    },
    handleSceneData: function () {
      let sceneList = [];
      if (
        appInstance &&
        appInstance.globalData &&
        appInstance.globalData.sceneList
      ) {
        sceneList = appInstance.globalData.sceneList || [];
        console.log('sceneList-----------')
        console.log(sceneList)
      }
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
        if (an >= this.py1 && an <= this.py2 && an2 >= this.py3 && an2 <= this.py4) {
          jd = true
        }
        if (!jd) {
          console.log('b1')
          const video = this.scene.assets.getAsset("video-texture", this.timerVideoId);
          video.stop();
          this.triggerEvent("catchAsset", false);
          this.setData({ visibleVideo: false })
        } else {
          console.log('b2')
          const video = this.scene.assets.getAsset("video-texture", this.timerVideoId);
          video.play();
          this.triggerEvent("catchAsset", true);
          this.setData({ visibleVideo: true })
        }
      }, 1000)
    }
  },
});
