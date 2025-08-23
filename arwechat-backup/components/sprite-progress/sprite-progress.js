// components/sprite-progress/sprite-progress.js
Component({

  /**
   * 组件的属性列表
   */
  properties: {

  },
  lifetimes: {
    attached() {
      this.repeat()
    }
  },

  /**
   * 组件的初始数据
   */
  data: {
    progress: 0,
    showProgress: true,
  },

  /**
   * 组件的方法列表
   */
  methods: {
    repeat() {
      if (this.data.progress < 80) {
        this.setData({
          progress: this.data.progress + 1,
        })
        setTimeout(() => {
          this.repeat()
        }, 30);
      }
    },
    dismiss(callback) {
      if (this.data.showProgress) {
        this.setData({
          progress: 100,
        })
        setTimeout(() => {
          this.setData({
            showProgress: false,
          })
          callback && callback()
        }, 300)
      }
    },
  },
})