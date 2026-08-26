# ============================================================
# FlowDesk Android — ProGuard Rules
# ============================================================

# 디버깅용 스택 트레이스 라인 번호 보존
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================================
# 1. Retrofit / OkHttp
# ============================================================
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepattributes RuntimeVisibleAnnotations
-keepattributes AnnotationDefault

# ============================================================
# 2. Gson / JSON DTO 모델 (직렬화/역직렬화 필드명 보존)
# ============================================================
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class com.example.flowdesk_android.feature.**.data.dto.** { *; }
-keep class com.example.flowdesk_android.feature.**.domain.model.** { *; }
-keep class com.example.flowdesk_android.data.** { *; }

# SerializedName 어노테이션 보존
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ============================================================
# 3. Hilt / Dagger
# ============================================================
-dontwarn dagger.**
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class dagger.hilt.** { *; }
-keepclasseswithmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

# ============================================================
# 4. Android Jetpack (Navigation, ViewModel, LiveData)
# ============================================================
-keep class androidx.navigation.** { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class * extends androidx.fragment.app.Fragment { *; }

# ============================================================
# 5. MPAndroidChart
# ============================================================
-keep class com.github.mikephil.charting.** { *; }

# ============================================================
# 6. Lottie
# ============================================================
-keep class com.airbnb.lottie.** { *; }

# ============================================================
# 7. ColorPickerView
# ============================================================
-keep class com.skydoves.colorpickerview.** { *; }

# ============================================================
# 8. Flexbox
# ============================================================
-keep class com.google.android.flexbox.** { *; }

# ============================================================
# 9. 일반 Android 보존 규칙
# ============================================================
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class **.R$* {
    public static <fields>;
}
-dontwarn okio.**
-dontwarn okhttp3.**