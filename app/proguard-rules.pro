# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature, InnerClasses, EnclosingMethod

# OkHttp
-keepattributes Annotation
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Kotlin
-keep class kotlin.Metadata { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*

# Hilt
-keep class * extendsdagger.hilt.android.plugin.HiltAndroidApp { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class *
-keepclassmembers class * {
    @dagger.hilt.Inject <init>(...);
}
