// index.js
const appInstance = getApp();

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
    isInitPage: true, // 是否首次加载页面
    array: [],
    index: 0,
    pageLoading: true,
    originHisroryListz: [],
    scanEntryVisible: false,
    errorMessage: "",
    logoImgUrl:"",
    bgImgUrl: "https://yao-1300735383.cos.ap-beijing.myqcloud.com/test/template/241230/310049_99987.png",
    brandName: "AR · 耀文化",
    collectionName: "",
    bgIsGif: false,
    collectionType: null, // 0:'图像识别'  1:'平面识别'
    description:
      "探索传统与现代的交融，我们的文创项目致力于将经典文化元素融入日常生活用品，如手工艺品、特色文具、时尚服饰等。每件作品都是对传统文化的一次创新诠释，旨在激发人们对本土文化的兴趣和自豪感，同时提供独特的美学体验。"
  },
  gotoArScanPage() {
    // 埋点统计
    wx.request({
      url: `${appInstance.globalData.domainWithProtocol}${appInstance.globalData.statisticApi}?collectionUuid=${appInstance.globalData.collectionUuid}&type=click1Count`,
      method: "GET",
      header: { "content-type": "application/json" },
      success: (res) => {}
    })
    if (!this.data.scanEntryVisible) {
      wx.showToast({
        title: '请关闭小程序后，用微信扫描产品二维码进行体验！',
        icon: 'none',
        duration: 2000
      })
      return;
    };
    // 0:'图像识别'   1:'平面识别',
    if(this.data.collectionType == 1) {
      wx.navigateTo({
        url: `/pages/plane-ar-preview/index`,
      });
    } else {
      wx.navigateTo({
        url: `/pages/preview/scan`,
      });
    }
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
  async onLoad(query) {
    console.log('-query---')
    console.log(query)
    this.setData({ errorMessage: "" });
    let { url } = query || {};
    if (url && appInstance && appInstance.globalData) {
      url = decodeURIComponent(url);
      appInstance.globalData.sceneList = [];
      const { miniProgram } = wx.getAccountInfoSync();

      appInstance.globalData.collectionUuid = this.getUrlParam(url, 'collectionUuid')
      console.log('collectionUuid------')
      console.log(appInstance.globalData.collectionUuid)

      // 从 URL 中获取包含协议的域名的函数
      const getDomainWithProtocol = (url) => {
        // 先提取协议部分
        const protocolMatch = url.match(/^https?:\/\//i);
        const protocol = protocolMatch ? protocolMatch[0] : '';
        
        // 去除协议部分后，截取到第一个斜杠之前的部分
        let domainPart = url.replace(/^https?:\/\//i, '');
        const pathIndex = domainPart.indexOf('/');
        if (pathIndex !== -1) {
          domainPart = domainPart.slice(0, pathIndex);
        }
        // 截取到第一个问号之前的部分
        const queryIndex = domainPart.indexOf('?');
        if (queryIndex !== -1) {
          domainPart = domainPart.slice(0, queryIndex);
        }
        // 截取到第一个井号之前的部分
        const hashIndex = domainPart.indexOf('#');
        if (hashIndex !== -1) {
          domainPart = domainPart.slice(0, hashIndex);
        }
        return protocol + domainPart;
      };
      // appInstance.globalData.domainWithProtocol = getDomainWithProtocol(url)

      // 埋点统计
      wx.request({
        url: `${appInstance.globalData.domainWithProtocol}${appInstance.globalData.statisticApi}?collectionUuid=${appInstance.globalData.collectionUuid}&type=pvCount`,
        method: "GET",
        header: { "content-type": "application/json" },
        success: (res) => {}
      })
      
      this.upPage()

      const openid = await this.getOpenid()
      appInstance.globalData.openid = openid

      await this.setpHistory()

      await this.upHistory()

    } else {
      this.setData({
        pageLoading: false,
        isInitPage: false
      });
      this.showScanEntry();

      const openid = await this.getOpenid()
      appInstance.globalData.openid = openid

      await this.setpHistory()
    }
  },
  onShow () {
    console.log('show-----')
    console.log(this.data.isInitPage)
    // 非首次加载 去更新
    if (!this.data.isInitPage) {
      this.upPage()
    }
  },
  getOpenid () {
    const { miniProgram } = wx.getAccountInfoSync();
    return new Promise((resolve, reject) => {
       wx.login({
        success:({code})=>
          wx.request({
            url: `${appInstance.globalData.domainWithProtocol}${appInstance.globalData.openIdApi}?code=${code}&appId=${miniProgram.appId}`,
            method: "GET",
            header: { "content-type": "application/json" },
            success: (res) => {
              resolve(res.data.data)
            }
          })
      })
    })
  },
  upHistory () {
    return new Promise((resolve, reject) => {
       wx.request({
        url: `${appInstance.globalData.domainWithProtocol}${appInstance.globalData.historyApi}?openId=${appInstance.globalData.openid}`,
        method: "GET",
        header: { "content-type": "application/json" },
        success: (res) => {
          const names = res.data.data.map(item => item.collectionName)
          this.setData({
            array: names,
            originHisroryListz: res.data.data || []
          })
          resolve()
        } 
      })
    })
  },
  setpHistory () {
    const { miniProgram } = wx.getAccountInfoSync();
    return new Promise((resolve, reject) => {
      wx.request({
        url: `${appInstance.globalData.domainWithProtocol}${appInstance.globalData.historyRecordApi}?openId=${appInstance.globalData.openid}&appId=${miniProgram.appId}&collectionUuid=${appInstance.globalData.collectionUuid}`,
        method: "GET",
        header: { "content-type": "application/json" },
        success: (res) => {
          resolve()
        }
      })
    })
  },
  bindPickerChange: function(e) {
    const { collectionUuid } = this.data.originHisroryListz[e.detail.value]
    appInstance.globalData.collectionUuid = collectionUuid
    // this.setData({
    //   index: e.detail.value
    // })
    this.upPage()
  },
  getUrlParam (url, paramName) {
    // 找到 URL 中查询参数部分的起始位置
    const queryIndex = url.indexOf('?');
    if (queryIndex === -1) {
      return null;
    }
    // 提取查询参数部分
    const queryString = url.slice(queryIndex + 1);
    // 将查询参数按 & 分割成键值对数组
    const params = queryString.split('&');

    for (let i = 0; i < params.length; i++) {
      const [key, value] = params[i].split('=');
      if (key === paramName) {
        // 对参数值进行解码
        return decodeURIComponent(value.replace(/\+/g, ' '));
      }
    }
    return null;
  },
  upPage () {
    console.log('up-----')
    wx.request({
      url: `${appInstance.globalData.domainWithProtocol}/api/guest/getAllSceneByCollection?collectionUuid=${appInstance.globalData.collectionUuid}`,
      method: "GET",
      header: {
        "content-type": "application/json", // 默认值
      },
      success: (res) => {
        const { returnCode, returnDesc, data } = res.data || {};
        if (returnCode === 17000) {
          const { sceneInfo, sceneList } = data || {};
          const { bgImgUrl, coverImgUrl, collectionName,  collectionType, brandName, description } =
            sceneInfo || {};
          const _bgImgUrl = bgImgUrl || "https://yao-1300735383.cos.ap-beijing.myqcloud.com/test/template/241230/310049_99987.png";
          const _collectionName = collectionName || "";
          const _brandName = brandName || "";
          const _logoImgUrl = coverImgUrl || "/assets/logo.png";
          const _description =
            description ||
            "探索传统与现代的交融，我们的文创项目致力于将经典文化元素融入日常生活用品，如手工艺品、特色文具、时尚服饰等。每件作品都是对传统文化的一次创新诠释，旨在激发人们对本土文化的兴趣和自豪感，同时提供独特的美学体验。";
          const _sceneInfoList = (sceneList || [])
            .filter((item) => !!item.sceneImgUrl && !!item.arResourceUrl)
            .map((item) => {
              const { position, rotation, scale } = parseArResourceSpaceParam(
                item.spaceParam,
              );
              const { xScale, yScale } = parseArResourceDimension(
                item.arResourceDimension,
                scale
              );
              const arResourceType = item.arResourceType || "video";

              const extraJson = item.extraJson ? JSON.parse(item.extraJson) : []
              const extraArr = []
              extraJson.map(i => {
                const { position, rotation, scale } = parseArResourceSpaceParam(
                  i.spaceParam,
                );
                const { xScale, yScale } = parseArResourceDimension(
                  i.arResourceDimension,
                  scale
                );
                const arResourceType = i.arResourceType || "video";
                extraArr.push({
                  sceneUuid: i.sceneUuid || Math.floor(Math.random() * 1000000000000) + 1,
                  sceneImgUrl: i.sceneImgUrl,
                  arResourceUrl: i.arResourceUrl,
                  type: arResourceType,
                  videoEffect: i.videoEffect,
                  audioResourceUrl: i.audioResourceUrl,
                  xScale,
                  yScale,
                  position,
                  rotation,
                  scale
                })
              })

              return [
                {
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
                  scale
                },
                ...extraArr
              ];
            });
          console.log(_sceneInfoList)
          appInstance.globalData.sceneList = _sceneInfoList;
          appInstance.globalData.shareImgUrl = sceneInfo.shareImgUrl
          appInstance.globalData.loadType = sceneInfo.loadType || 0;

          this.setData({
            isInitPage: false,
            pageLoading: false,
            errorMessage: "",
            bgImgUrl: _bgImgUrl,
            logoImgUrl: _logoImgUrl,
            brandName: _brandName,
            collectionName: _collectionName,
            bgIsGif: _bgImgUrl.toLowerCase().endsWith('.gif'),
            collectionType: collectionType,
            description: _description,
            scanEntryVisible: true,
          });
        } else {
          // this.setData({
          //   errorMessage: returnDesc || "请求数据发生错误",
          // });
        }
      },
      fail: () => {
        this.setData({ pageLoading: false });
      },
    });
  },
  goLsjl () {
    wx.navigateTo({
      url: `/pages/history/index`
    })
  }
});
