package piuk.blockchain.android.ui.coinview.presentation.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import com.blockchain.analytics.Analytics
import com.blockchain.analytics.events.LaunchOrigin
import com.blockchain.coincore.AccountBalance
import com.blockchain.coincore.ActionState
import com.blockchain.coincore.ActivitySummaryList
import com.blockchain.coincore.AssetAction
import com.blockchain.coincore.CryptoAccount
import com.blockchain.coincore.ReceiveAddress
import com.blockchain.coincore.SingleAccount
import com.blockchain.coincore.StateAwareAction
import com.blockchain.coincore.TradingAccount
import com.blockchain.coincore.TxSourceState
import com.blockchain.componentlib.alert.AlertType
import com.blockchain.componentlib.alert.CardAlert
import com.blockchain.componentlib.basic.Image
import com.blockchain.componentlib.basic.ImageResource
import com.blockchain.componentlib.icons.Icons
import com.blockchain.componentlib.icons.Info
import com.blockchain.componentlib.system.ShimmerLoadingTableRow
import com.blockchain.componentlib.tablerow.BalanceTableRow
import com.blockchain.componentlib.tablerow.DefaultTableRow
import com.blockchain.componentlib.theme.AppTheme
import com.blockchain.componentlib.theme.Grey400
import com.blockchain.componentlib.utils.TextValue
import com.blockchain.componentlib.utils.previewAnalytics
import com.blockchain.componentlib.utils.value
import com.blockchain.data.DataResource
import com.blockchain.data.FreshnessStrategy
import info.blockchain.balance.CryptoCurrency
import info.blockchain.balance.Currency
import info.blockchain.balance.Money
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.koin.androidx.compose.get
import piuk.blockchain.android.R
import piuk.blockchain.android.simplebuy.CustodialBalanceClicked
import piuk.blockchain.android.ui.coinview.domain.model.CoinviewAccount
import piuk.blockchain.android.ui.coinview.domain.model.isInterestAccount
import piuk.blockchain.android.ui.coinview.domain.model.isPrivateKeyAccount
import piuk.blockchain.android.ui.coinview.domain.model.isStakingAccount
import piuk.blockchain.android.ui.coinview.domain.model.isTradingAccount
import piuk.blockchain.android.ui.coinview.presentation.CoinViewNetwork
import piuk.blockchain.android.ui.coinview.presentation.CoinviewAccountsState
import piuk.blockchain.android.ui.coinview.presentation.LogoSource
import piuk.blockchain.android.ui.dashboard.coinview.CoinViewAnalytics

@Composable
fun AssetAccounts(
    analytics: Analytics = get(),
    data: DataResource<CoinviewAccountsState?>,
    l1Network: CoinViewNetwork?,
    assetTicker: String,
    onAccountClick: (CoinviewAccount) -> Unit,
    onLockedAccountClick: () -> Unit
) {
    when (data) {
        DataResource.Loading -> {}

        is DataResource.Error -> {
            AssetAccountsError()
        }

        is DataResource.Data -> {
            AssetAccountsData(
                analytics = analytics,
                data = data,
                l1Network = l1Network,
                assetTicker = assetTicker,
                onAccountClick = onAccountClick,
                onLockedAccountClick = onLockedAccountClick
            )
        }
    }
}

@Composable
fun AssetAccountsLoading() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppTheme.dimensions.smallSpacing)
            .background(color = Color.White, shape = RoundedCornerShape(AppTheme.dimensions.borderRadiiMedium))
    ) {
        ShimmerLoadingTableRow(showIconLoader = true)
    }
}

@Composable
fun AssetAccountsError() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppTheme.dimensions.smallSpacing)
            .background(color = Color.White, shape = RoundedCornerShape(AppTheme.dimensions.borderRadiiMedium))
    ) {
        CardAlert(
            title = stringResource(R.string.coinview_account_load_error_title),
            subtitle = stringResource(R.string.coinview_account_load_error_subtitle),
            alertType = AlertType.Warning,
            backgroundColor = AppTheme.colors.background,
            isBordered = false,
            isDismissable = false,
        )
    }
}

