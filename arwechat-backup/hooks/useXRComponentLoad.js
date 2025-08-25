// 页面加载xr-frame组件时的默认行为

const useXRComponentLoad = () => {
  const info = wx.getSystemInfoSync();
  const width = info.windowWidth;
  const height = info.windowHeight;
  const dpi = info.pixelRatio;

  return {
    width,
    height,
    renderWidth: width * dpi,
    renderHeight: height * dpi,
  };
};

module.exports = useXRComponentLoad;
