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
      const { assetIndex } = dataset || {};
      const arr = this.data.sceneList[assetIndex]
      
      // 分段加载模式下，识别成功后显示加载提示
      if (active && this.data.loadType === 1) {
        wx.showLoading({
          title: '资源加载中...',
          mask: true
        });
        
        // 检查defer资源是否已加载完成
        let allResourcesLoaded = true;
        
        if (arr && Array.isArray(arr)) {
          arr.forEach(item => {
            if (item.type === 'video' && item.sceneUuid) {
              const video = this.scene.assets.getAsset("video-texture", `video_${item.sceneUuid}`);
              if (!video || !video.loaded) {
                allResourcesLoaded = false;
              }
            }
          });
        }
        
        // 如果资源未完全加载，等待加载完成
        if (!allResourcesLoaded) {
          const checkResourcesInterval = setInterval(() => {
            let nowAllLoaded = true;
            
            if (arr && Array.isArray(arr)) {
              arr.forEach(item => {
                if (item.type === 'video' && item.sceneUuid) {
                  const video = this.scene.assets.getAsset("video-texture", `video_${item.sceneUuid}`);
                  if (!video || !video.loaded) {
                    nowAllLoaded = false;
                  }
                }
              });
            }
            
            if (nowAllLoaded) {
              clearInterval(checkResourcesInterval);
              wx.hideLoading();
              this.playResources(arr, assetIndex, active);
            }
          }, 100);
          
          // 设置超时，防止无限等待
          setTimeout(() => {
            clearInterval(checkResourcesInterval);
            wx.hideLoading();
            this.playResources(arr, assetIndex, active);
          }, 10000);
          
          return;
        } else {
          wx.hideLoading();
        }
      }
      
      this.playResources(arr, assetIndex, active);
    },
    
    playResources: function(arr, assetIndex, active) {
      if (arr && Array.isArray(arr)) {
        arr.forEach(item => {
          // video
          if (item.type === 'video' && item.sceneUuid) {
            const video = this.scene.assets.getAsset("video-texture", `video_${item.sceneUuid}`);
            if (video) {
              active ? video.play() : video.stop();
            }
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
        });
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
        // 直接使用已经处理好的数据
        sceneList = appInstance.globalData.sceneList;
      }
      
      // 更新loadType（从主分支移植的逻辑）
      const loadType = appInstance.globalData.loadType || 0;
      const assetsLoaded = loadType === 1 ? true : this.data.assetsLoaded;
      
      this.setData({
        loadType,
        sceneList,
        // 分段加载模式下，defer资源不需要等待，直接显示AR场景
        assetsLoaded,
      });
    },
  },
});