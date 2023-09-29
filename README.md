# TerraFirmaCraftAnvilCalculator

> 该工具是用于快速计算TerraFirmaCraft（群峦传说）Minecraft模组的铁砧功能而实现的一个快速计算工具。

### 依赖说明：

程序最小依赖群峦传说1.18.2模组文件，其余需求的软件包可依情况缺失，仅影响部分材质/名称的显示，不影响实际使用。

### 使用方法：

首先解压软件包后应该是如下目录结构，其中exe为程序启动入口：<br>
![appPackagedDir](https://github.com/IceLitty/TerraFirmaCraftAnvilCalculator/assets/6522057/2b542887-3ac9-4ad0-9e3d-0ef037da568a)

双击打开后会进行配置文件初始化，及提示缺失mod：<br>
![needModTips](https://github.com/IceLitty/TerraFirmaCraftAnvilCalculator/assets/6522057/38955c61-7211-4cfd-ac3b-4a5702b582a7)

在程序位于根目录生成的mod文件夹中，放置你所下载的群峦传说1.18.2版本的modjar，以及minecraft1.18.2的客户端jar。<br>
群峦传说下载地址：[curseForge](https://www.curseforge.com/minecraft/mc-mods/terrafirmacraft/files?version=1.18.2&gameVersionTypeId=1)<br>
minecraft客户端一般位于`%appdata%\.minecraft\versions\1.18.2\1.18.2.jar`<br>

> 若您非英文用户，则还需要将中文本地化文件打入minecraft客户端jar中，<br>
根据`%appdata%\.minecraft\assets\indexes\1.18.json`索引文件，<br>
如`minecraft/lang/zh_cn.json`的哈希为`9fabbc798786c7b7364902b31bdb37c2c037e9b8`，<br>
则可定位文件位于`%appdata%\.minecraft\assets\objects\9f\9fabbc798786c7b7364902b31bdb37c2c037e9b8`，<br>
将其拷贝至`1.18.2.jar`中的`assets\minecraft\lang\zh_cn.json`。

若缺失模组材质，或缺失模组显示命名的时候在选择合成配方时只会发生如下情况（无法显示材质/无法显示物品名称）仍然可以正常选择配方并计算：<br>
![missingTextureOrDisplayName](https://github.com/IceLitty/TerraFirmaCraftAnvilCalculator/assets/6522057/cebd3afe-eaec-43c6-9074-d3c066c6b685)

任意操作如选择配方、修改地图种子、修改当前值、修改目标值均会触发自动计算，只要目标值与配方都存在，计算就会进行。<br>

程序具有配置文件：<br>
![config](https://github.com/IceLitty/TerraFirmaCraftAnvilCalculator/assets/6522057/0a939f3a-3dc0-483a-a99f-9ef7c5d9dff1)

### 配置文件说明：

目前部分配置项可进行自由调整，大多数配置项是用来定位群峦传说铁砧材质的各个图形坐标宽高。

配置Key | 配置说明 | 默认值
--- | --- | ---
loadResourceRegex | 加载资源时的正则表达式，但实际处理过程中对对应资源位置是有实体转换的，所以此处大幅修改可能会导致错误，抽离出配置项是因为`assets/.*?/models/(?:item\|block)/.*`这一条会导致大幅增加加载时间（9s -> 1m6s）去除后只会影响少量的物品材质因为是模型的原因取不到 | 见程序生成文件
mapSeed | 地图种子预设，面板可填入当前游玩的地图种子，根据1.18.2群峦的逻辑方式计算地图种子和配方为目标值，通过此设置可在加载时就固定化该种子值 | 无
isResetScreenLocation | 暂不使用，现在程序默认显示在屏幕正中心 | false
screenX | 暂不使用，现在程序默认显示在屏幕正中心 | 0
screenY | 暂不使用，现在程序默认显示在屏幕正中心 | 0
scaleUI | UI放大倍率，可通过该值控制UI的整体大小，建议使用偶数 | 2

其余配置项均为材质裁切定位使用。

### 工具截图：

截图 | 截图
:---: | :---:
主界面 | 资源文件加载进度提示
![main](https://github.com/IceLitty/TerraFirmaCraftAnvilCalculator/assets/6522057/edf8b3fb-860a-4a51-a88c-af9aa5a8a281) | ![loading](https://github.com/IceLitty/TerraFirmaCraftAnvilCalculator/assets/6522057/9ff05ac6-0020-4d5a-b187-55f4dd90f0a7)
选择配方 | 根据已选输出/输入筛选可选配方
![chooseRecipe](https://github.com/IceLitty/TerraFirmaCraftAnvilCalculator/assets/6522057/7054f0bb-9c94-48ee-b5dd-8a0dc6f2abce) | ![filterChoose](https://github.com/IceLitty/TerraFirmaCraftAnvilCalculator/assets/6522057/b58d122d-28ee-41df-a4da-e73e52ed0b5d)
计算 | 自动计算 |
![calculating](https://github.com/IceLitty/TerraFirmaCraftAnvilCalculator/assets/6522057/41dab091-a81d-4ab6-bb22-7daff0672de6) | ![actionModifyNowPostionAndAutoCalculating](https://github.com/IceLitty/TerraFirmaCraftAnvilCalculator/assets/6522057/5e0d0dba-2b80-4372-a2dc-b528cb956233)
自动本地化支持 | 
![autoI18nSupport](https://github.com/IceLitty/TerraFirmaCraftAnvilCalculator/assets/6522057/b2f6e662-76ed-44c2-a837-a4f39330756e)
