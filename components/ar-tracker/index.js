const appInstance = getApp();

let innerAudioContext = null
let innerAudioUrl = null

Component({
  data: {
    loadType: appInstance.globalData.loadType || 0,
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
      console.log('loadType----------')
      console.log(this.data.loadType)
      // 埋点统计
      wx.request({
        url: `${appInstance.globalData.domainWithProtocol}${appInstance.globalData.statisticApi}?collectionUuid=${appInstance.globalData.collectionUuid}&type=click5Count`,
        method: "GET",
        header: { "content-type": "application/json" },
        success: (res) => {}
      })
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
      const { assetIndex } = dataset || {};
      const arr = this.data.sceneList[assetIndex]
      console.log(arr)
      arr.map(item => {
        // video
        if (item.type === 'video' && item.sceneUuid) {
          const video = this.scene.assets.getAsset("video-texture", `${item.type}_${item.sceneUuid}`);
          active ? video.play() : video.stop();
        }
        // mp3
        if (item.sceneUuid && item.type === "model" && item.audioResourceUrl) {
          if (!innerAudioContext) {
            innerAudioContext = wx.createInnerAudioContext({
              useWebAudioImplement: true // 是否使用 WebAudio 作为底层音频驱动，默认关闭。对于短音频、播放频繁的音频建议开启此选项，开启后将获得更优的性能表现。由于开启此选项后也会带来一定的内存增长，因此对于长音频建议关闭此选项
            })
            innerAudioContext.src = item.audioResourceUrl
            innerAudioContext.loop = true
          }
          if (innerAudioUrl && innerAudioUrl !== item.audioResourceUrl) {
            innerAudioContext.stop() // 停止
            innerAudioContext.destroy() // 释放音频资源
            innerAudioContext = wx.createInnerAudioContext({
              useWebAudioImplement: true // 是否使用 WebAudio 作为底层音频驱动，默认关闭。对于短音频、播放频繁的音频建议开启此选项，开启后将获得更优的性能表现。由于开启此选项后也会带来一定的内存增长，因此对于长音频建议关闭此选项
            })
            innerAudioContext.src = item.audioResourceUrl
          }
          active ? innerAudioContext.play() : innerAudioContext.pause()
          innerAudioUrl = item.audioResourceUrl
          innerAudioContext.loop = true
        }
      })

      // 埋点统计
      wx.request({
        url: `${appInstance.globalData.domainWithProtocol}${appInstance.globalData.statisticApi}?collectionUuid=${appInstance.globalData.collectionUuid}&type=click2Count`,
        method: "GET",
        header: { "content-type": "application/json" },
        success: (res) => {}
      })
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
