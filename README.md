# ActiveLook® HELLO WORLD

Description : Try different features of your Augmented Reality ActiveLook® smart glasses

              A mode exhaustive demo with all features of AciveLook can found here :
			  https://github.com/ActiveLook/demo-app/tree/main/android
			  
### License

```
Licensed under the Apache License, Version 2.0 (the “License”);
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an “AS IS” BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

### Requirements

You will need the following:
- A pair of ActiveLook® glasses
- Android Studio
- An android phone/watch with BLE

Known supported Devices :
- Engo : Cycling & Running action glasses (http://engoeyewear.com/)
- Julbo EVAD : Premium smart glasses providing live data for intense sporting experiences (https://www.julbo.com/en_gb/evad-1)
- Cosmo Connected : GPS & cycling (https://cosmoconnected.com/fr/produits-velo-trottinette/cosmo-vision)

### File to create and add at the root : .env

First, you need to add a file called '.env' at the source of the project. This file will contain only 2 lines :
```
ACTIVELOOK_SDK_TOKEN = ""
ACTIVELOOK_CFG_PASSWORD = 0xDEADBEEF
```

### Main files to modify

The name of the app is defined in the strings.xml file.

* app\src\main\res\layout\content_scrolling.xml
* app\src\main\res\values\strings.xml
* app\src\main\java\com\HelloWorld\demo\MainActivity.java

In order to get the best performances, the ActiveLookSDK directory should be the latest release from : https://github.com/ActiveLook/android-sdk

### detailed description of this Android application

After launching this demo, you have to wait while the data of the config (mainly fonts) are uploaded to the glasses - because of the lines 165 to 177 of MainActivity.java

These files is generated under Windows with the python scripts from : https://github.com/ActiveLook/Config-Generator
but modified as described in the application : ActiveLook FONTSIZES

Then, you can try different features of ActiveLook :
* DEMO : a clock is displayed with the glasses and phone/watch batteries level at the top
* TEXT : you can hit several times on that key and will see different fonts displayed in the glasses
* GESTURE : put you eye in front of your left eye and it wil increment a counter, if you do it twice in less than 5 sec, it is a 'double clic'
* GPS : some GPS data are displayed if you can get GPS signals
* IMAGE1 : you can hit several times on that key and different images will be displayed in the glasses after some seconds, the time to upload them from the phone/watch to the glasses
* IMAGE2 : a different way to display the images using configurations
* SCREEN : a screenshot ofyour phone/watch is displayed in your glasses. But I don't know how to keep that function running while using other applications. I you know and can help me : Your advices are welcome !
* GRAPHIC : some examples of lines, rectangles, circles written in the glasses
* BITMAPS : some examples of the bitmaps available in the glasses are displayed
* ANIMATION : some animated pictures are displayed in the glasses, they are generated from animated GIFs or a serie of images with the Config-Generator mentionned above. NOTE : in the build.gradle you need to use this preliminary version of the SDK : implementation 'com.github.ActiveLook:android-sdk:feat~anim-SNAPSHOT'
