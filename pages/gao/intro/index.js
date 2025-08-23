const useQuery = require("../../../hooks/useQuery");
const {
  parseArResourceDimension,
  parseArResourceSpaceParam,
} = require("../../../utils/index");

const appInstance = getApp();

Page({
  data: {
    collectionUuid: "240809010829880",
    collectionName: "G.A.O",
    description:
      "探索传统与现代的交融，我们的文创项目致力于将经典文化元素融入日常生活用品，如手工艺品、特色文具、时尚服饰等。每件作品都是对传统文化的一次创新诠释，旨在激发人们对本土文化的兴趣和自豪感，同时提供独特的美学体验。",
    coverImgUrl: "/assets/gao/banner.png",
    url: "/pages/gao/congrats/index",
    errorMessage: "",
  },
  onLoad(query) {
    // 埋点统计 - 页面访问
    wx.request({
      url: `${appInstance.globalData.domainWithProtocol}${appInstance.globalData.statisticApi}?collectionUuid=${appInstance.globalData.collectionUuid}&type=pvCount`,
      method: "GET",
      header: { "content-type": "application/json" },
      success: (res) => {}
    })

    useQuery(query, {
      onSuccess: (data) => {
        const { sceneInfo, sceneList } = data || {};
        const { collectionUuid,collectionName, description, coverImgUrl: originalCoverImgUrl } = sceneInfo || {};
        
        let coverImgUrl = originalCoverImgUrl;
        if(collectionUuid == "240816163456546"){
          coverImgUrl = "/assets/gao/banner.png"
        }  
        // 设置合集名称和描述
        this.setData({
          collectionUuid,
          collectionName,
          description,
          coverImgUrl,

          errorMessage: "",
        });

        // 全局数据globalData缓存场景列表
        if (appInstance && appInstance.globalData) {
          appInstance.globalData.sceneList = [];
          const _sceneInfoList = (sceneList || [])
            .filter((item) => !!item.sceneImgUrl && !!item.arResourceUrl)
            .map((item) => {
              const { xScale, yScale } = parseArResourceDimension(
                item.arResourceDimension
              );
              const { position, rotation, scale } = parseArResourceSpaceParam(
                item.spaceParam
              );
              const arResourceType = item.arResourceType || "video";
              return {
                sceneUuid: item.sceneUuid,
                sceneImgUrl: item.sceneImgUrl,
                arResourceUrl: item.arResourceUrl,
                videoEffect: item.videoEffect,
                type: arResourceType,
                xScale,
                yScale,
                position,
                rotation,
                scale,
              };
            });
          appInstance.globalData.sceneList = _sceneInfoList;
        }
      },
      onFail: (errInfo) => {
        const { errMsg } = errInfo || {};
        this.setData({
          errorMessage: errMsg,
        });
      },
    });
  },
  gotoCongratsPage() {
    // 埋点统计 - 进入扫描
    wx.request({
      url: `${appInstance.globalData.domainWithProtocol}${appInstance.globalData.statisticApi}?collectionUuid=${appInstance.globalData.collectionUuid}&type=click1Count`,
      method: "GET",
      header: { "content-type": "application/json" },
      success: (res) => {}
    })

    let url = "/pages/gao/preview/index";
    if (this.data.collectionUuid == '240816163456546') {
      url = "/pages/gao/congrats/index";
    }
    console.log(this.data.collectionUuid)
    wx.navigateTo({
      url: url,
    });
  },
});
