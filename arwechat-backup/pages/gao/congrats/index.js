const appInstance = getApp();

Page({
  data: {
    avatarUrl: "",
    nickname: "",
    congratulation: "",
    showAuthDialog: false,
    congratulationList: [
      {
        id: 1,
        avatar: "/assets/gao/avatar1.png",
        message: "多赚银子请喝酒",
        color: "#0256FF",
      },
      {
        id: 2,
        avatar: "/assets/gao/avatar2.png",
        message: "好好开店，不要恋爱",
        color: "#182126",
        offset: 200,
      },
      {
        id: 3,
        avatar: "/assets/gao/avatar3.png",
        message: "切勿殴打顾客",
        color: "#00E88A",
      },
    ],
  },
  onLoad() {
    wx.getStorage({
      key: "nickname",
      success: (res) => {
        const { data } = res || {};
        this.setData({
          nickname: data ?? "",
        });
      },
    });
    wx.getStorage({
      key: "avatarUrl",
      success: (res) => {
        const { data } = res || {};
        this.setData({
          avatarUrl: data ?? "",
        });
      },
    });
  },
  openAuthDialog() {
    this.setData({
      showAuthDialog: true,
    });
  },
  closeAuthDialog() {
    this.setData({
      showAuthDialog: false,
    });
  },
  onConfirm() {
    if (!this.data.avatarUrl) {
      wx.showToast({
        title: "请授权头像",
        icon: "error",
        duration: 2000,
      });
      return;
    }
    wx.setStorage({
      key: "avatarUrl",
      data: this.data.avatarUrl,
    });
    if (!this.data.nickname) {
      wx.showToast({
        title: "请填写昵称",
        icon: "error",
        duration: 2000,
      });
      return;
    }
    wx.setStorage({
      key: "nickname",
      data: this.data.nickname,
      success: () => {
        this.closeAuthDialog();
      },
    });
    this.gotoScanPage();
  },
  gotoScanPage() {
    if (!this.data.avatarUrl || !this.data.nickname) {
      this.openAuthDialog();
      return;
    }
    if (!this.data.congratulation) {
      wx.showToast({
        title: "请填写祝词",
        icon: "error",
        duration: 2000,
      });
      return;
    }
    if (appInstance && appInstance.globalData) {
      appInstance.globalData.congratulation = this.data.congratulation;
    }
    wx.navigateTo({
      url: "/pages/gao/scan/index",
    });
  },
  onChooseAvatar(e) {
    const { avatarUrl } = e.detail;
    this.setData({
      avatarUrl,
    });
  },
});
