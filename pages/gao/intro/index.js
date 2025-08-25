const useQuery = require("../../../hooks/useQuery");

// 直接定义解析函数而不是导入
const parseArResourceDimension = (arResourceDimension, scale) => {
  let xScale = 1,
    yScale = 1;
  if (typeof arResourceDimension === "string" && arResourceDimension) {
    const [xDimension, yDimension] = arResourceDimension.split("*");
    if (Number(xDimension) && Number(yDimension)) {
      yScale = yDimension / xDimension;
    }
  }
  return {
    xScale: xScale * scale.x,
    yScale: yScale * scale.y,
  };
};

const parseArResourceType = (arResourceUrl) => {
  if (!arResourceUrl || typeof arResourceUrl !== "string") {
    return "video";
  }
  
  const url = arResourceUrl.toLowerCase();
  
  // 检查3D模型文件扩展名
  if (url.endsWith('.glb') || url.endsWith('.gltf')) {
    return "model";
  }
  
  // 检查视频文件扩展名
  if (url.endsWith('.mp4') || url.endsWith('.mov') || url.endsWith('.avi') || url.endsWith('.webm')) {
    return "video";
  }
  
  // 默认为视频
  return "video";
};

const parseArResourceSpaceParam = (spaceParam) => {
  const position = { x: 0, y: 0, z: 0 };
  const rotation = { x: 0, y: 0, z: 0 };
  const scale = { x: 1, y: 1, z: 1 };
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
  return { position, rotation, scale };
};

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
    debugClickCount: 0, // 调试模式点击计数
  },
  onLoad(query) {
    // 首先设置globalData中的collectionUuid
    const defaultCollectionUuid = this.data.collectionUuid;
    appInstance.globalData.collectionUuid = defaultCollectionUuid;
    
    // 埋点统计 - 页面访问
    wx.request({
      url: `${appInstance.globalData.domainWithProtocol}${appInstance.globalData.statisticApi}?collectionUuid=${appInstance.globalData.collectionUuid}&type=pvCount`,
      method: "GET",
      header: { "content-type": "application/json" },
      success: (res) => {}
    })

    // 如果没有url参数，构造默认的API URL
    if (!query.url) {
      query.url = encodeURIComponent(`${appInstance.globalData.domainWithProtocol}/api/guest/getAllSceneByCollection?collectionUuid=${defaultCollectionUuid}`);
      console.log('构造默认API URL:', query.url);
    }

    useQuery(query, {
      onSuccess: (data) => {
        console.log('API成功响应，原始数据:', data);
        const { sceneInfo, sceneList } = data || {};
        console.log('场景信息:', sceneInfo);
        console.log('场景列表:', sceneList);
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
          // 设置shareImgUrl，避免截屏功能出错
          appInstance.globalData.shareImgUrl = sceneInfo?.shareImgUrl || "";
          const _sceneInfoList = (sceneList || [])
            .filter((item) => !!item.sceneImgUrl && !!item.arResourceUrl)
            .map((item) => {
              try {
                const { position, rotation, scale } = parseArResourceSpaceParam(
                  item.spaceParam
                );
                const { xScale, yScale } = parseArResourceDimension(
                  item.arResourceDimension,
                  scale
                );
                const arResourceType = item.arResourceType || parseArResourceType(item.arResourceUrl);
                
                // 处理多资源数据
                let extraJson = [];
                try {
                  extraJson = item.extraJson ? JSON.parse(item.extraJson) : [];
                  console.log(`场景 ${item.sceneUuid} 解析extraJson:`, extraJson);
                } catch (e) {
                  console.error(`场景 ${item.sceneUuid} extraJson解析失败:`, item.extraJson, e);
                }
                
                const extraResources = extraJson.map(i => {
                  const { position, rotation, scale } = parseArResourceSpaceParam(i.spaceParam);
                  const { xScale, yScale } = parseArResourceDimension(i.arResourceDimension, scale);
                  const extraType = i.arResourceType || parseArResourceType(i.arResourceUrl);
                  return {
                    sceneUuid: i.sceneUuid || Math.floor(Math.random() * 1000000000000) + 1,
                    sceneImgUrl: item.sceneImgUrl, // 共享同一个识别图
                    arResourceUrl: i.arResourceUrl,
                    type: extraType,
                    videoEffect: i.videoEffect,
                    audioResourceUrl: i.audioResourceUrl,
                    xScale,
                    yScale,
                    position,
                    rotation,
                    scale
                  };
                });
                
                console.log(`场景 ${item.sceneUuid} extraResources:`, extraResources);
                
                // 主资源
                const mainResource = {
                  sceneUuid: item.sceneUuid,
                  sceneImgUrl: item.sceneImgUrl,
                  arResourceUrl: item.arResourceUrl,
                  audioResourceUrl: item.audioResourceUrl,
                  videoEffect: item.videoEffect,
                  type: arResourceType,
                  xScale,
                  yScale,
                  position,
                  rotation,
                  scale
                };

                // 构建该识别图的所有资源数组（主资源 + 多资源）
                const allResources = [mainResource, ...extraResources];
                
                console.log(`场景 ${item.sceneUuid} 最终资源数组:`, allResources);
                
                return allResources;
              } catch (e) {
                console.error('处理场景数据出错:', item, e);
                return null;
              }
            })
            .filter(item => item !== null);
          appInstance.globalData.sceneList = _sceneInfoList;
          console.log('处理后的场景数据数量:', _sceneInfoList.length);
          console.log('数据类型检查:');
          console.log('_sceneInfoList是数组吗?', Array.isArray(_sceneInfoList));
          if (_sceneInfoList.length > 0) {
            console.log('第一个元素是数组吗?', Array.isArray(_sceneInfoList[0]));
            console.log('第一个元素:', _sceneInfoList[0]);
          }
          console.log('完整的globalData.sceneList:', appInstance.globalData.sceneList);
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
  // 隐藏调试模式：连续点击banner图片10次进入调试页面
  onBannerTap() {
    const newCount = this.data.debugClickCount + 1;
    this.setData({
      debugClickCount: newCount
    });
    
    if (newCount >= 10) {
      // 重置计数器并进入调试页面
      this.setData({
        debugClickCount: 0
      });
      wx.navigateTo({
        url: '/pages/debug/collections/index'
      });
    } else {
      // 3秒内没有继续点击则重置计数器
      setTimeout(() => {
        this.setData({
          debugClickCount: 0
        });
      }, 3000);
    }
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