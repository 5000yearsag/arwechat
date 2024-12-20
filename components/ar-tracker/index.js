const appInstance = getApp();

let innerAudioContext = null
let innerAudioUrl = null

Component({
  data: {
    assetsLoaded: false,
    sceneList: [],
  },
  detached () {
    if (innerAudioContext) {
      innerAudioContext.stop() // 停止
    }
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
      const active = detail.value;
      const { dataset } = target || {};
      const { assetId, assetType, assetMp3 } = dataset || {};
      if (assetId && assetType === "video") {
        const video = this.scene.assets.getAsset("video-texture", assetId);
        active ? video.play() : video.stop();
      }
      if (assetId && assetType === "model" && assetMp3) {
        if (!innerAudioContext) {
          innerAudioContext = wx.createInnerAudioContext({
            useWebAudioImplement: true // 是否使用 WebAudio 作为底层音频驱动，默认关闭。对于短音频、播放频繁的音频建议开启此选项，开启后将获得更优的性能表现。由于开启此选项后也会带来一定的内存增长，因此对于长音频建议关闭此选项
          })
          innerAudioContext.src = assetMp3
          innerAudioContext.loop = true
        }
        if (innerAudioUrl && innerAudioUrl !== assetMp3) {
          innerAudioContext.stop() // 停止
          innerAudioContext.destroy() // 释放音频资源
          innerAudioContext = wx.createInnerAudioContext({
            useWebAudioImplement: true // 是否使用 WebAudio 作为底层音频驱动，默认关闭。对于短音频、播放频繁的音频建议开启此选项，开启后将获得更优的性能表现。由于开启此选项后也会带来一定的内存增长，因此对于长音频建议关闭此选项
          })
          innerAudioContext.src = assetMp3
        }
        active ? innerAudioContext.play() : innerAudioContext.pause()
        innerAudioUrl = assetMp3
        innerAudioContext.loop = true
      }
      this.triggerEvent("catchAsset", active);
    },
    handleSceneData: function () {
      let sceneList = [];
      if (
        appInstance &&
        appInstance.globalData &&
        appInstance.globalData.sceneList
      ) {
        sceneList = appInstance.globalData.sceneList || [];
      }
      this.setData({
        sceneList,
      });
    },
  },
});
