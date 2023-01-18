package com.blockchain.prices.prices

import com.blockchain.commonarch.presentation.mvi_v2.ModelState
import com.blockchain.core.price.Prices24HrWithDelta
import com.blockchain.data.DataResource
import info.blockchain.balance.AssetInfo

data class PricesModelState(
    val filters: List<PricesFilter> = emptyList(),
    val tradableCurrencies: DataResource<List<String>> = DataResource.Loading,
    val watchlist: DataResource<List<String>> = DataResource.Loading,
    val data: DataResource<List<AssetPriceInfo>> = DataResource.Loading,
    val filterTerm: String = "",
    val filterBy: PricesFilter = PricesFilter.All,
    val lastFreshDataTime: Long = 0
) : ModelState

enum class PricesFilter {
    All, Tradable, Favorites
}

data class AssetPriceInfo(
    val price: DataResource<Prices24HrWithDelta>,
    val assetInfo: AssetInfo,
)
