# 構建說明

## 首次構建

1. 使用 Android Studio 打開項目
2. 等待 Gradle 同步完成
3. 如果沒有 gradlew，在終端執行：
   ```
   gradle wrapper --gradle-version=8.2
   ```

## 構建庫

```bash
# Windows
gradlew.bat :tutorial-lib:assembleDebug

# Mac/Linux
./gradlew :tutorial-lib:assembleDebug
```

## 運行示例

```bash
# Windows
gradlew.bat :sample:installDebug

# Mac/Linux
./gradlew :sample:installDebug
```

## 發布到本地 Maven

在 `tutorial-lib/build.gradle` 末尾添加：

```gradle
publishing {
    publications {
        release(MavenPublication) {
            groupId = 'com.tutorial'
            artifactId = 'tutorial-lib'
            version = '1.0.0'
            
            afterEvaluate {
                from components.release
            }
        }
    }
}
```

然後執行：
```bash
./gradlew publishToMavenLocal
```

## 在其他項目中使用

```gradle
repositories {
    mavenLocal()
}

dependencies {
    implementation 'com.tutorial:tutorial-lib:1.0.0'
}
```