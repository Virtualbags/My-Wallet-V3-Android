package piuk.blockchain.android.ui.dashboard.assetdetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blockchain.wallet.DefaultLabels
import info.blockchain.balance.AssetInfo
import info.blockchain.balance.Money
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import piuk.blockchain.android.R
import piuk.blockchain.android.coincore.AccountGroup
import piuk.blockchain.android.coincore.AssetAction
import piuk.blockchain.android.coincore.AssetFilter
import piuk.blockchain.android.coincore.BlockchainAccount
import piuk.blockchain.android.coincore.CryptoAccount
import piuk.blockchain.android.databinding.ViewAccountCryptoOverviewBinding
import piuk.blockchain.android.ui.customviews.account.CellDecorator
import piuk.blockchain.android.ui.customviews.account.addViewToBottomWithConstraints
import piuk.blockchain.android.util.context
import piuk.blockchain.android.util.gone
import piuk.blockchain.android.util.visible
import kotlin.properties.Delegates

data class AssetDetailItem(
    val assetFilter: AssetFilter,
    val account: BlockchainAccount,
    val balance: Money,
    val fiatBalance: Money,
    val actions: Set<AssetAction>,
    val interestRate: Double
)

class AssetDetailViewHolder(
    private val binding: ViewAccountCryptoOverviewBinding,
    private val labels: DefaultLabels
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        item: AssetDetailItem,
        onAccountSelected: (BlockchainAccount, AssetFilter) -> Unit,
        disposable: CompositeDisposable,
        block: AssetDetailsDecorator
    ) {
        with(binding) {
            val asset = getAsset(item.account, item.balance.currencyCode)

            assetSubtitle.text = when (item.assetFilter) {
                AssetFilter.NonCustodial,
                AssetFilter.Custodial -> labels.getAssetMasterWalletLabel(asset)
                AssetFilter.Interest -> context.resources.getString(
                    R.string.dashboard_asset_balance_interest, item.interestRate
                )
                else -> throw IllegalArgumentException("Not supported filter")
            }

            walletName.text = when (item.assetFilter) {
                AssetFilter.NonCustodial -> labels.getDefaultNonCustodialWalletLabel()
                AssetFilter.Custodial -> labels.getDefaultCustodialWalletLabel()
                AssetFilter.Interest -> labels.getDefaultInterestWalletLabel()
                else -> throw IllegalArgumentException("Not supported filter")
            }

            root.setOnClickListener {
                onAccountSelected(item.account, item.assetFilter)
            }

            when (item.assetFilter) {
                AssetFilter.NonCustodial,
                AssetFilter.Interest,
                AssetFilter.Custodial -> {
                    assetWithAccount.visible()
                    assetWithAccount.updateIcon(
                        when (item.account) {
                            is CryptoAccount -> item.account
                            is AccountGroup -> item.account.selectFirstAccount()
                            else -> throw IllegalStateException(
                                "Unsupported account type for asset details ${item.account}"
                            )
                        }
                    )
                }
                AssetFilter.All -> assetWithAccount.gone()
            }

            walletBalanceFiat.text = item.balance.toStringWithSymbol()
            walletBalanceCrypto.text = item.fiatBalance.toStringWithSymbol()
            disposable += block(item).view(root.context)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    container.addViewToBottomWithConstraints(
                        view = it,
                        bottomOfView = assetSubtitle,
                        startOfView = assetSubtitle,
                        endOfView = walletBalanceCrypto
                    )
                }
        }
    }

    private fun getAsset(account: BlockchainAccount, currency: String): AssetInfo =
        when (account) {
            is CryptoAccount -> account.asset
            is AccountGroup -> account.accounts.filterIsInstance<CryptoAccount>()
                .firstOrNull()?.asset ?: throw IllegalStateException(
                "No crypto accounts found in ${this::class.java} with currency $currency "
            )
            else -> null
        } ?: throw IllegalStateException("Unsupported account type ${this::class.java}")
}

internal class AssetDetailAdapter(
    private val onAccountSelected: (BlockchainAccount, AssetFilter) -> Unit,
    private val labels: DefaultLabels,
    private val assetDetailsDecorator: AssetDetailsDecorator
) : RecyclerView.Adapter<AssetDetailViewHolder>() {
    private val compositeDisposable = CompositeDisposable()

    var itemList: List<AssetDetailItem> by Delegates.observable(emptyList()) { _, oldValue, newValue ->
        if (oldValue != newValue) {
            notifyDataSetChanged()
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        compositeDisposable.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetDetailViewHolder =
        AssetDetailViewHolder(
            ViewAccountCryptoOverviewBinding.inflate(LayoutInflater.from(parent.context), parent, false), labels
        )

    override fun getItemCount(): Int = itemList.size

    override fun onBindViewHolder(holder: AssetDetailViewHolder, position: Int) {
        holder.bind(
            itemList[position],
            onAccountSelected,
            compositeDisposable,
            assetDetailsDecorator
        )
    }
}

typealias AssetDetailsDecorator = (AssetDetailItem) -> CellDecorator