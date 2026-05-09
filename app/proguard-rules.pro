# kotlinx-serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.smartir.remote.**$$serializer { *; }
-keepclassmembers class com.smartir.remote.** {
    *** Companion;
}
-keepclasseswithmembers class com.smartir.remote.** {
    kotlinx.serialization.KSerializer serializer(...);
}
