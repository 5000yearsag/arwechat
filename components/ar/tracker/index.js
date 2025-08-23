const appInstance = getApp();

let audioContextMap = {} // 存储每个资源的音频上下文

Component({
  data: {
    assetsLoaded: false,
    sceneList: [],
  },
  detached() {
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

      // 埋点统计 - 资源加载
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
      const { assetId, assetType } = dataset || {};
      if (assetId && assetType === "video") {
        const video = this.scene.assets.getAsset("video-texture", assetId);
        active ? video.play() : video.stop();
      }

      // 每个资源独立的音频播放（支持叠加播放）
      if (target) {
        const item = this.data.sceneList.find(sceneData => sceneData.sceneUuid === dataset.sceneUuid);
        if (item && item.audioResourceUrl && item.sceneUuid) {
          const contextKey = `${item.sceneUuid}_audio`;
          
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
      }

      // 埋点统计 - 识别成功播放
      if (active) {
        wx.request({
          url: `${appInstance.globalData.domainWithProtocol}${appInstance.globalData.statisticApi}?collectionUuid=${appInstance.globalData.collectionUuid}&type=click2Count`,
          method: "GET",
          header: { "content-type": "application/json" },
          success: (res) => {}
        })
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
        sceneList = appInstance.globalData.sceneList.map(item => {
          if (item.spaceParam && typeof item.spaceParam === 'string') {
            try {
              const parsedSpaceParam = JSON.parse(item.spaceParam);
              return {
                ...item,
                position: parsedSpaceParam.position || { x: 0, y: 0, z: 0 },
                rotation: parsedSpaceParam.rotation || { x: 0, y: 0, z: 0 },
                scale: parsedSpaceParam.scale || { x: 1, y: 1, z: 1 },
              };
            } catch (e) {
              console.error('Failed to parse spaceParam:', item.spaceParam, e);
              return {
                ...item,
                position: { x: 0, y: 0, z: 0 },
                rotation: { x: 0, y: 0, z: 0 },
                scale: { x: 1, y: 1, z: 1 },
              };
            }
          }
          return {
            ...item,
            position: item.position || { x: 0, y: 0, z: 0 },
            rotation: item.rotation || { x: 0, y: 0, z: 0 },
            scale: item.scale || { x: 1, y: 1, z: 1 },
          };
        });
      }
      this.setData({
        sceneList,
      });
    },
  },
});