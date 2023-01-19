package com.blockchain.coincore.testutil

import com.blockchain.api.selfcustody.BalancesResponse
import com.blockchain.core.custodial.domain.TradingService
import com.blockchain.core.price.ExchangeRatesDataManager
import com.blockchain.koin.payloadScopeQualifier
import com.blockchain.logging.RemoteLogger
import com.blockchain.preferences.CurrencyPrefs
import com.blockchain.store.Store
import com.blockchain.testutils.rxInit
import com.blockchain.unifiedcryptowallet.domain.balances.UnifiedBalancesService
import com.nhaarman.mockitokotlin2.mock
import info.blockchain.balance.AssetCategory
import info.blockchain.balance.CryptoCurrency
import info.blockchain.balance.ExchangeRate
import info.blockchain.balance.FiatCurrency
import io.mockk.mockk
import org.junit.After
import org.junit.Rule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

private fun injectMocks(module: Module) {
    startKoin {
        modules(
            listOf(
                module
            )
        )
    }
}

open class CoincoreTestBase {

    @get:Rule
    val initSchedulers = rxInit {
        mainTrampoline()
        ioTrampoline()
        computationTrampoline()
    }

    protected val currencyPrefs: CurrencyPrefs = mock {
        on { selectedFiatCurrency }.thenReturn(TEST_USER_FIAT)
    }

    private val tradingService: TradingService = mock()

    private val mockedRemoteLogger: RemoteLogger = mock()
    private val balancesStore: Store<BalancesResponse> = mock()
    protected val unifiedBalancesService: UnifiedBalancesService = mockk()

    private val userFiatToUserFiat = ExchangeRate(
        from = TEST_USER_FIAT,
        to = TEST_USER_FIAT,
        rate = 1.0.toBigDecimal()
    )

    private val apiFiatToUserFiat = ExchangeRate(
        from = TEST_API_FIAT,
        to = TEST_USER_FIAT,
        rate = 2.0.toBigDecimal()
    )

    protected val exchangeRates: ExchangeRatesDataManager = mock {
        on { getLastFiatToUserFiatRate(TEST_USER_FIAT) }.thenReturn(userFiatToUserFiat)
        on { getLastFiatToUserFiatRate(TEST_API_FIAT) }.thenReturn(apiFiatToUserFiat)
    }

    protected open fun initMocks() {
        injectMocks(
            module {
                scope(payloadScopeQualifier) {
                    factory {
                        currencyPrefs
                    }
                }
                factory {
                    mockedRemoteLogger
                }.bind(RemoteLogger::class)

                factory {
                    unifiedBalancesService
                }.bind(UnifiedBalancesService::class)

                factory {
                    balancesStore
                }.bind(Store::class)

                factory {
                    tradingService
                }.bind(TradingService::class)
            }
        )
    }

    @After
    open fun teardown() {
        stopKoin()
    }

    companion object {
        @JvmStatic
        protected val TEST_USER_FIAT = EUR

        @JvmStatic
        protected val TEST_API_FIAT = USD

        // do not modify these objects, as they're shared between tests
        val TEST_ASSET = object : CryptoCurrency(
            displayTicker = "NOPE",
            networkTicker = "NOPE",
            name = "Not a real thing",
            categories = setOf(AssetCategory.CUSTODIAL),
            precisionDp = 8,
            requiredConfirmations = 3,
            colour = "000000"
        ) {}

        val SECONDARY_TEST_ASSET = object : CryptoCurrency(
            displayTicker = "NOPE2",
            networkTicker = "NOPE2",
            name = "Not a real thing",
            categories = setOf(AssetCategory.CUSTODIAL),
            precisionDp = 8,
            requiredConfirmations = 3,
            colour = "000000"
        ) {}

        val TEST_ASSET_NC = object : CryptoCurrency(
            displayTicker = "NOPE",
            networkTicker = "NOPE",
            name = "Not a real thing",
            categories = setOf(AssetCategory.NON_CUSTODIAL),
            precisionDp = 8,
            requiredConfirmations = 3,
            colour = "000000"
        ) {}
    }
}

internal val USD = FiatCurrency.fromCurrencyCode("USD")
internal val EUR = FiatCurrency.fromCurrencyCode("EUR")
internal val GBP = FiatCurrency.fromCurrencyCode("GBP")

object CoinCoreFakeData {
    val TEST_USER_FIAT = EUR
    val TEST_API_FIAT = USD
    val TEST_ASSET = object : CryptoCurrency(
        displayTicker = "NOPE",
        networkTicker = "NOPE",
        name = "Not a real thing",
        categories = setOf(AssetCategory.CUSTODIAL),
        precisionDp = 8,
        requiredConfirmations = 3,
        colour = "000000"
    ) {}

    val SECONDARY_TEST_ASSET = object : CryptoCurrency(
        displayTicker = "NOPE2",
        networkTicker = "NOPE2",
        name = "Not a real thing",
        categories = setOf(AssetCategory.CUSTODIAL),
        precisionDp = 8,
        requiredConfirmations = 3,
        colour = "000000"
    ) {}

    val TEST_ASSET_NC = object : CryptoCurrency(
        displayTicker = "NOPE",
        networkTicker = "NOPE",
        name = "Not a real thing",
        categories = setOf(AssetCategory.NON_CUSTODIAL),
        precisionDp = 8,
        requiredConfirmations = 3,
        colour = "000000"
    ) {}

    val userFiatToUserFiat = ExchangeRate(
        from = TEST_USER_FIAT,
        to = TEST_USER_FIAT,
        rate = 1.0.toBigDecimal()
    )

    val TEST_TO_USER_RATE = ExchangeRate(
        from = TEST_API_FIAT,
        to = TEST_USER_FIAT,
        rate = 2.0.toBigDecimal()
    )
}
