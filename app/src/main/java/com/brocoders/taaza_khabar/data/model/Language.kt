package com.brocoders.taaza_khabar.data.model

data class Language(
    val code: String,
    val name: String,
    val flag: String
)

object LanguageData {
    // Languages supported by NewsAPI according to their documentation
    val supportedLanguages = listOf(
        Language("en", "English", "🇺🇸"),
        Language("es", "Spanish", "🇪🇸"),
        Language("fr", "French", "🇫🇷"),
        Language("de", "German", "🇩🇪"),
        Language("it", "Italian", "🇮🇹"),
        Language("pt", "Portuguese", "🇵🇹"),
        Language("ru", "Russian", "🇷🇺"),
        Language("zh", "Chinese", "🇨🇳"),
        Language("ar", "Arabic", "🇸🇦"),
        Language("nl", "Dutch", "🇳🇱"),
        Language("sv", "Swedish", "🇸🇪"),
        Language("no", "Norwegian", "🇳🇴"),
        Language("he", "Hebrew", "🇮🇱"),
        Language("ud", "Urdu", "🇵🇰")
    )
    
    fun getDefaultLanguage(): Language = supportedLanguages.first()
    
    fun getLanguageByCode(code: String): Language? = 
        supportedLanguages.find { it.code == code }
} 