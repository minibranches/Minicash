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

# Widgets (Glance): R8 fusionaba MinicashWidget y QuickActionsWidget en una sola clase y,
# como Glance registra cada receptor por el nombre de su widget, ambos se quedaban cargando.
-keep class * extends androidx.glance.appwidget.GlanceAppWidget
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver
-keep class com.uramos.minicash.widget.** { *; }

# WorkManager 2.9.1 solo conserva la clase de InputMerger, no su constructor; Glance carga cada widget
# con WorkManager y sin el constructor falla con NoSuchMethodException (el widget queda cargando).
-keepclassmembers class * extends androidx.work.InputMerger {
    public <init>();
}
