const appInstance = getApp();

Page({
  data: {
    collections: [],
    loading: true,
    loadingMore: false,
    errorMessage: "",
    hasMore: true,
    currentPage: 1,
    pageSize: 20,
  },
  
  onLoad() {
    this.loadAllCollections();
  },
  
  async loadAllCollections(isLoadMore = false) {
    if (!isLoadMore) {
      this.setData({
        loading: true,
        currentPage: 1,
        collections: [],
        hasMore: true
      });
    } else {
      this.setData({ loadingMore: true });
    }

    try {
      const currentPage = isLoadMore ? this.data.currentPage + 1 : 1;
      const result = await this.fetchCollectionPage(currentPage);
      
      const newCollections = isLoadMore 
        ? [...this.data.collections, ...result.collections]
        : result.collections;

      this.setData({
        collections: newCollections,
        loading: false,
        loadingMore: false,
        currentPage: currentPage,
        hasMore: result.hasMore,
        errorMessage: "",
      });

    } catch (error) {
      console.error('获取合集列表失败:', error);
      this.setData({
        loading: false,
        loadingMore: false,
        errorMessage: "获取合集列表失败",
      });
    }
  },

  
  async fetchCollectionPage(page) {
    // 只使用真实数据，不使用模拟数据
    const result = await this.requestCollectionsFromAPI(page);
    return {
      collections: result.collections,
      hasMore: result.hasMore
    };
  },

  async requestCollectionsFromAPI(page) {
    return new Promise((resolve, reject) => {
      // 使用小程序专用的安全接口
      const requestData = {
        pageNum: page,
        pageSize: this.data.pageSize,
        appId: "wx360d6d845e60562e", // 小程序AppID验证
        timestamp: Date.now() // 时间戳防重放攻击
      };
      
      console.log('请求合集数据:', requestData);
      
      wx.request({
        url: `${appInstance.globalData.domainWithProtocol}/api/guest/getAllCollections`,
        method: "POST",
        header: { "content-type": "application/json" },
        data: requestData,
        success: (res) => {
          console.log('安全接口响应:', res.data);
          if (res.data?.returnCode === 17000 && res.data?.data) {
            const list = res.data.data.list;
            if (Array.isArray(list)) {
              const collections = list.map(item => ({
                uuid: item.collectionUuid,
                collectionName: item.collectionName,
                coverImgUrl: item.coverImgUrl || "/assets/logo.png",
                sceneCount: item.sceneCount || 0
              }));
              console.log('获取到真实合集数据:', collections);
              resolve({
                collections: collections,
                hasMore: res.data.data.hasNextPage || false
              });
            } else {
              console.error('返回的list不是数组:', list);
              reject(new Error('返回数据格式错误'));
            }
          } else {
            console.error('接口返回错误:', res.data);
            reject(new Error(`获取合集列表失败: ${res.data?.returnDesc || '接口错误'}`));
          }
        },
        fail: (error) => {
          console.error('网络请求失败:', error);
          reject(new Error(`网络请求失败: ${error.errMsg || '网络异常'}`));
        }
      });
    });
  },
  
  getCollectionByUuid(uuid) {
    return new Promise((resolve, reject) => {
      wx.request({
        url: `${appInstance.globalData.domainWithProtocol}/api/guest/getAllSceneByCollection?collectionUuid=${uuid}`,
        method: "GET",
        header: { "content-type": "application/json" },
        success: (res) => {
          if (res.data && res.data.returnCode === 17000 && res.data.data?.sceneInfo) {
            const sceneInfo = res.data.data.sceneInfo;
            resolve({
              uuid: uuid,
              collectionName: sceneInfo.collectionName,
              description: sceneInfo.description,
              coverImgUrl: sceneInfo.coverImgUrl
            });
          } else {
            reject(new Error('获取合集信息失败'));
          }
        },
        fail: (error) => {
          reject(error);
        }
      });
    });
  },
  
  onCollectionTap(e) {
    const { uuid, name } = e.currentTarget.dataset;
    if (uuid) {
      // 设置全局collectionUuid
      appInstance.globalData.collectionUuid = uuid;
      
      // 清除现有的场景数据，强制重新加载
      if (appInstance.globalData.sceneList) {
        appInstance.globalData.sceneList = [];
      }

      // 显示加载提示
      wx.showLoading({
        title: `加载${name}...`,
        mask: true
      });

      // 构造API URL，让原有的useQuery逻辑正常工作
      const apiUrl = `${appInstance.globalData.domainWithProtocol}/api/guest/getAllSceneByCollection?collectionUuid=${uuid}`;
      const encodedUrl = encodeURIComponent(apiUrl);
      
      // 跳转到合集页面，传递url参数让useQuery处理
      wx.navigateTo({
        url: `/pages/gao/intro/index?url=${encodedUrl}&from=debug`,
        success: () => {
          // 延迟关闭loading，确保页面有时间加载
          setTimeout(() => {
            wx.hideLoading();
          }, 1000);
        },
        fail: () => {
          wx.hideLoading();
          wx.showToast({
            title: '跳转失败',
            icon: 'error'
          });
        }
      });
    }
  },
  
  onRefresh() {
    this.loadAllCollections(false);
  },

  onLoadMore() {
    if (!this.data.hasMore || this.data.loadingMore) {
      return;
    }
    this.loadAllCollections(true);
  }
});