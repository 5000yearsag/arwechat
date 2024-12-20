// index.js
const appInstance = getApp();

const parseArResourceDimension = (arResourceDimension) => {
  let xScale = 1,
    yScale = 1;
  if (typeof arResourceDimension === "string" && arResourceDimension) {
    const [xDimension, yDimension] = arResourceDimension.split("*");
    if (Number(xDimension) && Number(yDimension)) {
      yScale = yDimension / xDimension;
    }
  }
  return {
    xScale,
    yScale,
  };
};

const parseArResourceSpaceParam = (spaceParam) => {
  const position = {
    x: 0,
    y: 0,
    z: 0,
  };
  const rotation = {
    x: 0,
    y: 0,
    z: 0,
  };
  const scale = {
    x: 1,
    y: 1,
    z: 1,
  };
  if (typeof spaceParam === "string" && spaceParam) {
    try {
      const spaceParamJson = JSON.parse(spaceParam);

      position.x = spaceParamJson?.position?.x || 0;
      position.y = spaceParamJson?.position?.y || 0;
      position.z = spaceParamJson?.position?.z || 0;

      rotation.x = spaceParamJson?.rotation?.x || 0;
      rotation.y = spaceParamJson?.rotation?.y || 0;
      rotation.z = spaceParamJson?.rotation?.z || 0;

      scale.x = spaceParamJson?.scale?.x || 1;
      scale.y = spaceParamJson?.scale?.y || 1;
      scale.z = spaceParamJson?.scale?.z || 1;
    } catch (e) {}
  }
  return {
    position,
    rotation,
    scale,
  };
};
Page({
  data: {
    pageLoading: true,
    scanEntryVisible: false,
    errorMessage: "",
    logoImgUrl:"",
    bgImgUrl: "",
    brandName: "",
    collectionName: "",
    description:
      "探索传统与现代的交融，我们的文创项目致力于将经典文化元素融入日常生活用品，如手工艺品、特色文具、时尚服饰等。每件作品都是对传统文化的一次创新诠释，旨在激发人们对本土文化的兴趣和自豪感，同时提供独特的美学体验。"
  },
  gotoArScanPage() {
    if (!this.data.scanEntryVisible) {
      wx.showToast({
        title: '请关闭小程序后，用微信扫描产品二维码进行体验！',
        icon: 'none',
        duration: 2000
      })
      return;
    };
    wx.navigateTo({
      url: `/pages/preview/scan`,
    });
  },
  showScanEntry() {
    let scanEntryVisible = false;
    if (
      appInstance &&
      appInstance.globalData &&
      Array.isArray(appInstance.globalData.sceneList) &&
      appInstance.globalData.sceneList.length > 0
    ) {
      scanEntryVisible = true;
    }
    this.setData({
      scanEntryVisible,
    });
  },
  onLoad(query) {
    this.setData({ errorMessage: "" });
    let { url } = query || {};
    if (url && appInstance && appInstance.globalData) {
      url = decodeURIComponent(url);
      appInstance.globalData.sceneList = [];
      wx.request({
        url,
        method: "GET",
        header: {
          "content-type": "application/json", // 默认值
        },
        success: (res) => {
          const { returnCode, returnDesc, data } = res.data || {};
          if (returnCode === 17000) {
            const { sceneInfo, sceneList } = data || {};
            const { bgImgUrl, coverImgUrl, collectionName, brandName, description } =
              sceneInfo || {};
            const _bgImgUrl = bgImgUrl || "/assets/yao/intro-bg.png";
            const _collectionName = collectionName || "";
            const _brandName = brandName || "";
            const _logoImgUrl = coverImgUrl || "/assets/logo.png";
            const _description =
              description ||
              "探索传统与现代的交融，我们的文创项目致力于将经典文化元素融入日常生活用品，如手工艺品、特色文具、时尚服饰等。每件作品都是对传统文化的一次创新诠释，旨在激发人们对本土文化的兴趣和自豪感，同时提供独特的美学体验。";
            const _sceneInfoList = (sceneList || [])
              .filter((item) => !!item.sceneImgUrl && !!item.arResourceUrl)
              .map((item) => {
                const { xScale, yScale } = parseArResourceDimension(
                  item.arResourceDimension
                );
                const { position, rotation, scale } = parseArResourceSpaceParam(
                  item.spaceParam
                );
                /*
                let arResourceType = "video";
                if (item.arResourceUrl.slice(-4) === ".glb") {
                  arResourceType = "model";
                }
                */
                const arResourceType = item.arResourceType || "video";
                
                return {
                  sceneUuid: item.sceneUuid,
                  sceneImgUrl: item.sceneImgUrl,
                  arResourceUrl: item.arResourceUrl,
                  type: arResourceType,
                  videoEffect: item.videoEffect,
                  audioResourceUrl: item.audioResourceUrl,
                  xScale,
                  yScale,
                  position,
                  rotation,
                  scale,
                };
              });

            appInstance.globalData.sceneList = _sceneInfoList;

            this.setData({
              pageLoading: false,
              errorMessage: "",
              bgImgUrl: _bgImgUrl,
              logoImgUrl: _logoImgUrl,
              brandName: _brandName,
              collectionName: _collectionName,
              description: _description,
              scanEntryVisible: true,
            });
          } else {
            this.setData({
              errorMessage: returnDesc || "请求数据发生错误",
            });
          }
        },
        fail: () => {
          this.setData({ pageLoading: false });
        },
      });
    } else {
      this.setData({ pageLoading: false });
      this.showScanEntry();
    }
  }
});
