package com.elfmcys.yesstevemodel.client.gui

import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.resource.models.ModelPackData
import com.google.common.collect.Lists
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import org.apache.commons.lang3.StringUtils
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object ModelMetadataPresenter {
    const val DEFAULT_LOCALE = "en_us"

    @JvmStatic
    fun normalizeLocale(locale: String): String =
        locale.lowercase(Locale.ROOT).replace('-', '_')

    @JvmStatic
    fun findLocaleMap(translations: Map<String, Map<String, String>>?, targetLocale: String): Map<String, String>? {
        if (translations.isNullOrEmpty()) return null
        val normalized = normalizeLocale(targetLocale)

        // Direct or normalized exact match
        translations[normalized]?.let { return it }
        for ((key, value) in translations) {
            if (normalizeLocale(key) == normalized) return value
        }
        return null
    }

    @JvmStatic
    fun getLocalizedString(modelPackData: ModelPackData, key: String, defaultValue: String?): String {
        val def = defaultValue ?: StringPool.EMPTY
        val translations = modelPackData.translations ?: return def
        if (translations.isEmpty()) return def

        val selectedLocale = Minecraft.getInstance().languageManager.selected
        return lookupTranslation(translations, selectedLocale, key, def)
    }

    @JvmStatic
    fun getLocalizedModelString(modelAssembly: ModelAssembly, key: String, defaultValue: String): String =
        getLocalizedModelStringForLocale(
            modelAssembly,
            Minecraft.getInstance().languageManager.selected,
            key,
            defaultValue
        )

    @JvmStatic
    fun getLocalizedModelStringForLocale(
        modelAssembly: ModelAssembly,
        locale: String,
        key: String,
        defaultValue: String
    ): String {
        val metadataMap = modelAssembly.expressionCache.metadata
        if (metadataMap.isEmpty()) return defaultValue
        return lookupTranslation(metadataMap, locale, key, defaultValue)
    }

    @JvmStatic
    fun lookupTranslation(
        translations: Map<String, Map<String, String>>,
        locale: String,
        key: String,
        defaultValue: String
    ): String {
        val primary = findLocaleMap(translations, locale)
        val primaryVal = primary?.get(key)
        if (!primaryVal.isNullOrBlank()) return primaryVal

        if (normalizeLocale(locale) != DEFAULT_LOCALE) {
            val fallback = findLocaleMap(translations, DEFAULT_LOCALE)
            val fbVal = fallback?.get(key)
            if (!fbVal.isNullOrBlank()) return fbVal
        }

        return defaultValue
    }

    @JvmStatic
    fun buildModelTooltip(
        modelAssembly: ModelAssembly,
        locale: String,
        fileName: String,
        showAdvancedInfo: Boolean
    ): List<Component> {
        val tooltipLines = Lists.newArrayList<Component>()
        val extraInfo = modelAssembly.modelData.metadata
        if (extraInfo != null) {
            val localizedName = getLocalizedModelStringForLocale(modelAssembly, locale, "metadata.name", extraInfo.name)
            if (StringUtils.isNoneBlank(localizedName))
                tooltipLines.add(Component.literal(localizedName).withStyle(ChatFormatting.GOLD))
            val localizedTips = getLocalizedModelStringForLocale(modelAssembly, locale, "metadata.tips", extraInfo.tips)
            if (StringUtils.isNoneBlank(localizedTips)) {
                localizedTips.replace("\r", StringPool.EMPTY).split("\n").forEach { tipLine ->
                    tooltipLines.add(Component.literal(tipLine).withStyle(ChatFormatting.GRAY))
                }
            }
            if (extraInfo.authors.isNotEmpty() || StringUtils.isNoneBlank(extraInfo.license.first))
                tooltipLines.add(CommonComponents.space())
            if (extraInfo.authors.isNotEmpty()) {
                val authorsString = extraInfo.authors.mapIndexed { index, authorInfo ->
                    val localizedAuthorName = getLocalizedModelStringForLocale(
                        modelAssembly,
                        locale,
                        "metadata.authors.$index.name",
                        authorInfo.name
                    )
                    if (authorInfo.role.isEmpty()) {
                        localizedAuthorName
                    } else {
                        val localizedRole = getLocalizedModelStringForLocale(
                            modelAssembly,
                            locale,
                            "metadata.authors.$index.role",
                            authorInfo.role
                        )
                        "$localizedRole: $localizedAuthorName"
                    }
                }.joinToString("丨")
                tooltipLines.add(
                    Component.translatable(
                        "gui.yes_steve_model.model.authors",
                        Component.literal(authorsString).withStyle(ChatFormatting.DARK_GRAY)
                    )
                )
            }
            if (StringUtils.isNoneBlank(extraInfo.license.first)) {
                tooltipLines.add(
                    Component.translatable(
                        "gui.yes_steve_model.model.license",
                        Component.literal(extraInfo.license.first).withStyle(ChatFormatting.DARK_GRAY)
                    )
                )
            }
        }
        if (showAdvancedInfo) {
            tooltipLines.add(
                Component.translatable(
                    "gui.yes_steve_model.model.file",
                    Component.literal(fileName).withStyle(ChatFormatting.DARK_GRAY)
                )
            )
            tooltipLines.add(
                Component.translatable(
                    "gui.yes_steve_model.model.hash",
                    Component.literal(modelAssembly.modelData.modelHash).withStyle(ChatFormatting.DARK_GRAY)
                )
            )
            if (StringUtils.isNoneBlank(modelAssembly.modelData.extra)) {
                tooltipLines.add(
                    Component.translatable(
                        "gui.yes_steve_model.model.extra",
                        Component.literal(modelAssembly.modelData.extra).withStyle(ChatFormatting.DARK_GRAY)
                    )
                )
            }
            if (modelAssembly.modelData.timestamp != 0L) {
                val formattedDate = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(modelAssembly.modelData.timestamp * 1000),
                    ZoneId.systemDefault()
                )
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                tooltipLines.add(
                    Component.translatable(
                        "gui.yes_steve_model.model.timestamp",
                        Component.literal(formattedDate).withStyle(ChatFormatting.DARK_GRAY)
                    )
                )
            }
            if (StringUtils.isNoneBlank(modelAssembly.modelData.rand)) {
                tooltipLines.add(
                    Component.translatable(
                        "gui.yes_steve_model.model.rand",
                        Component.literal(modelAssembly.modelData.rand).withStyle(ChatFormatting.DARK_GRAY)
                    )
                )
            }
        }
        val info = modelAssembly.modelData.mainModelInfo
        tooltipLines.add(CommonComponents.space())
        tooltipLines.add(
            Component.translatable(
                "gui.yes_steve_model.model.main_model_info",
                info.bones,
                info.cubes,
                info.faces
            ).withStyle(ChatFormatting.GRAY)
        )
        tooltipLines.add(
            Component.translatable(
                "gui.yes_steve_model.model.texture_info",
                modelAssembly.animationBundle.textures.size
            ).withStyle(ChatFormatting.GRAY)
        )
        return tooltipLines
    }
}