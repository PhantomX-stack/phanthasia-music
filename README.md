# Phantasia Music
Offline-first M3 music streaming — InnerTube powered.

## Structure
```
app/
 src/main/
  kotlin/com/phantasia/music/
   network/    HTTP client, cipher, parsers, models, repo
   storage/    SQLCipher Room DB + 2 GB LRU cache
   player/     Media3 ExoPlayer service + queue
   security/   AES-256-GCM prefs + SSL pinning
   ui/         M3 Compose screens + MVI states
  res/
   xml/        network_security_config.xml
   values/     themes.xml
  AndroidManifest.xml
 build.gradle.kts
 proguard-rules.pro
build.gradle.kts
libs.versions.toml
settings.gradle.kts
```

## Build
```bash
./gradlew assembleDebug
```
