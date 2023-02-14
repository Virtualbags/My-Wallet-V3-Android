package com.blockchain.home.presentation.topmovers

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.blockchain.componentlib.lazylist.paddedItem
import com.blockchain.componentlib.tablerow.TableRowHeader
import com.blockchain.componentlib.theme.AppTheme
import com.blockchain.data.DataResource
import com.blockchain.home.presentation.R
import com.blockchain.prices.prices.PriceItemViewState
import com.blockchain.prices.prices.composable.TopMoversScreen
import info.blockchain.balance.AssetInfo

internal fun LazyListScope.topMovers(
    data: DataResource<List<PriceItemViewState>>,
    assetOnClick: (AssetInfo) -> Unit,
) {
    (data as? DataResource.Data)?.data?.let {
        paddedItem(
            paddingValues = PaddingValues(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.size(AppTheme.dimensions.largeSpacing))
            TableRowHeader(
                title = stringResource(R.string.prices_top_movers),
                actionTitle = stringResource(R.string.see_all),
                actionOnClick = { }
            )
            Spacer(modifier = Modifier.size(AppTheme.dimensions.tinySpacing))
        }

        item {
            TopMoversScreen(
                data = data,
                assetOnClick = assetOnClick
            )
        }
    }
}
