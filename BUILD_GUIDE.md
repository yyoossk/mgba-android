# mGBA Android - ビルドガイド

このガイドでは、mGBA AndroidプロジェクトをビルドしてAPKを生成する手順を説明します。

## 🎯 プロジェクト状況

✅ **完成済み要素：**
- Kotlin/Java コード（3つのメインクラス）
- JNI/C ネイティブコード実装
- Android マニフェスト設定
- Gradle ビルド設定
- XML リソースファイル（レイアウト・値）
- CMakeLists.txt（ネイティブコンパイル設定）
- Gradle ラッパースクリプト（gradlew）

⚠️ **注意：**
- ネットワーク接続のない環境ではビルドできません
- Gradle は初回実行時に必要なプラグインをダウンロードします

## 前提条件

### インストール必須

1. **Java Development Kit (JDK)**
   - バージョン: 17以上（推奨：21）
   - ダウンロード: https://www.oracle.com/java/technologies/downloads/
   - または OpenJDK: https://openjdk.org/

2. **Android SDK**
   - API Level 34 以上
   - 方法A: Android Studio 経由（推奨）
     - https://developer.android.com/studio をダウンロード
     - インストール時に SDK/NDK/CMake を自動インストール
   - 方法B: コマンドラインツール経由
     - https://developer.android.com/studio/command-line/sdkmanager

3. **Android NDK (Native Development Kit)**
   - バージョン: r25以上
   - Android SDK Manager で自動インストール可能

4. **CMake**
   - バージョン: 3.22.1以上
   - Android SDK Manager で自動インストール可能

### 環境変数設定

Linux / macOS:
```bash
# ~/.bashrc または ~/.zshrc に追加
export JAVA_HOME=/path/to/java/home          # 例: /usr/lib/jvm/java-21-openjdk
export ANDROID_SDK_ROOT=$HOME/android-sdk    # SDKインストール先
export ANDROID_HOME=$ANDROID_SDK_ROOT
export ANDROID_NDK_ROOT=$ANDROID_SDK_ROOT/ndk/25.1.8937393
export CMAKE_HOME=$ANDROID_SDK_ROOT/cmake/3.22.1
export PATH="$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/emulator:$PATH"

# 設定を反映
source ~/.bashrc
```

Windows (CMD):
```cmd
set JAVA_HOME=C:\Program Files\Java\jdk-21
set ANDROID_SDK_ROOT=%USERPROFILE%\android-sdk
set ANDROID_HOME=%ANDROID_SDK_ROOT%
set ANDROID_NDK_ROOT=%ANDROID_SDK_ROOT%\ndk\25.1.8937393
set CMAKE_HOME=%ANDROID_SDK_ROOT%\cmake\3.22.1
set PATH=%ANDROID_SDK_ROOT%\platform-tools;%PATH%
```

## ビルド手順

### 1. 準備

```bash
cd mgba-android

# ビルドラッパーに実行権限を付与（Linux/macOS のみ）
chmod +x gradlew

# 環境確認
./gradlew --version
```

### 2. デバッグビルド（開発・テスト用）

```bash
# 依存関係を解決してビルド
./gradlew clean build
```

初回実行時は数分かかります（Gradle プラグインをダウンロード）。

**ビルド成果物:**
```
app/build/outputs/apk/debug/app-debug.apk
```

### 3. リリースビルド（配布用）

リリースビルドには署名キー（keystore）が必要です。

#### 3.1 署名キーを作成（初回のみ）

```bash
keytool -genkey -v -keystore release.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias mgba

# プロンプトで以下を入力：
# - パスワード: 任意の強力なパスワード（記録しておく）
# - 姓名: Game Boy Advance Emulator
# - 組織: mGBA Project
# - 場所: Tokyo
# - 都道府県: Tokyo
# - 国コード: JP
```

#### 3.2 リリースビルドを実行

```bash
./gradlew assembleRelease

# keystore のパスワードを入力
# (または build.gradle.kts に設定)
```

**ビルド成果物:**
```
app/build/outputs/apk/release/app-release.apk
```

## デバイスへのインストール

### Androidエミュレータでのテスト

```bash
# 利用可能なAVD一覧
emulator -list-avds

# AVDを起動
emulator -avd Pixel_6_API_34 &

# デバッグAPKをインストール
adb install -r app/build/outputs/apk/debug/app-debug.apk

# アプリを起動
adb shell am start -n com.mgba.emulator/.GameLoaderActivity
```

### 実機へのインストール

