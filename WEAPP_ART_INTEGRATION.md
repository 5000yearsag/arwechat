# WeApp Art Integration

This branch (`weapp-art-branch`) contains the integration of ar-weapp-art.zip code with the main arwechat mini-program.

## Changes Made

### 1. Fixed Domain Configuration
- **Fixed**: Changed `domainWithProtocol` from `'https://yaoculture.shenyuantek.com'` to `'https://app.lanyuxr.com'`
- **Location**: `app.js:15`
- **Impact**: This fixes the statistics recording issue where API calls were going to the wrong server

### 2. Added New Pages from ar-weapp-art
- `pages/gao/intro/index` - Introduction page
- `pages/gao/scan/index` - Scan page  
- `pages/gao/preview/index` - Preview page
- `pages/gao/congrats/index` - Congratulations page

### 3. Added New Components from ar-weapp-art
- `components/ar/` - Enhanced AR components
  - `components/ar/base/tracker` - Base AR tracker
  - `components/ar/hud/recorder` - AR HUD recorder
  - `components/ar/tracker` - AR tracker component
- `components/nav-bar/` - Navigation bar component
- `components/page-bg/` - Page background component

### 4. Added New Assets from ar-weapp-art
- `assets/gao/` directory with art-specific images and resources

## Key Features Added
1. **Enhanced AR Experience**: New AR components with better tracking and recording capabilities
2. **Art-focused UI**: New pages and components designed for art exhibitions
3. **Better Navigation**: Enhanced navigation components
4. **Fixed Statistics**: Corrected domain configuration ensures proper statistics recording

## Backup
Original files were backed up to `arwechat-backup/` directory before integration.

## Domain Configuration Fix
The main issue causing statistics not to record was the wrong domain configuration:
- **Before**: `https://yaoculture.shenyuantek.com` (incorrect)
- **After**: `https://app.lanyuxr.com` (correct)

This ensures all API calls (statistics, openId, history) go to the correct server.

## Testing Required
After deployment, test:
1. Statistics recording functionality
2. New gao/* pages functionality  
3. Enhanced AR components
4. Navigation and page background components

## Note
This branch combines the best of both codebases - the working backend integration from the main branch and the enhanced UI/AR components from ar-weapp-art.