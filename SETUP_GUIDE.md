# 設置指南

## 修復構建錯誤

如果遇到構建錯誤，請按照以下步驟操作：

### 1. 配置 Android SDK 路徑

編輯 `local.properties` 文件，將 SDK 路徑改為你的實際路徑：

```properties
# Windows
sdk.dir=C:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk

# Mac
sdk.dir=/Users/YourUsername/Library/Android/sdk

# Linux
sdk.dir=/home/YourUsername/Android/Sdk
```

### 2. 使用 Android Studio

1. 打開 Android Studio
2. 選擇 "Open an existing project"
3. 選擇 `android-tutorial-lib` 文件夾
4. 等待 Gradle 同步完成
5. Android Studio 會自動下載所需的依賴

### 3. 命令行構建

如果需要在命令行構建：

```bash
# Windows
gradlew.bat clean build

# Mac/Linux
./gradlew clean build
```

### 4. 常見問題

#### 問題：Plugin was not found
解決：確保你有網絡連接，Gradle 需要下載 Android 插件

#### 問題：SDK location not found
解決：創建 local.properties 文件並設置正確的 SDK 路徑

#### 問題：Gradle sync failed
解決：
1. 檢查 File > Settings > Build, Execution, Deployment > Build Tools > Gradle
2. 使用 Gradle wrapper (推薦)
3. JDK 版本應該是 11 或更高

### 5. 手動下載依賴

如果自動下載失敗，可以手動配置代理或鏡像：

在 `gradle.properties` 添加：
```properties
systemProp.http.proxyHost=代理地址
systemProp.http.proxyPort=代理端口
systemProp.https.proxyHost=代理地址
systemProp.https.proxyPort=代理端口
```

或使用阿里雲鏡像，在 `settings.gradle` 的 repositories 中添加：
```gradle
maven { url 'https://maven.aliyun.com/repository/google' }
maven { url 'https://maven.aliyun.com/repository/central' }
```