```bash
# デバイスをUSBで接続し、USB デバッグを有効にする

# デバイス一覧確認
adb devices

# デバッグAPKをインストール
adb install -r app/build/outputs/apk/debug/app-debug.apk

# アプリを起動
adb shell am start -n com.mgba.emulator/.GameLoaderActivity
```

### ログ確認

```bash
adb logcat | grep "mGBA"
```

## ビルドトラブルシューティング

### エラー: "Plugin not found"

```
Plugin [id: 'com.android.application', version: '8.2.0', apply: false] was not found
```

**原因**: ネットワーク接続がない、またはリポジトリへのアクセスが制限されている

**解決策**:
1. インターネット接続を確認
2. プロキシ設定を確認（企業ネットワーク内の場合）
3. gradle.properties でプロキシを設定:

```properties
systemProp.http.proxyHost=proxy.example.com
systemProp.http.proxyPort=8080
systemProp.http.nonProxyHosts=localhost|127.0.0.1
```

### エラー: "JAVA_HOME not set"

**原因**: Java がインストールされていない、または JAVA_HOME が設定されていない

**解決策**:
```bash
# Java を確認
java -version

# JAVA_HOME を設定
export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
echo $JAVA_HOME  # 確認
```

### エラー: "NDK not found"

**原因**: Android NDK がインストールされていない

**解決策**:
```bash
# Android SDK Manager で NDK をインストール
sdkmanager "ndk;25.1.8937393"

# またはAndroid Studio UI でインストール
# Android Studio → Tools → SDK Manager → SDK Tools → NDK
```

### キャッシュのクリア

ビルドエラーが解決しない場合：

```bash
# Gradle キャッシュをクリア
./gradlew clean

# ローカル .gradle ディレクトリを削除
rm -rf .gradle

# 再度ビルド
./gradlew build
```

## 次のステップ

### 1. mGBA コアソースの統合

```bash
# mGBA リポジトリをクローン
git clone https://github.com/mgba-emu/mgba.git

# コアファイルをコピー
cp -r mgba/src/core/* app/src/main/jni/mgba/
cp -r mgba/include/* app/src/main/jni/mgba/include/
```

### 2. レイアウトとリソースの完成

以下のファイルを作成・完成させてください：

- `app/src/main/res/drawable/ic_launcher.png` - アプリアイコン
- `app/src/main/res/drawable/ic_play.png` - 再生ボタン
- `app/src/main/res/drawable/ic_pause.png` - 一時停止ボタン

### 3. 画面出力・オーディオの実装

以下が現在スケルトン実装です：

- `EmulatorCore.kt`: OpenGL ES フレーム描画
- `mgba_jni.c`: OpenSL ES オーディオコールバック

詳細は IMPLEMENTATION_CHECKLIST.md を参照。

### 4. テスト

```bash
# ユニットテストを実行
./gradlew test

# インストルメンテーションテストを実行（デバイス/エミュレータ必須）
./gradlew connectedAndroidTest
```

## ドキュメント

- **README.md** - プロジェクト概要
- **SETUP.md** - 詳細なセットアップガイド
- **PROJECT_STRUCTURE.md** - ファイル構成説明
- **IMPLEMENTATION_CHECKLIST.md** - 実装進捗管理
- **BUILDENV_STATUS.md** - 環境セットアップ状況（参考用）

## 参考リンク

- [Android NDK 開発ガイド](https://developer.android.com/ndk/guides)
- [Android Gradle Plugin ドキュメント](https://developer.android.com/studio/releases/gradle-plugin)
- [mGBA 公式リポジトリ](https://github.com/mgba-emu/mgba)
- [CMake Android ツールチェーン](https://cmake.org/cmake/help/latest/manual/cmake-toolchains.7.html#android)

## よくある質問 (FAQ)

### Q: リリースAPKはどこに出力されますか？
**A**: `app/build/outputs/apk/release/app-release.apk`

### Q: デバッグAPKでのテストは可能ですか？
**A**: はい。署名キーなしで即座にテストできます。デバッグAPKは `app/build/outputs/apk/debug/app-debug.apk` です。

### Q: Java 11 で動作しますか？
**A**: 公式には Java 17+ 推奨ですが、Java 11 でも試すことはできます。

### Q: x86 エミュレータで動作しますか？
**A**: はい。CMakeLists.txt が x86 / x86_64 も対応しています。

### Q: Kotlin のバージョンは変更できますか？
**A**: はい。`build.gradle.kts` で `kotlin-gradle-plugin` のバージョンを変更できます。

---

**サポート**: 問題が発生した場合は GitHub Issues で報告してください。
