package com.alzimerahmed.oasisbrowser.adblock

import com.alzimerahmed.oasisbrowser.R
import com.alzimerahmed.oasisbrowser.adblock.source.HostsDataSourceProvider
import com.alzimerahmed.oasisbrowser.adblock.source.HostsResult
import com.alzimerahmed.oasisbrowser.adblock.util.BloomFilter
import com.alzimerahmed.oasisbrowser.adblock.util.DefaultBloomFilter
import com.alzimerahmed.oasisbrowser.adblock.util.DelegatingBloomFilter
import com.alzimerahmed.oasisbrowser.adblock.util.hash.MurmurHashHostAdapter
import com.alzimerahmed.oasisbrowser.adblock.util.hash.MurmurHashStringAdapter
import com.alzimerahmed.oasisbrowser.adblock.util.`object`.JvmObjectStore
import com.alzimerahmed.oasisbrowser.adblock.util.`object`.ObjectStore
import com.alzimerahmed.oasisbrowser.browser.di.DatabaseScheduler
import com.alzimerahmed.oasisbrowser.concurrency.CoroutineDispatchers
import com.alzimerahmed.oasisbrowser.database.adblock.Host
import com.alzimerahmed.oasisbrowser.database.adblock.HostsPreferenceStore
import com.alzimerahmed.oasisbrowser.database.adblock.HostsRepository
import com.alzimerahmed.oasisbrowser.extensions.toast
import com.alzimerahmed.oasisbrowser.log.Logger
import android.app.Application
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.net.URISyntaxException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * An [AdBlocker] that is backed by a [BloomFilter].
 *
 * @param logger The logger used to log status.
 * @param hostsDataSourceProvider The provider that provides the data source used to populate the
 * bloom filter and [hostsRepository].
 * @param hostsRepository The long term store for blocked hosts.
 */
@Singleton
class BloomFilterAdBlocker @Inject constructor(
    private val logger: Logger,
    private val hostsDataSourceProvider: HostsDataSourceProvider,
    private val hostsRepository: HostsRepository,
    private val hostsPreferenceStore: HostsPreferenceStore,
    private val application: Application,
    private val appCoroutineScope: CoroutineScope,
    @DatabaseScheduler
    private val objectStoreDispatcher: CoroutineDispatcher,
    private val coroutineDispatchers: CoroutineDispatchers,
) : AdBlocker {

    private val bloomFilter: DelegatingBloomFilter<Host> = DelegatingBloomFilter()
    private val objectStore: ObjectStore<DefaultBloomFilter<Host>> = JvmObjectStore(
        application = application,
        hashingAlgorithm = MurmurHashStringAdapter(),
        key = BLOOM_FILTER_KEY,
        objectStoreDispatcher = objectStoreDispatcher,
    )

    private val loadHostsFlow = MutableStateFlow(true)

    init {
        loadHostsFlow
            .buffer(capacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            .map { forceRefresh ->
                val storedBloomFilter = objectStore.retrieve()
                val hostsDataSource = hostsDataSourceProvider.createHostsDataSource()
                val hostsDataSourceIdentifier = hostsDataSource.identifier()
                // Force a new hosts request if the hosts are out of date or if the repo has no hosts.
                if (!forceRefresh &&
                    storedBloomFilter != null &&
                    hostsRepository.hasHosts() &&
                    hostsPreferenceStore.identity.get() == hostsDataSourceIdentifier
                ) {
                    return@map storedBloomFilter
                }

                when (val result = hostsDataSource.loadHosts()) {
                    is HostsResult.Failure -> {
                        logger.log(TAG, "Unable to load hosts", result.cause)
                        null
                    }

                    is HostsResult.Success -> {
                        // Clear out the old hosts and bloom filter now that we have the new hosts.
                        hostsRepository.removeAllHosts()
                        hostsRepository.addHosts(result.hosts)
                        hostsPreferenceStore.identity.set(hostsDataSourceIdentifier)
                        createAndSaveBloomFilter(result.hosts)
                    }
                }
            }
            .flowOn(coroutineDispatchers.io)
            .onEach {
                // If we were unsuccessful in loading hosts, and we don't have hosts in the repo, don't
                // allow initialization, as false positives will result in bad browsing experience.
                if (hostsRepository.hasHosts() && it != null) {
                    bloomFilter.delegate = it
                    logger.log(TAG, "Finished loading bloom filter")
                } else {
                    logger.log(TAG, "Failed to load bloom filter")
                    appCoroutineScope.launch(coroutineDispatchers.main) {
                        application.toast(R.string.ad_block_load_failure)
                    }
                }
            }
            .launchIn(appCoroutineScope)
    }

    /**
     * Force the ad blocker to (re)populate its internal hosts filter from the provided hosts data
     * source.
     */
    fun populateAdBlockerFromDataSource(forceRefresh: Boolean) = loadHostsFlow.tryEmit(forceRefresh)

    private suspend fun createAndSaveBloomFilter(hosts: List<Host>): BloomFilter<Host> {
        logger.log(TAG, "Constructing bloom filter from list")

        val bloomFilter = DefaultBloomFilter(
            numberOfElements = hosts.size,
            falsePositiveRate = 0.01,
            hashingAlgorithm = MurmurHashHostAdapter()
        )
        bloomFilter.putAll(hosts)
        objectStore.store(bloomFilter)

        return bloomFilter
    }

    override fun isAd(url: String): Boolean {
        val domain = url.host() ?: return false

        val mightBeOnBlockList = bloomFilter.mightContain(domain)

        return when {
            mightBeOnBlockList -> {
                val isOnBlockList = hostsRepository.containsHost(domain)
                if (isOnBlockList) {
                    logger.log(TAG, "Request host blocked by ad blocker")
                } else {
                    logger.log(TAG, "Ad blocker bloom filter false positive")
                }

                isOnBlockList
            }

            domain.name.startsWith("www.") -> isAd(domain.name.substring(4))
            else -> false
        }
    }

    /**
     * Extract the [Host] from a [String] representing a URL. Returns null if no host was extracted.
     */
    private fun String.host(): Host? = try {
        this.toUri().host?.let(::Host)
    } catch (exception: URISyntaxException) {
        logger.log(TAG, "Invalid URL", exception)
        null
    }

    companion object {
        private const val TAG = "BloomFilterAdBlocker"
        private const val BLOOM_FILTER_KEY = "AdBlockingBloomFilter"
    }
}
