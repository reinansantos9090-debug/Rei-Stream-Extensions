# ReiFlix Local catalog

This CloudStream extension turns a local anime library into a CloudStream-style
catalogue: home sections, search, detail pages, episode lists, and the native
video player. Unlike a streaming-provider extension, it does **not** scrape a
catalogue or load video URLs from the internet.

## Library layout

Create one of these folders on the Android device:

* `/storage/emulated/0/ReiFlix`
* `/sdcard/ReiFlix`
* `/storage/emulated/0/Download/ReiFlix`

Use a genre-first layout. Each anime is a directory and each video file is an
episode:

```text
ReiFlix/
├── Ação, Fantasia/
│   └── Fullmetal Alchemist Brotherhood/
│       ├── poster.jpg
│       ├── description.txt
│       ├── 01 - O alquimista de aço.mkv
│       └── 02 - A primeira jornada.mkv
└── Romance/
    └── Your Lie in April/
        └── 01.mp4
```

The top-level folder becomes the CloudStream home section and is also shown as
the anime genre. Separate multiple genres with commas, slashes, or `|`.
Supported video formats are MP4, MKV, WebM, AVI, MOV, and M4V. A local
`poster.jpg`, `poster.jpeg`, `poster.png`, `poster.webp`, or `cover.jpg` is
used when present; `description.txt` becomes the synopsis.

## Privacy and metadata

Video discovery, search, grouping, and playback are local-only. The extension
makes no network request, so a future cover/synopsis downloader or AI metadata
assistant can be added as an explicit opt-in feature without exposing the
library by default. Downloaded covers and synopses should be saved into each
anime folder using the filenames above, keeping the player usable offline.

## Android permission

CloudStream needs **All files access** to read the library on Android 11+. For
the debug build, grant it with:

```bash
adb shell appops set --uid com.lagradost.cloudstream3.prerelease.debug MANAGE_EXTERNAL_STORAGE allow
```

For the prerelease or stable app, replace the package name respectively with
`com.lagradost.cloudstream3.prerelease` or `com.lagradost.cloudstream3`.

## Build

```bash
./gradlew SmartAnimes:make
```
