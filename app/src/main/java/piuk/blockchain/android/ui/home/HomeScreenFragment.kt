package piuk.blockchain.android.ui.home

import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import piuk.blockchain.android.campaign.CampaignType
import com.blockchain.coincore.AssetAction
import com.blockchain.coincore.BlockchainAccount
import com.blockchain.coincore.CryptoAccount
import com.blockchain.notifications.analytics.LaunchOrigin
import info.blockchain.balance.AssetInfo
import piuk.blockchain.android.ui.base.mvi.MviFragment
import piuk.blockchain.android.ui.base.mvi.MviIntent
import piuk.blockchain.android.ui.base.mvi.MviModel
import piuk.blockchain.android.ui.base.mvi.MviState
import piuk.blockchain.android.ui.linkbank.BankLinkingInfo
import piuk.blockchain.android.ui.sell.BuySellFragment

interface HomeScreenFragment {
    fun navigator(): HomeNavigator
    fun onBackPressed(): Boolean
}

interface HomeNavigator {
    fun launchDashboard()

    fun launchSwap(
        sourceAccount: CryptoAccount? = null,
        targetAccount: CryptoAccount? = null
    )

    fun launchKyc(campaignType: CampaignType)
    fun launchThePitLinking(linkId: String = "")
    fun launchThePit()
    fun launchBackupFunds(fragment: Fragment? = null, requestCode: Int = 0)
    fun launchSetup2Fa()
    fun launchVerifyEmail()
    fun launchSetupFingerprintLogin()
    fun launchReceive()
    fun launchSend()
    fun launchBuySell(
        viewType: BuySellFragment.BuySellViewType = BuySellFragment.BuySellViewType.TYPE_BUY,
        asset: AssetInfo? = null
    )
    fun launchSimpleBuy(asset: AssetInfo)
    fun launchInterestDashboard(origin: LaunchOrigin)
    fun launchFiatDeposit(currency: String)
    fun launchTransfer()
    fun launchOpenBankingLinking(bankLinkingInfo: BankLinkingInfo)
    fun launchSimpleBuyFromDeepLinkApproval()
    fun launchPendingVerificationScreen(campaignType: CampaignType)

    fun performAssetActionFor(action: AssetAction, account: BlockchainAccount)
    fun resumeSimpleBuyKyc()
}

abstract class HomeScreenMviFragment<M : MviModel<S, I>, I : MviIntent<S>, S : MviState, E : ViewBinding> :
    MviFragment<M, I, S, E>(),
    HomeScreenFragment {

    override fun navigator(): HomeNavigator =
        (activity as? HomeNavigator) ?: throw IllegalStateException("Parent must implement HomeNavigator")
}