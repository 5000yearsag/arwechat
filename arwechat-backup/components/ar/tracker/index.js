const appInstance = getApp();

Component({
  data: {
    assetsLoaded: false,
    sceneList: [],
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
      const { assetId, assetType } = dataset || {};
      if (assetId && assetType === "video") {
        const video = this.scene.assets.getAsset("video-texture", assetId);
        active ? video.play() : video.stop();
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