import { rpxTopx } from '../../utils/common';

Component({
  properties: {
    type: {
      // 风格 1沉浸式效果 2正常效果
      type: String,
      value: '1',
    },
    title: {
      // 标题
      type: String,
      value: '',
    },
    color: {
      // 标题和返回箭头颜色
      type: String,
      value: '#333',
    },
    backgroundColor: {
      // 背景色
      type: String,
      value: '#fff',
    },
    showBack: {
      // 是否显示返回按钮 默认显示
      type: Boolean,
      value: true,
    },
    scrollTop: {
      type: Number, // 页面滚动距离
      value: 0,
    },
    scrollTopEffect: {
      type: Number, // 页面滚动距离多大才完全展示完动画
      value: 30,
    },
  },
  data: {
    headerHeight: 0,
    headerPaddingTop: 0,
    headerPaddingLeft: 0,
    headerPaddingBottom: 0,
  },
  attached () {
    const sys = wx.getSystemInfoSync();
    const res = wx.getMenuButtonBoundingClientRect();
    this.setData({
      headerHeight: res.height,
      headerPaddingTop: res.top,
      headerPaddingLeft: sys.screenWidth - res.right,
      headerPaddingBottom: rpxTopx(10),
    });
  },
  methods: {
    // 返回上一页
    goBack () {
      wx.navigateBack({
        delta: 1,
      });
    },
  },
});
