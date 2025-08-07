const STATE = {
  NONE: -1,
  MOVE: 0,
  ZOOM_OR_PAN: 1
}
const appInstance = getApp();
let innerAudioContext = null
let innerAudioUrl = null

Component({
  behaviors: [require('../common/share-behavior').default],
  data: {
    loaded: false,
    arReady: false,
    planeData: {}
  },
  lifetimes: {
    async attached() {
      console.log('data', this.data)
    }
  },
  detached () {
    if (innerAudioContext) {
      innerAudioContext.stop() // 停止
    }
  },
  methods: {
    handleReady({detail}) {
      this.handlePlaneData()
      const xrScene = this.scene = detail.value;
      this.mat = new (wx.getXrFrameSystem().Matrix4)();
      this.triggerEvent("arTrackerReady", detail);
      
      const { width, height } = this.scene
      // 旋转缩放相关配置
      this.radius = (width + height) / 4
      this.rotateSpeed = 5

      this.handleTouchStart = (event) => {
        this.mouseInfo = { startX: 0, startY: 0, isDown: false, startPointerDistance: 0, state: STATE.NONE }
        this.mouseInfo.isDown = true

        const touch0 = event.touches[0]
        const touch1 = event.touches[1]

        if (event.touches.length === 1) {
          this.mouseInfo.startX = touch0.pageX
          this.mouseInfo.startY = touch0.pageY
          this.mouseInfo.state = STATE.MOVE
        } else if (event.touches.length === 2) {
          const dx = (touch0.pageX - touch1.pageX)
          const dy = (touch0.pageY - touch1.pageY)
          this.mouseInfo.startPointerDistance = Math.sqrt(dx * dx + dy * dy)
          this.mouseInfo.startX = (touch0.pageX + touch1.pageX) / 2
          this.mouseInfo.startY = (touch0.pageY + touch1.pageY) / 2
          this.mouseInfo.state = STATE.ZOOM_OR_PAN
        }

        this.scene.event.add('touchmove', this.handleTouchMove.bind(this))
        this.scene.event.addOnce('touchend', this.handleTouchEnd.bind(this))

      },
      this.handleTouchMove = (event) => {
        const mouseInfo = this.mouseInfo
        if (!mouseInfo.isDown) {
          return
        }

        switch (mouseInfo.state) {
        case STATE.MOVE:
          if (event.touches.length === 1) {
            this.handleRotate(event)
          } else if (event.touches.length === 2) {
            // 支持单指变双指，兼容双指操作但是两根手指触屏时间不一致的情况
            this.scene.event.remove('touchmove', this.handleTouchMove)
            this.scene.event.remove('touchend', this.handleTouchEnd)
            this.handleTouchStart(event)
          }
          break
        case STATE.ZOOM_OR_PAN:
          if (event.touches.length === 1) {
            // 感觉双指松掉一指的行为还是不要自动切换成旋转了，实际操作有点奇怪
          }
          else if (event.touches.length === 2) {
            this.handleZoomOrPan(event)
          }
          break
        default:
          break
        }
      }

      this.handleTouchEnd = (event) => {
        this.mouseInfo.isDown = false
        this.mouseInfo.state = STATE.NONE

        this.scene.event.remove('touchmove', this.handleTouchMove)
        this.scene.event.addOnce('touchstart', this.handleTouchStart)
      }

      this.handleRotate = (event) => {
        const x = event.touches[0].pageX
        const y = event.touches[0].pageY

        const { startX, startY } = this.mouseInfo

        const theta = (x - startX) / this.radius * - this.rotateSpeed
        const phi = (y - startY) / this.radius * - this.rotateSpeed
        if (Math.abs(theta) < .01 && Math.abs(phi) < .01) {
          return
        }
        this.gltfItemTRS.rotation.x -= phi
        this.gltfItemSubTRS.rotation.y -= theta
        this.mouseInfo.startX = x
        this.mouseInfo.startY = y
      }

      this.handleZoomOrPan = (event) => {
        const touch0 = event.touches[0]
        const touch1 = event.touches[1]

        const dx = (touch0.pageX - touch1.pageX)
        const dy = (touch0.pageY - touch1.pageY)
        const distance = Math.sqrt(dx * dx + dy * dy)

        let deltaScale = distance - this.mouseInfo.startPointerDistance
        this.mouseInfo.startPointerDistance = distance
        this.mouseInfo.startX = (touch0.pageX + touch1.pageX) / 2
        this.mouseInfo.startY = (touch0.pageY + touch1.pageY) / 2
        if (deltaScale < -2) {
          deltaScale = -2
        } else if (deltaScale > 2) {
          deltaScale = 2
        }

        const s = deltaScale * 0.02 + 1
        // 缩小
        this.gltfItemTRS.scale.x *= s
        this.gltfItemTRS.scale.y *= s
        this.gltfItemTRS.scale.z *= s
      }
    },
    handleAssetsProgress: function({detail}) {
      console.log('assets progress', detail.value);
    },
    handleAssetsLoaded: function({detail}) {
      console.log('assets loaded', detail.value);
      wx.showToast({title: '点击屏幕放置', duration: 2000});
      // this.setData({loaded: true});
      this.placedFlag = false;
      this.scene.event.addOnce('touchstart', this.placeNode.bind(this));
    },
    handleARReady: function({detail}) {
      console.log('arReady', this.scene.ar.arVersion);
    },
    placeNode(event) {
      if (this.placedFlag) {
        return;
      }
      const xrFrameSystem = wx.getXrFrameSystem()
      this.placedFlag = true;
      this.scene.ar.placeHere('setitem', true)
      const anchorTRS = this.scene.getElementById('anchor').getComponent(xrFrameSystem.Transform)
      anchorTRS.setData({ visible: false })
      wx.setKeepScreenOn({ keepScreenOn: true })


      // 获取改动元素
      this.gltfItemTRS = this.scene.getElementById('preview-model').getComponent(xrFrameSystem.Transform)
      this.gltfItemSubTRS = this.scene.getElementById('preview-model-sub').getComponent(xrFrameSystem.Transform)

      // 开启旋转缩放逻辑
      this.scene.event.addOnce('touchstart', this.handleTouchStart)
    },
    resetModel() {
      const xrFrameSystem = wx.getXrFrameSystem()
      this.placedFlag = false;
      this.scene.getNodeById('setitem').visible = false;
      this.scene.ar.resetPlane();
      const anchorTRS = this.scene.getElementById('anchor').getComponent(xrFrameSystem.Transform)
      anchorTRS.setData({ visible: true })
      this.scene.event.addOnce('touchstart', this.placeNode.bind(this));
    },
    handleShare: function() {
      this.scene.share.captureToFriends();
    },
    handleTrackerSwitch: function ({ detail, target }) {
      const active = detail.value;
      const { dataset } = target || {};
      const { assetId, assetType, assetMp3 } = dataset || {};
      if (assetId && assetType === "model" && assetMp3) {
        if (!innerAudioContext) {
          innerAudioContext = wx.createInnerAudioContext({
            useWebAudioImplement: true // 是否使用 WebAudio 作为底层音频驱动，默认关闭。对于短音频、播放频繁的音频建议开启此选项，开启后将获得更优的性能表现。由于开启此选项后也会带来一定的内存增长，因此对于长音频建议关闭此选项
          })
          innerAudioContext.src = assetMp3
          innerAudioContext.loop = true
        }
        if (innerAudioUrl && innerAudioUrl !== assetMp3) {
          innerAudioContext.stop() // 停止
          innerAudioContext.destroy() // 释放音频资源
          innerAudioContext = wx.createInnerAudioContext({
            useWebAudioImplement: true // 是否使用 WebAudio 作为底层音频驱动，默认关闭。对于短音频、播放频繁的音频建议开启此选项，开启后将获得更优的性能表现。由于开启此选项后也会带来一定的内存增长，因此对于长音频建议关闭此选项
          })
          innerAudioContext.src = assetMp3
        }
        active ? innerAudioContext.play() : innerAudioContext.pause()
        innerAudioUrl = assetMp3
        innerAudioContext.loop = true
      }
    },
    filterGlbResources(list) {
      // 处理嵌套数组结构
      if (!Array.isArray(list)) return null;
      
      // 先尝试在第一层查找
      const directModel = list.find(item => item?.type === 'model');
      if (directModel) return directModel;
      
      // 如果第一层没找到，尝试在嵌套数组中查找
      for (let i = 0; i < list.length; i++) {
        const subList = list[i];
        if (Array.isArray(subList)) {
          const model = subList.find(item => item?.type === 'model');
          if (model) return model;
        }
      }
      
      return null;
    },
    handlePlaneData: function () {
      let planeData = {}
      if (
        appInstance &&
        appInstance.globalData &&
        appInstance.globalData.sceneList
      ) {
        planeData = this.filterGlbResources(appInstance.globalData.sceneList);
      }
      this.setData({
        planeData: planeData
      });
    },
  }
})