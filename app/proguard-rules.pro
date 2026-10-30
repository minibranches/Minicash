# Room Database & DAOs
-keep class androidx.room.RoomDatabase
-dontwarn androidx.room.**
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers class * {
    @androidx.room.* <fields>;
    @androidx.room.* <methods>;
}

# AndroidX Keep Annotations
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Minicash Data Layer (Entities, DAOs, Enums, Backup Models & Preferences)
-keep class com.uramos.minicash.data.** { *; }
-keep class com.uramos.minicash.data.local.entities.** { *; }
-keep class com.uramos.minicash.data.local.dao.** { *; }
-keep class com.uramos.minicash.data.local.backup.** { *; }
-keep class com.uramos.minicash.data.model.** { *; }
-keepclassmembers class com.uramos.minicash.data.local.backup.** { <fields>; }

# Gson & Backup Models (Protección de serialización JSON vía SAF)
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
