// 页面加载参数携带请求url，对url做请求并返回解析后的数据

const useQuery = (query, { onSuccess, onFail, options } = {}) => {
  let { url } = query || {};

  if (url) {
    url = decodeURIComponent(url);
    wx.request({
      url,
      method: "GET",
      header: {
        "content-type": "application/json",
      },
      success: (res) => {
        const { returnCode, returnDesc, data } = res?.data || {};
        if (returnCode === 17000) {
          onSuccess?.(data);
        } else {
          onFail?.({ errMsg: returnDesc });
        }
      },
      fail: (errInfo) => {
        onFail?.(errInfo);
      },
      ...(options || {}),
    });
  }
};

module.exports = useQuery;