@Composable
fun AssetAccountsData(
    analytics: Analytics = get(),
    assetTicker: String,
    l1Network: CoinViewNetwork?,
    data: DataResource.Data<CoinviewAccountsState?>,
    onAccountClick: (CoinviewAccount) -> Unit,
    onLockedAccountClick: () -> Unit
) {
    data.data?.let {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.dimensions.smallSpacing)
        ) {

            // balance
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    modifier = Modifier.weight(1F),
                    text = stringResource(R.string.common_balance),
                    style = AppTheme.typography.body2,
                    color = AppTheme.colors.muted
                )

                Text(
                    text = it.totalBalance,
                    style = AppTheme.typography.body2,
                    color = AppTheme.colors.title,
                )
            }

            Spacer(modifier = Modifier.size(AppTheme.dimensions.tinySpacing))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White, shape = RoundedCornerShape(AppTheme.dimensions.borderRadiiMedium))
            ) {
                // accounts
                it.accounts.forEachIndexed { index, account ->
                    when (account) {
                        is CoinviewAccountsState.CoinviewAccountState.Available -> {
                            BalanceTableRow(
                                titleStart = buildAnnotatedString { append(account.title) },
                                bodyStart = account.subtitle?.let { buildAnnotatedString { append(it.value()) } },
                                titleEnd = buildAnnotatedString { append(account.fiatBalance) },
                                bodyEnd = buildAnnotatedString { append(account.cryptoBalance) },
                                startImageResource = when (account.logo) {
                                    is LogoSource.Remote -> {
                                        ImageResource.Remote(url = account.logo.value, shape = CircleShape)
                                    }
                                    is LogoSource.Resource -> {
                                        ImageResource.Local(
                                            id = account.logo.value,
                                            colorFilter = ColorFilter.tint(
                                                Color(android.graphics.Color.parseColor(account.assetColor))
                                            ),
                                            shape = CircleShape
                                        )
                                    }
                                },
                                tags = emptyList(),
                                backgroundColor = Color.Transparent,
                                onClick = {
                                    account.cvAccount.account.let { account ->
                                        if (account is CryptoAccount && account is TradingAccount) {
                                            analytics.logEvent(CustodialBalanceClicked(account.currency))
                                        }
                                    }

                                    analytics.logEvent(
                                        CoinViewAnalytics.WalletsAccountsClicked(
                                            origin = LaunchOrigin.COIN_VIEW,
                                            currency = assetTicker,
                                            accountType = account.cvAccount.toAccountType()
                                        )
                                    )

                                    onAccountClick(account.cvAccount)
                                }
                            )
                        }
                        is CoinviewAccountsState.CoinviewAccountState.Unavailable -> {
                            DefaultTableRow(
                                primaryText = account.title,
                                secondaryText = account.subtitle.value(),
                                startImageResource = when (account.logo) {
                                    is LogoSource.Remote -> {
                                        ImageResource.Remote(url = account.logo.value, shape = CircleShape)
                                    }
                                    is LogoSource.Resource -> {
                                        ImageResource.Local(
                                            id = account.logo.value,
                                            colorFilter = ColorFilter.tint(Grey400),
                                            shape = CircleShape
                                        )
                                    }
                                },
                                endImageResource = ImageResource.Local(
                                    R.drawable.ic_lock, colorFilter = ColorFilter.tint(Grey400)
                                ),
                                backgroundColor = Color.Transparent,
                                onClick = { onLockedAccountClick() }
                            )
                        }
                    }

                    if (index < it.accounts.lastIndex) {
                        Divider(color = Color(0XFFF1F2F7))
                    }
                }
            }

            // l1 netwrok
            l1Network?.let { l1Network ->
                Spacer(modifier = Modifier.size(AppTheme.dimensions.smallSpacing))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color.White, shape = RoundedCornerShape(AppTheme.dimensions.borderRadiiMedium)
                        )
                        .padding(AppTheme.dimensions.tinySpacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        ImageResource.Remote(
                            url = l1Network.logo,
                            shape = CircleShape,
                            size = AppTheme.dimensions.mediumSpacing
                        )
                    )

                    Spacer(modifier = Modifier.size(AppTheme.dimensions.tinySpacing))

                    Text(
                        modifier = Modifier.weight(1F),
                        text = stringResource(R.string.coinview_asset_l1, it.assetName, l1Network.name),
                        style = AppTheme.typography.paragraph2,
                        color = AppTheme.colors.muted,
                    )

                    Spacer(modifier = Modifier.size(AppTheme.dimensions.tinySpacing))

                    Image(
                        Icons.Filled.Info.copy(colorFilter = ColorFilter.tint(AppTheme.colors.dark))
                    )
                }
            }
        }
    }
}

