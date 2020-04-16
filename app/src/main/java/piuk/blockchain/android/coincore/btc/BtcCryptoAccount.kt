package piuk.blockchain.android.coincore.btc

import com.blockchain.swap.nabu.datamanagers.CustodialWalletManager
import info.blockchain.balance.CryptoCurrency
import info.blockchain.balance.CryptoValue
import info.blockchain.wallet.payload.PayloadManager
import info.blockchain.wallet.payload.data.Account
import info.blockchain.wallet.payload.data.LegacyAddress
import io.reactivex.Single
import piuk.blockchain.android.coincore.ActivitySummaryList
import piuk.blockchain.android.coincore.TxCache
import piuk.blockchain.android.coincore.impl.CryptoSingleAccountCustodialBase
import piuk.blockchain.android.coincore.impl.CryptoSingleAccountNonCustodialBase
import piuk.blockchain.android.coincore.impl.transactionFetchCount
import piuk.blockchain.android.coincore.impl.transactionFetchOffset
import piuk.blockchain.androidcore.data.exchangerate.ExchangeRateDataManager
import piuk.blockchain.androidcore.data.payload.PayloadDataManager

internal class BtcCryptoAccountCustodial(
    override val label: String,
    override val custodialWalletManager: CustodialWalletManager,
    override val exchangeRates: ExchangeRateDataManager,
    override val txCache: TxCache
) : CryptoSingleAccountCustodialBase() {
    override val cryptoCurrencies = setOf(CryptoCurrency.BTC)
}

internal class BtcCryptoAccountNonCustodial(
    override val label: String,
    private val address: String,
    private val payloadManager: PayloadManager,
    private val payloadDataManager: PayloadDataManager,
    override val isDefault: Boolean = false,
    override val exchangeRates: ExchangeRateDataManager,
    override val txCache: TxCache
) : CryptoSingleAccountNonCustodialBase() {
    override val cryptoCurrencies = setOf(CryptoCurrency.BTC)

    override val balance: Single<CryptoValue>
        get() = Single.just(payloadManager.getAddressBalance(address))
            .map { CryptoValue.fromMinor(CryptoCurrency.BTC, it) }

    override val activity: Single<ActivitySummaryList>
        get() = Single.fromCallable {
                    payloadManager.getAccountTransactions(address, transactionFetchCount, transactionFetchOffset)
                    .map {
                        BtcActivitySummaryItem(
                            it,
                            payloadDataManager,
                            exchangeRates
                        )
                }
        }
        .doOnSuccess { txCache.addToCache(it) }
        .map { txCache.asActivityList() }

    constructor(
        jsonAccount: Account,
        payloadManager: PayloadManager,
        payloadDataManager: PayloadDataManager,
        isDefault: Boolean = false,
        exchangeRates: ExchangeRateDataManager,
        txCache: TxCache
    ) : this(
        jsonAccount.label,
        jsonAccount.xpub,
        payloadManager,
        payloadDataManager,
        isDefault,
        exchangeRates,
        txCache
    )

    constructor(
        legacyAccount: LegacyAddress,
        payloadManager: PayloadManager,
        payloadDataManager: PayloadDataManager,
        exchangeRates: ExchangeRateDataManager,
        txCache: TxCache
    ) : this(
        legacyAccount.label,
        legacyAccount.address,
        payloadManager,
        payloadDataManager,
        false,
        exchangeRates,
        txCache
    )
}