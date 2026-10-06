# mGBA Android Porting Project

Game Boy Advanceエミュレータ「mGBA」をAndroidアプリとして移植するプロジェクト。

## プロジェクト構成

```
mgba-android/
├── app/                          # Androidアプリメインモジュール
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/mgba/emulator/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── EmulatorCore.kt
│   │   │   │   ├── GameLoaderActivity.kt
│   │   │   │   └── SaveDataManager.kt
│   │   │   ├── jni/
│   │   │   │   ├── CMakeLists.txt
│   │   │   │   └── mgba_jni.c
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   ├── values/
│   │   │   │   └── drawable/
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts              # ルートレベルのビルド設定
├── settings.gradle.kts
├── local.properties              # 環境設定（生成されるファイル）
└── README.md

## 開発環境要件

### 必須ツール
- **Android SDK**: API 31以上
- **Android NDK**: r25c以上
- **CMake**: 3.22.1以上
- **JDK**: 17以上
- **Gradle**: 8.0以上（Android Gradle Pluginと同期）

### 推奨開発環境
- Android Studio Hedgehog (2023.1.1)以上
- Git
- Make（オプション）

## 実装フェーズ

### フェーズ1: ネイティブコアビルド
- mGBAコアのクロスコンパイル設定
- CMakeLists.txtの作成
- 依存ライブラリの統合（SDL2など不要、Androidネイティブに置き換え）

### フェーズ2: JNIブリッジ
- Java ↔ C/C++の通信層
- エミュレータ状態管理
- フレームバッファ共有

### フェーズ3: 基本UIスケルトン
- ゲーム選択画面
- エミュレータ画面
- 基本的なコントローラーUI

### フェーズ4: 機能実装
- ROM読み込み・管理
- セーブデータ管理
- タッチ入力マッピング
- 画面出力（OpenGL ES）
- オーディオ出力

### フェーズ5: テスト・最適化
- デバイステスト
- パフォーマンス最適化
- バグ修正

## セットアップ手順

```bash
# 1. 環境変数設定
export ANDROID_SDK_ROOT=/path/to/android-sdk
export ANDROID_NDK_ROOT=/path/to/ndk/25.1.8937393
export JAVA_HOME=/path/to/jdk17

# 2. ビルド
./gradlew build

# 3. デバイスへのデプロイ
./gradlew installDebug
```

## 主要なコンポーネント詳細

### EmulatorCore.kt
- mGBAコアの管理
- ゲームロード・実行
- フレーム処理
- JNIメソッド呼び出し

### mgba_jni.c
- ネイティブメソッド実装
- GBA実行ループ
- フレームバッファ出力
- オーディオコールバック

### GameLoaderActivity.kt
- ファイルピッカー
- ROM情報表示
- ゲーム起動

### SaveDataManager.kt
- セーブファイル管理
- セーブスロット
- クラウド同期対応設計

## ビルド設定の主要項目

- **minSdk**: 24（Android 7.0）
- **targetSdk**: 34（Android 14）
- **compileSdk**: 34
- **Kotlin版**: 1.9.20

## ライセンス・ソース

- mGBA本体: MPL 2.0
- このAndroidラッパー: MPL 2.0

## 参考リンク

- mGBA公式: https://mgba.io
- Android NDK: https://developer.android.com/ndk
- JNI/NDK開発ガイド: https://developer.android.com/ndk/guides