private fun CoinviewAccount.toAccountType() = when {
    isTradingAccount() -> CoinViewAnalytics.Companion.AccountType.CUSTODIAL
    isInterestAccount() -> CoinViewAnalytics.Companion.AccountType.REWARDS_ACCOUNT
    isStakingAccount() -> CoinViewAnalytics.Companion.AccountType.STAKING_ACCOUNT
    isPrivateKeyAccount() -> CoinViewAnalytics.Companion.AccountType.USERKEY
    else -> CoinViewAnalytics.Companion.AccountType.EXCHANGE_ACCOUNT
}

@Preview(showBackground = true, backgroundColor = 0XFFF0F2F7)
@Composable
fun PreviewAssetAccounts_Loading() {
    AssetAccounts(
        analytics = previewAnalytics,
        data = DataResource.Loading,
        l1Network = null,
        assetTicker = "ETH",
        onAccountClick = {},
        onLockedAccountClick = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0XFFF0F2F7)
@Composable
fun PreviewAssetAccounts_Error() {
    AssetAccounts(
        analytics = previewAnalytics,
        data = DataResource.Error(Exception()),
        l1Network = null,
        assetTicker = "ETH",
        onAccountClick = {},
        onLockedAccountClick = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0XFFF0F2F7)
@Composable
fun PreviewAssetAccounts_Data() {
    AssetAccounts(
        analytics = previewAnalytics,
        data = DataResource.Data(
            CoinviewAccountsState(
                totalBalance = "$2,000.00",
                accounts = listOf(
                    CoinviewAccountsState.CoinviewAccountState.Available(
                        cvAccount = previewCvAccount,
                        title = "Ethereum 1",
                        subtitle = TextValue.StringValue("ETH"),
                        cryptoBalance = "0.90349281 ETH",
                        fiatBalance = "$2,000.00",
                        logo = LogoSource.Resource(R.drawable.ic_interest_account_indicator),
                        assetColor = "#324921"
                    ),
                    CoinviewAccountsState.CoinviewAccountState.Available(
                        cvAccount = previewCvAccount,
                        title = "Ethereum 2",
                        subtitle = TextValue.StringValue("ETH"),
                        cryptoBalance = "0.90349281 ETH",
                        fiatBalance = "$2,000.00",
                        logo = LogoSource.Resource(R.drawable.ic_interest_account_indicator),
                        assetColor = "#324921"
                    ),
                    CoinviewAccountsState.CoinviewAccountState.Unavailable(
                        cvAccount = previewCvAccount,
                        title = "Ethereum 2",
                        subtitle = TextValue.StringValue("ETH"),
                        logo = LogoSource.Resource(R.drawable.ic_interest_account_indicator)
                    )
                ),
                assetName = "Ethereum"
            )
        ),
        l1Network = CoinViewNetwork("", "MATIC"),
        assetTicker = "ETH",
        onAccountClick = {},
        onLockedAccountClick = {}
    )
}

private val previewBlockchainAccount = object : SingleAccount {
    override val isDefault: Boolean
        get() = false
    override val currency: Currency
        get() = error("preview")
    override val sourceState: Single<TxSourceState>
        get() = error("preview")
    override val label: String
        get() = error("preview")

    override fun balanceRx(freshnessStrategy: FreshnessStrategy): Observable<AccountBalance> {
        error("preview")
    }

    override fun activity(freshnessStrategy: FreshnessStrategy): Observable<ActivitySummaryList> {
        error("preview")
    }

    override val isFunded: Boolean
        get() = error("preview")
    override val hasTransactions: Boolean
        get() = error("preview")
    override val receiveAddress: Single<ReceiveAddress>
        get() = error("preview")
    override val stateAwareActions: Single<Set<StateAwareAction>>
        get() = error("preview")

    override fun stateOfAction(assetAction: AssetAction): Single<ActionState> {
        error("preview")
    }
}

val previewCvAccount: CoinviewAccount = CoinviewAccount.PrivateKey(
    account = previewBlockchainAccount,
    cryptoBalance = DataResource.Data(Money.zero(CryptoCurrency.BTC)),
    fiatBalance = DataResource.Data(Money.zero(CryptoCurrency.BTC)),
    isEnabled = false
)
