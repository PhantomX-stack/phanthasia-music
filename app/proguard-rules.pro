-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep @kotlinx.serialization.Serializable class com.phantasia.music.network.** { *; }
-keep class com.phantasia.music.storage.** { *; }
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class net.sqlcipher.** { *; }
-repackageclasses "p"
-allowaccessmodification
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
