---
title: macOS Default Setting
date: 2019-02-12 14:49:04
tags: [速记,macOS]
---

### 0x80 前言

该篇文章主要用于记录macOS下defaults操作的相关指令。

### 0x81 命令

``` Bash
# 通用参数
-g/-globalDomain
-currentHost
-bool YES/NO

# 指针速度读写
defaults read -g com.apple.mouse.scaling
defaults write -g com.apple.mouse.scaling 0.5

# 禁用字体渲染（Mojave）
defaults write -g CGFontRenderingFontSmoothingDisabled -bool NO
defaults read -g CGFontRenderingFontSmoothingDisabled

# 字体渲染级别
defaults write -g AppleFontSmoothing 2
defaults read -g AppleFontSmoothing

# 设置语言首选项
defaults write com.apple.Safari AppleLanguages '(zh-CN)'
defaults read com.apple.Safari
```