package com.alzimerahmed.oasisbrowser.adblock.custom

import android.app.Application
import java.io.File
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomFilterRepository @Inject constructor(application: Application) {
    private val file = File(application.filesDir, "adblock/custom-filters.txt")
    private val filters = linkedMapOf<String, CustomFilter>()
    private var loaded = false

    private var networkCache: NetworkRuleCache? = null
    private var cosmeticCache: List<CosmeticFilter>? = null

    private data class NetworkRuleCache(val exceptions: Set<String>, val domains: Set<String>)

    @Synchronized fun all(): List<CustomFilter> {
        ensureLoaded()
        return filters.values.toList()
    }

    @Synchronized fun add(line: String, source: CustomFilterSource = CustomFilterSource.MANUAL): CustomFilter {
        ensureLoaded()
        val result = CustomFilterParser.parse(line, source)
        val valid = result as? CustomFilterParseResult.Valid
            ?: throw IllegalArgumentException((result as? CustomFilterParseResult.Invalid)?.message ?: "Comment cannot be saved")
        filters[valid.filter.line] = valid.filter
        invalidate()
        save()
        return valid.filter
    }

    @Synchronized fun addAll(lines: Iterable<String>, source: CustomFilterSource = CustomFilterSource.IMPORTED): List<String> {
        ensureLoaded()
        val errors = mutableListOf<String>()
        lines.forEach { line ->
            when (val result = CustomFilterParser.parse(line, source)) {
                is CustomFilterParseResult.Valid -> filters[result.filter.line] = result.filter
                is CustomFilterParseResult.Invalid -> errors += "$line — ${result.message}"
                CustomFilterParseResult.Comment -> Unit
            }
        }
        invalidate()
        save()
        return errors
    }

    @Synchronized fun setEnabled(line: String, enabled: Boolean) {
        ensureLoaded()
        filters[line]?.let { filters[line] = it.copy(enabled = enabled); invalidate(); save() }
    }

    @Synchronized fun delete(line: String) {
        ensureLoaded()
        if (filters.remove(line) != null) {
            invalidate()
            save()
        }
    }

    @Synchronized fun clear() {
        ensureLoaded()
        filters.clear()
        invalidate()
        save()
    }

    @Synchronized fun cosmeticFor(url: String): List<CosmeticFilter> {
        ensureLoaded()
        val host = CustomFilterParser.hostFromUrl(url) ?: return emptyList()
        val cosmetics = cosmeticCache ?: buildCosmeticCache().also { cosmeticCache = it }
        return cosmetics.filter { it.appliesTo(host) }
    }

    @Synchronized fun shouldBlockNetwork(url: String): Boolean {
        ensureLoaded()
        val host = runCatching { URI(url).host?.lowercase() }.getOrNull() ?: return false
        val cache = networkCache ?: buildNetworkCache().also { networkCache = it }

        if (cache.exceptions.any { matchesDomain(host, it) }) return false
        return cache.domains.any { matchesDomain(host, it) }
    }

    private fun ensureLoaded() {
        if (loaded) return
        loaded = true
        file.parentFile?.mkdirs()
        if (!file.exists()) return
        file.readLines().forEach { line ->
            when (val result = CustomFilterParser.parse(line)) {
                is CustomFilterParseResult.Valid -> filters[result.filter.line] = result.filter
                else -> Unit
            }
        }
    }

    private fun invalidate() {
        networkCache = null
        cosmeticCache = null
    }

    private fun buildNetworkCache(): NetworkRuleCache {
        val rules = filters.values.filter { it.enabled }.map { it.line }
        val exceptions = rules
            .filter { it.startsWith("@@||") }
            .map { it.removePrefix("@@||").substringBefore('^').substringBefore('/').lowercase() }
            .toSet()
        val domains = rules
            .filter { it.startsWith("||") }
            .map { it.removePrefix("||").substringBefore('^').substringBefore('/').lowercase() }
            .filter { it.isNotBlank() }
            .toSet()
        return NetworkRuleCache(exceptions, domains)
    }

    private fun buildCosmeticCache(): List<CosmeticFilter> =
        filters.values.asSequence()
            .filter { it.enabled }
            .mapNotNull { filter ->
                (CustomFilterParser.parse(filter.line, filter.source) as? CustomFilterParseResult.Valid)?.cosmetic
            }
            .toList()

    private fun matchesDomain(host: String, domain: String): Boolean =
        host == domain || host.endsWith(".$domain")

    private fun save() {
        val temporary = File(file.parentFile, "${file.name}.tmp")
        temporary.writeText(filters.values.joinToString("\n") { it.line } + if (filters.isNotEmpty()) "\n" else "")
        check(temporary.renameTo(file)) { "Unable to save custom filters" }
    }
}
