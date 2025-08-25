const { generateNFCUrlScheme } = require('../utils/urlScheme')

Page({
  data: {
    urlScheme: '',
    loading: false
  },

  async generateScheme() {
    this.setData({ loading: true })
    
    try {
      const urlScheme = await generateNFCUrlScheme()
      this.setData({ 
        urlScheme: urlScheme,
        loading: false 
      })
      
      wx.showToast({
        title: 'URL Scheme generated successfully',
        icon: 'success'
      })
    } catch (error) {
      this.setData({ loading: false })
      wx.showToast({
        title: 'Generation failed',
        icon: 'error'
      })
      console.error('Error generating URL scheme:', error)
    }
  },

  copyToClipboard() {
    if (this.data.urlScheme) {
      wx.setClipboardData({
        data: this.data.urlScheme,
        success: () => {
          wx.showToast({
            title: 'Copied to clipboard',
            icon: 'success'
          })
        }
      })
    }
  }
})
