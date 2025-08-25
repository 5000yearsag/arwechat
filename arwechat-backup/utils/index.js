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

const parseArResourceDimension = (arResourceDimension) => {
  let xScale = 1,
    yScale = 1;
  if (typeof arResourceDimension === "string" && arResourceDimension) {
    const [xDimension, yDimension] = arResourceDimension.split("*");
    if (Number(xDimension) && Number(yDimension)) {
      yScale = yDimension / xDimension;
    }
  }
  return {
    xScale,
    yScale,
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

module.exports = {
  parseArResourceDimension,
  parseArResourceSpaceParam,
  saveImageToPhotosAlbumSync,
  shareMediaMessageSync,
};
