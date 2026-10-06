# mGBA Android - 開発環境セットアップガイド

このドキュメントはmGBA Androidプロジェクトの開発環境構築手順を説明します。

## 前提条件

- Linux / macOS / Windows (WSL2推奨)
- インターネット接続
- 8GB以上のRAM
- 50GB以上のディスク空き容量

## ステップ1: JDKのインストール

### Linux (Ubuntu/Debian)
```bash
sudo apt-get update
sudo apt-get install openjdk-17-jdk-headless
java -version  # バージョン確認
```

### macOS
```bash
brew install openjdk@17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

### Windows (WSL2)
```bash
sudo apt-get install openjdk-17-jdk-headless
```

## ステップ2: Android SDKのインストール

### Option A: Android Studio経由（推奨）

1. [Android Studio](https://developer.android.com/studio)をダウンロード・インストール
2. インストール時にSDK・NDK・CMakeの自動インストールを選択

### Option B: コマンドラインツール経由

```bash
# SDKツールをダウンロード
mkdir -p ~/android-sdk
cd ~/android-sdk
wget https://dl.google.com/android/repository/commandlinetools-linux-9699837_latest.zip
unzip commandlinetools-linux-9699837_latest.zip

# 環境変数設定
export ANDROID_SDK_ROOT=~/android-sdk
export PATH="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$PATH"

# SDKコンポーネントのインストール
sdkmanager "platforms;android-34"
sdkmanager "build-tools;34.0.0"
sdkmanager "ndk;25.1.8937393"
sdkmanager "cmake;3.22.1"
sdkmanager "platform-tools"
```

## ステップ3: 環境変数設定

`~/.bashrc` または `~/.zshrc` に以下を追加:

```bash
# JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64  # Linuxの場合
# export JAVA_HOME=/Library/Java/JavaVirtualMachines/openjdk-17.jdk/Contents/Home  # macOSの場合

# Android SDK
export ANDROID_SDK_ROOT=$HOME/android-sdk
export ANDROID_HOME=$ANDROID_SDK_ROOT
export PATH="$ANDROID_SDK_ROOT/emulator:$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$PATH"

# Android NDK
export ANDROID_NDK_ROOT=$ANDROID_SDK_ROOT/ndk/25.1.8937393
export NDK_HOME=$ANDROID_NDK_ROOT

# CMake
export CMAKE_HOME=$ANDROID_SDK_ROOT/cmake/3.22.1
export PATH="$CMAKE_HOME/bin:$PATH"
```

設定を反映:
```bash
source ~/.bashrc  # または source ~/.zshrc
```

## ステップ4: mGBAソースの準備

```bash
# mGBAリポジトリをクローン
git clone https://github.com/mgba-emu/mgba.git
cd mgba

# 必要なファイル構造を確認
ls -la src/core/  # コアエミュレータコード
ls -la include/   # ヘッダーファイル
```

## ステップ5: Androidプロジェクトのセットアップ

```bash
# mgba-androidディレクトリに移動
cd /path/to/mgba-android

# Gradleラッパーに実行権限を付与
chmod +x gradlew

# 依存関係の確認・ダウンロード
./gradlew dependencies

# ビルドファイルのクリーン
./gradlew clean
```

## ステップ6: ビルド

### デバッグビルド
```bash
./gradlew assembleDebug
# 出力: app/build/outputs/apk/debug/app-debug.apk
```

### リリースビルド
```bash
# keystore.jksの作成（初回のみ）
keytool -genkey -v -keystore release.keystore -keyalg RSA -keysize 2048 -validity 10000 -alias mgba

# ビルド
./gradlew assembleRelease
# 出力: app/build/outputs/apk/release/app-release.apk
```

## ステップ7: エミュレータ/デバイスへのデプロイ

### Androidエミュレータの起動
```bash
# 利用可能なAVDリスト表示
emulator -list-avds

# AVDの起動
emulator -avd Pixel_6_API_34 &
```

### デバイスへのインストール
```bash
# デバイス一覧表示
adb devices

# APKのインストール
adb install -r app/build/outputs/apk/debug/app-debug.apk

# アプリの起動
adb shell am start -n com.mgba.emulator/.GameLoaderActivity
```

### ログ出力確認
```bash
adb logcat | grep "mGBA"
```

## ステップ8: mGBAコアとの統合

### 1. mGBA C/C++ファイルのコピー

```bash
# app/src/main/jni/mgba/にコアファイルをコピー
mkdir -p app/src/main/jni/mgba
cp -r /path/to/mgba/src/core/* app/src/main/jni/mgba/
cp -r /path/to/mgba/include/* app/src/main/jni/mgba/include/
```

### 2. CMakeLists.txtの更新

`CMakeLists.txt`の`MGBA_CORE_SOURCES`セクションを更新:

```cmake
file(GLOB MGBA_CORE_SOURCES
    "mgba/arm/arm.c"
    "mgba/arm/alu.c"
    "mgba/cpu.c"
    "mgba/gba/gba.c"
    "mgba/gba/io.c"
    "mgba/memory.c"
    # ... その他のファイル
)
```

### 3. ビルド・テスト

```bash
./gradlew assembleDebug
```

## トラブルシューティング

### NDK/CMakeが見つからない

```bash
# SDKManagerで確認・インストール
sdkmanager --list | grep ndk
sdkmanager "ndk;25.1.8937393"
```

### Gradleビルドエラー

```bash
# キャッシュクリア・再ビルド
./gradlew clean build --stacktrace
```

### JNI コンパイルエラー

```bash
# ネイティブビルドの詳細ログ
./gradlew assembleDebug --info | grep -i cmake
```

## 参考資料

- [Android NDK 開発ガイド](https://developer.android.com/ndk/guides)
- [Android Gradle Plugin ドキュメント](https://developer.android.com/studio/releases/gradle-plugin)
- [mGBA 公式ドキュメント](https://mgba.io/development)
- [CMake Android ツールチェーン](https://cmake.org/cmake/help/latest/manual/cmake-toolchains.7.html#android)

## サポート

問題が発生した場合:

1. エラーメッセージをコピー
2. `./gradlew clean build --stacktrace` で詳細ログを取得
3. GitHub Issueで報告
