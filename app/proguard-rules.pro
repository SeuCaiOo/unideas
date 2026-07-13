# kotlinx.serialization — official baseline rules. Preserves the generated $$serializer
# companion for any @Serializable class (present and future), needed by Navigation Compose's
# type-safe routes at runtime.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.seucaio.unideas.**$$serializer { *; }
-keepclassmembers class com.seucaio.unideas.** {
    *** Companion;
}
-keepclasseswithmembers class com.seucaio.unideas.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Navigation Compose route arguments — domain model enums/sealed types passed as @Serializable
# route args are resolved by fully qualified class name at runtime (not just reflection R8 can
# trace). Confirmed crash without this: ItemsRoute.Form's ItemType arg got stripped, breaking
# navigation at startup on a release build (Crashlytics issue e8d77124d056321595dae7e2a1e7b3db).
-keep class com.seucaio.unideas.domain.model.** { *; }

# Google Drive API client — models bind JSON fields via reflection (GenericJson-style), not
# visible to R8's static analysis.
-keep class com.google.api.client.** { *; }
-keep class com.google.api.services.drive.** { *; }
-keep class com.google.http.client.** { *; }
-dontwarn com.google.api.client.**
-dontwarn com.google.api.services.drive.**

# Transitive Apache HttpClient (pulled in by the Google API client's httpclient transport)
# references optional JNDI/Kerberos codepaths (javax.naming.*, org.ietf.jgss.*) that don't
# exist on Android and are never actually exercised — dead code, safe to silence.
-dontwarn javax.naming.**
-dontwarn org.ietf.jgss.**
