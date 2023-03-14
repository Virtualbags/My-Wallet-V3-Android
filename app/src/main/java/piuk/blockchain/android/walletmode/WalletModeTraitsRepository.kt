package piuk.blockchain.android.walletmode

import com.blockchain.analytics.TraitsService
import com.blockchain.featureflag.FeatureFlag
import com.blockchain.koin.payloadScope
import com.blockchain.koin.topMoversInBuy
import com.blockchain.walletmode.WalletMode
import com.blockchain.walletmode.WalletModeService
import kotlinx.coroutines.rx3.await

class WalletModeTraitsRepository : TraitsService {
    override suspend fun traits(): Map<String, String> {
        val walletModeService = payloadScope.getOrNull<WalletModeService>()
        val topMoversInBuyFF = payloadScope.getOrNull<FeatureFlag>(topMoversInBuy)
        return if (walletModeService != null) {
            val walletMode = walletModeService.walletModeSingle.await()
            mapOf(
                "is_superapp_v1" to true.toString(),
                "app_mode" to walletMode.toTraitsString(),
                "buy_top_movers_enabled" to (topMoversInBuyFF?.coEnabled() ?: false).toString()
            )
        } else
            emptyMap()
    }
}

private fun WalletMode.toTraitsString(): String {
    return when (this) {
        WalletMode.CUSTODIAL -> "TRADING"
        WalletMode.NON_CUSTODIAL -> "PKW"
    }
}
