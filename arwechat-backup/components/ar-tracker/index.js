const appInstance = getApp();

let audioContextMap = {} // 存储每个资源的音频上下文

Component({
  data: {
    loadType: appInstance.globalData.loadType || 0,
    assetsLoaded: false,
    sceneList: [],
  },
  detached () {
    // 清理所有音频上下文
    Object.values(audioContextMap).forEach(context => {
      if (context) {
        context.stop();
        context.destroy();
      }
    });
    audioContextMap = {};
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
        
        // 每个资源独立的音频播放（支持叠加播放）
        if (item.audioResourceUrl && item.sceneUuid) {
          const contextKey = `${assetIndex}_${item.sceneUuid}`;
          
          if (!audioContextMap[contextKey]) {
            audioContextMap[contextKey] = wx.createInnerAudioContext({
              useWebAudioImplement: true
            });
            audioContextMap[contextKey].src = item.audioResourceUrl;
            audioContextMap[contextKey].loop = true;
          }
          
          // 如果音频URL变化，重新创建
          if (audioContextMap[contextKey].src !== item.audioResourceUrl) {
            audioContextMap[contextKey].stop();
            audioContextMap[contextKey].destroy();
            audioContextMap[contextKey] = wx.createInnerAudioContext({
              useWebAudioImplement: true
            });
            audioContextMap[contextKey].src = item.audioResourceUrl;
            audioContextMap[contextKey].loop = true;
          }
          
          active ? audioContextMap[contextKey].play() : audioContextMap[contextKey].pause();
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
