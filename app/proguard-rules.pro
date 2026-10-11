# ProGuard rules for mGBA Emulator

# Keep the main activity and JNI classes
-keep public class com.mgba.emulator.** { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep classes that are used by JNI
-keepclasseswithmembernames class * {
    public static <fields>;
}

# Android classes
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin reflection
-keep class kotlin.reflect.** { *; }
-dontwarn kotlin.reflect.**

# Coroutines
-keepclasseswithmembernames class kotlinx.coroutines.** { *; }

# Serialization
-keepattributes *Annotation*
-keep class org.json.** { *; }

# Android support
-dontwarn androidx.**
-keep class androidx.** { *; }
