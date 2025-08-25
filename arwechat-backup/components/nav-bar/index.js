Component({
  properties: {
    back: {
      type: Boolean,
      value: true,
    },
    text: {
      type: String,
      value: "",
    },
    dividerColor: {
      type: String,
      value: "#979797",
    },
  },
  data: {
    statusBarHeight: 0,
  },
  methods: {
    goBack() {
      if (this.properties.back) {
        wx.navigateBack();
      }
    },
  },
  lifetimes: {
    ready() {
      const windowInfo = wx.getWindowInfo();
      const { statusBarHeight } = windowInfo || {};
      this.setData({
        statusBarHeight: statusBarHeight || 0,
      });
    },
  },
});
