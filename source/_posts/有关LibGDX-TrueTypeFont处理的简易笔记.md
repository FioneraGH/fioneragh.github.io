---
title: 有关LibGDX TrueTypeFont处理的简易笔记
date: 2015-07-03 17:14:58
tags: [libGDX]
---

## [迁移]

### 0x80 谨以备忘
libgdx/extensions/libgdx-freetype用于处理TTF字库。

### 0x81 常用Class
```Java
private BitmapFont font; // 位图字体
private FreeTypeFontGenerator generator; // 字体生成器
private FreeTypeBitmapFontData fontData; // 字体生成器输出字体数据
private SpriteBatch batch; // 画笔
```

### 0x82 初始化变量
```Java
（create阶段）：
generator = new FreeTypeFontGenerator(Gdx.files.internal("data/sans.ttf")); // 从文件构造生成器
fontData = generator.generateData(25, FreeTypeFontGenerator.DEFAULT_CHARS + "世界你好",false); // DEFAULT_CHARS 包含了最基本的字符 "+"后接的是汉字字符映射集，不可重复
font = new BitmapFont(fontData, fontData.getTextureRegions(),false); // 从字体数据中获取字体对象
batch = new SpriteBatch();

```

### 0x83 GL绘制
```Java
（render阶段）：
font.draw(batch, "你好,世界\nTest", 100, 100); // 绘制字体 （"\n"无效，换行使用drawMultiLine）
```

### 0x84 PS
new BitmapFont() 默认构造字体为15pt Arial字体。
