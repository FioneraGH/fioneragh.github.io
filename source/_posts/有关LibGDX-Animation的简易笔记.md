---
title: 有关LibGDX Animation的简易笔记
date: 2015-07-03 15:23:14
tags: [libGDX]
---

## [迁移]

### 0x80 简介
libGDX是常用的Android游戏框架， 而Animation类用于将一组图片按照一定的顺序展示，形成动画。

### 0x81 常用Class
```Java
private float statetime; // 状态时间
private Animation walkAnimation; // 动画类
private Texture walkSheet; // 包含图片素材的表格式图片纹理
private TextureRegion [] walkFrames; // 一维数组，用于保存上方Texture经过裁剪后得到的图片集合
private TextureRegion currentFrame; // 当前帧，指代当前时间节点显示的图片
private SpriteBatch batch; // 画笔，用于在render期间绘制图像
```

### 0x82 初始化变量
```Java
（create阶段）：
Texture.setEnforcePotImages(false); // 将纹理的强制2点阵关闭，这样才能使用任意分辨率的图片（libgdx 0.9.9 之前的限制）
walkSheet = new Texture(Gdx.files.internal("data/1.png")); // 读取纹理data/1.png
TextureRegion[][] temp = TextureRegion.split(walkSheet,walkSheet.getWidth() / FRAME_COL, walkSheet.getHeight()/ FRAME_ROW); // 二维数组临时保存2d裁剪的结果
walkFrames[] = temp[][]; // 将临时数组内容传到之前定义的用于展示的一维数组
walkAnimation = new Animation(0.05f, walkFrames); // 使用帧数组创建动画实例，时间间隔为0.05f
walkAnimation.setPlayMode(Animation.LOOP); // 设置动画播放模式为普通循环
batch = new SpriteBatch(); // 构造画笔实例
statetime = 0;
```

### 0x83 GL绘制
```Java
（render阶段）：
Gdx.gl.glClearColor(0, 0, 0, 0); // 置背景全黑
Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
statetime += Gdx.graphics.getDeltaTime(); // 当前状态时间为gdx默认图形绘制间隔时间增值
currentFrame = walkAnimation.getKeyFrame(statetime, true);  // 获取关键帧
batch.begin();
batch.draw(currentFrame, 0, 0, 500, 500); // 画笔绘制当前帧
batch.end();
```

### 0x84 总结
Texture纹理图片经裁剪放入一个TextureRegion[]，动画类通过该帧数组实例化一个内容动态的实例（该实例按时间轴播放），之后每隔一定的时间（比如deltatime）获取该实例当前帧并使用画笔绘制出来。
