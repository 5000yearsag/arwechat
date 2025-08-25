const appInstance = getApp();

Component({
  data: {
    assetsLoaded: false,
    sceneList: [],
    congratulation: "",
    catchedAssetId: null,
  },
  methods: {
    handleReady: function ({ detail }) {
      this.scene = detail.value;
      this.handleSceneData();
      this.triggerEvent("ready", detail);
    },

    handleAssetsProgress: function ({ detail }) {
      this.triggerEvent("arAssetsProgress", detail.value);
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
      if (assetId) {
        if (assetType === "video") {
          const video = this.scene.assets.getAsset("video-texture", assetId);
          active ? video.play() : video.stop();
        }

        this.setData({
          catchedAssetId: active ? assetId : null,
        });
      }

      this.triggerEvent("catchAsset", active);
    },

    handleSceneData: function () {
      let sceneList = [];
      let congratulation = "祝:";
      if (appInstance && appInstance.globalData) {
        sceneList = appInstance.globalData.sceneList || [];
        congratulation =
          congratulation + appInstance.globalData.congratulation || "";
      }
      this.setData({
        sceneList,
        congratulation,
      });
    },
  },
  lifetimes: {
    detached() {
      this.scene = null;
      this.setData({
        assetsLoaded: false,
        sceneList: [],
        congratulation: "",
        catchedAssetId: null,
      });
    },
  },
});
