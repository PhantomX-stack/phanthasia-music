# Phantasia Music
Offline-first M3 music streaming — InnerTube powered.

## Layers
```
app/src/main/kotlin/com/phantasia/music/
  network/   InnerTube client, cipher, parsers, models, repo
  storage/   SQLCipher Room DB + 2 GB LRU cache
  player/    Media3 ExoPlayer service + queue
  security/  AES-256-GCM keystore + SSL pinning + root detection
  ui/        M3 Compose screens + MVI states + dynamic canvas
```

## Build
```bash
./gradlew assembleDebug
```
