Crushing Table Android Client
============================

# Requirements to build
* Android Studio Beta 0.9.2 (Canary Channel)
* Android SDK Tools 23.0.5
* Android SDK Platform Tools 21
* Android SDK Build-tools 21.1.1
* Android SDK Platform 21
* Android Repository 8 (Available via SDK Manager)
* Google Repository 13 (Available via SDK Manager)

# How to import into IDE
After launching Android Studio, go to File -> Import Project and search folder where the code is.

# How to build outside IDE
Donwload the Android SDK itself, download the required tools and create an environment variable called ANDROID_HOME pointing to it.
After, use the gradle wrapper available in the root folder of project (called gradlew or gradlew.bat) and run:
```
./gradlew assembleDebug
or
./gradlew assembleRelease
```

# Running using Genymotion
**Genymotion** is an Android emulator, so much faster than the default one shipped with the SDK. At first, install [Oracle Virtual Box](https://www.virtualbox.org/) as Genymotion depends on it.
Next, download and install [Genymotion](http://www.genymotion.com/). It needs a registration to download.
Right after, run Genymotion and sign in to view the available devices. Select one you want and download it. A Nexus 4 with Android 4.2 is a good choice.

After the device download, run it. Now we need to install Google Apps on it. Download the [Genymotion ARM Translator](http://forum.xda-developers.com/attachment.php?attachmentid=2680937&d=1397258016) and the GApps version that matches the device you've created:

* [Android 4.4](http://itvends.com/gapps/gapps-kk-20140105-signed.zip)
* [Android 4.3](http://goo.im/gapps/gapps-jb-20130813-signed.zip)
* [Android 4.2](http://goo.im/gapps/gapps-jb-20130812-signed.zip)
* [Android 4.1](http://goo.im/gapps/gapps-jb-20121011-signed.zip)

Now, simply drag and drop the two zip files into Genymotion. After installation, restart (close and open again) Genymotion. If everything is well, we'll see apps like Google Play in the emulator. Open it to test.

Back to Android Studio, simply click in the play button in the toolbar with Genymotion running, and it will appear in the list as a target.

# Running on a Real Device
First, you need to enable application debug in the phone. If your device is running Android 4.2+, you need to enable the __Developer Settings__, as it's hidden by default. Launch the __Settings__ app, select __About phone__, then click 7 times in the __Build number__ info. Now, you're a developer.

Then, back to main settings screen, select __Developer options__, turn on __Developer options__ on ActionBar, then check __Android debugging__.

If you are running Windows, assert that the drivers provided by yout phone manufacturer are installed properly (MacOS and Linux works by default, without any action). Then, plug your phone to the computer, and click Run in __Android Studio__. If a confirmation be prompted on the phone, click __OK__.

__PS:__ The option names can vary a bit due to different manufacturer customizations.

Enjoy it!