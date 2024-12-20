const saveImageToPhotosAlbumSync = (filePath) => {
  return new Promise((resolve, reject) => {
    if (filePath) {
      wx.saveImageToPhotosAlbum({
        filePath,
        success(res) {
          resolve(res);
        },
        fail(err) {
          reject(err);
        },
      });
    } else {
      reject(new Error("file path empty"));
    }
  });
};

const shareMediaMessageSync = ({
  filePath,
  type = "file",
  fileName,
  thumbPath,
} = {}) => {
  return new Promise((resolve, reject) => {
    if (filePath) {
      if (type === "file") {
        wx.shareFileMessage({
          filePath,
          fileName,
          success(res) {
            resolve(res);
          },
          fail(err) {
            reject(err);
          },
        });
      } else if (type === "video") {
        wx.shareVideoMessage({
          videoPath: filePath,
          thumbPath,
          success(res) {
            resolve(res);
          },
          fail(err) {
            reject(err);
          },
        });
      } else {
        reject(new Error("invalid share file type"));
      }
    } else {
      reject(new Error("file path empty"));
    }
  });
};

module.exports = {
  saveImageToPhotosAlbumSync,
  shareMediaMessageSync,
};
