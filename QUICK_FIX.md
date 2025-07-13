# 快速修復構建錯誤

## 在 Android Studio 中打開

1. 打開 Android Studio
2. 選擇 File > Open
3. 選擇 `D:\Seektop\android-tutorial-lib` 文件夾
4. 點擊 OK
5. 等待 Gradle 同步（會自動下載依賴）

## 命令行構建

在項目根目錄執行：

```cmd
gradlew.bat clean build
```

## 如果仍有錯誤

### 1. 檢查 Java 版本
```cmd
java -version
```
應該是 Java 11 或更高版本

### 2. 檢查環境變量
確保設置了 JAVA_HOME：
```cmd
echo %JAVA_HOME%
```

### 3. 強制刷新依賴
```cmd
gradlew.bat clean build --refresh-dependencies
```

### 4. 查看詳細錯誤
```cmd
gradlew.bat build --stacktrace
```

## 項目結構確認

- ✅ tutorial-lib/ - 核心庫模塊
- ✅ sample/ - 示例應用
- ✅ gradle/wrapper/ - Gradle wrapper
- ✅ local.properties - SDK 路徑已設置
- ✅ settings.gradle - 倉庫配置已更新
- ✅ build.gradle - 插件配置已更新

## 最簡單的解決方案

使用 Android Studio 打開項目，它會自動處理所有配置和依賴下載。