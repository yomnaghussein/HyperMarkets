package com.example.myapplication.data

import com.example.myapplication.domain.model.HyperMarket
import com.example.myapplication.utils.HyperMarketNames
import com.google.android.gms.maps.model.LatLng


class PlacesRepository {
    /**
     * get Top 5 latitude and Longitude.
     */
    fun getPlaces(): List<HyperMarket> = listOf(
        HyperMarket(HyperMarketNames.LULU_MARKET, LatLng(24.6337003,46.7156861)),
        HyperMarket(HyperMarketNames.DANUB, LatLng(24.6929773,46.6686099)),
        HyperMarket(HyperMarketNames.HYPER_PANDA, LatLng(24.7025625,46.6518125)),
        HyperMarket(HyperMarketNames.CARREFOUR, LatLng(24.7578002,46.6288076)),
        HyperMarket(HyperMarketNames.OTHAIM_MARKET, LatLng(24.603383,46.704697))
    )
}