# MedTrack Pro

[English](#english) · [简体中文](#简体中文) · [Download APK / 下载 APK](apk/MedTrack-Pro-demo.apk)

## English

MedTrack Pro is an Android prototype for tracking medication schedules and symptoms between appointments. It uses Kotlin, Jetpack Compose, and Room. Patients can record doses, log symptoms, and share a health report. A separate clinician view summarises the records stored on the device.

### Install the demo

1. Download [MedTrack Pro demo APK](apk/MedTrack-Pro-demo.apk) on an Android 16 (API 36) or newer phone or emulator. On GitHub, open the APK link and select **Download raw file**.
2. Open the file to install it. If Android asks for permission, allow installation from the browser or file manager you are using.
3. Open **MedTrack Pro**, tap **Login**, and enter patient ID `1001` and password `DemoPass2026`.

The app opens a sample medication schedule and symptom history. To view the clinician dashboard, go to **Settings → Clinician Login** and enter the public demo code `medtrack-demo`. The included APK is a debug-signed build for hands-on testing. Its sample records and credentials are fictional. Use fictional records and a demo-only password when exploring the app.

### Build from source

Open the repository in Android Studio with JDK 21 and Android SDK 36.1 installed. Select an Android 16 or newer device, then run the `app` configuration. The first Gradle sync downloads project dependencies. To build an APK from the terminal, run:

```sh
./gradlew :app:assembleDebug
```

The build writes `app/build/outputs/apk/debug/app-debug.apk`. Android Studio can set the SDK path during sync. For a terminal build, set `ANDROID_HOME` or provide `sdk.dir` in the root `local.properties`.

### Online features

MedCoach uses openFDA for drug information lookup. Gemini generates medication tips and clinician dashboard summaries in local builds with a configured API key. To try those features, add `GEMINI_API_KEY=your_key_here` to the root `local.properties` and rebuild. Keep the key local and distribute builds made with an empty key. The included APK has an empty Gemini key, so its medication tracking, symptom logging, and local dashboard are ready to explore after installation.

Gemini is the generative AI service integrated into the project. The app is a prototype for tracking and demonstration. Seek medical advice from a qualified health professional.

## 简体中文

MedTrack Pro 是一个 Android 用药和症状记录原型，使用 Kotlin、Jetpack Compose 和 Room。用户可以记录服药情况、填写症状日志并分享健康报告。临床端界面可以查看设备上的记录汇总。

### 安装体验

1. 在 Android 16（API 36）或更新版本的手机或模拟器上下载 [MedTrack Pro 体验 APK](apk/MedTrack-Pro-demo.apk)。在 GitHub 打开 APK 链接后，选择 **Download raw file**。
2. 打开 APK 安装。如果 Android 弹出提示，允许当前浏览器或文件管理器安装应用。
3. 打开 **MedTrack Pro**，点 **Login**，输入患者编号 `1001` 和密码 `DemoPass2026`。

登录后可以查看示例用药安排和症状历史。进入 **Settings → Clinician Login**，输入公开演示码 `medtrack-demo`，就能查看临床端统计。仓库附带的是用调试证书签名的体验包。内置记录和演示凭据均为虚构，体验时也请使用虚构信息和专用的演示密码。

### 从源码构建

用 Android Studio 打开仓库，安装 JDK 21 和 Android SDK 36.1。选用 Android 16 或更新版本的设备，运行 `app` 配置。首次 Gradle 同步会下载依赖。也可以在终端构建 APK：

```sh
./gradlew :app:assembleDebug
```

输出路径是 `app/build/outputs/apk/debug/app-debug.apk`。Android Studio 同步时可以配置 SDK 路径。用终端构建时，可以设置 `ANDROID_HOME`，也可以在项目根目录的 `local.properties` 中设置 `sdk.dir`。

### 联网功能

MedCoach 通过 openFDA 查询药品资料。若要在本地体验 Gemini 生成的用药提示和临床端摘要，在根目录的 `local.properties` 中添加 `GEMINI_API_KEY=your_key_here` 后重新构建。密钥仅供本地使用。分享 APK 时，请用空密钥构建。仓库附带的 APK 使用空 Gemini 密钥，安装后可以直接体验用药记录、症状日志和本地统计。

Gemini 是项目集成的生成式 AI 服务。这个项目是用于记录和演示的原型。如需医疗建议，请咨询合格的医护人员。
