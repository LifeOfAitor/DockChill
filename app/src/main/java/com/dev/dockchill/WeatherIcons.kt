package com.dev.dockchill

/**
 * WeatherAPIren ikonoen eta eguraldiaren egoerari lotzen dion kodigoa kudeatzen da
 * nik nahi ditudan egoerak eta ikonoak erabiltzeko
 */
data class WeatherInfo(
    val descriptionEus: String,
    val iconRes: Int
)
val weatherMap = mapOf(
    1000 to WeatherInfo("Eguzkitsua", R.drawable.clear),
    1003 to WeatherInfo("Hodei batzuk", R.drawable.cloudy_1_day),
    1006 to WeatherInfo("Hodeitsu", R.drawable.cloudy),
    1009 to WeatherInfo("Zeru estalia", R.drawable.clear),
    1030 to WeatherInfo("Lainoak", R.drawable.cloudy_1_day),
    1063 to WeatherInfo("Euri gutxi", R.drawable.rainy_1_day),
    1066 to WeatherInfo("Elur gutxi", R.drawable.snowy_1_day),
    1069 to WeatherInfo("Izoztua", R.drawable.frost),
    1072 to WeatherInfo("Elur gutxi", R.drawable.snowy_1_day),
    1087 to WeatherInfo("Trumoiak", R.drawable.isolated_thunderstorms_day),
    1114 to WeatherInfo("Elurra", R.drawable.snowy_2),
    1117 to WeatherInfo("Elurra", R.drawable.snow_and_sleet_mix),
    1135 to WeatherInfo("Lanbroa", R.drawable.fog),
    1147 to WeatherInfo("Izoztuta", R.drawable.frost),
    1150 to WeatherInfo("Xirimiri", R.drawable.rainy_1),
    1153 to WeatherInfo("Xirimiri", R.drawable.rainy_1),
    1183 to WeatherInfo("Euri arina", R.drawable.rainy_2),
    1189 to WeatherInfo("Euri asko", R.drawable.rainy_3),
    1195 to WeatherInfo("Euri asko", R.drawable.rainy_3),
    1204 to WeatherInfo("Izotza", R.drawable.frost),
    1210 to WeatherInfo("Elur arina", R.drawable.snowy_1),
    1216 to WeatherInfo("Elur ertaina", R.drawable.snowy_2),
    1225 to WeatherInfo("Elur handia", R.drawable.snowy_3),
    1240 to WeatherInfo("Euri asko", R.drawable.rainy_3),
    1243 to WeatherInfo("Euri nahikoa", R.drawable.rainy_2),
    1246 to WeatherInfo("Euri asko", R.drawable.rainy_3),
    1273 to WeatherInfo("Trumoiak", R.drawable.isolated_thunderstorms),
    1276 to WeatherInfo("Trumoiak eta euria", R.drawable.scattered_thunderstorms),
    1279 to WeatherInfo("Trumoi eta elurra", R.drawable.scattered_thunderstorms),
    1282 to WeatherInfo("Trumoiak eta elurra", R.drawable.thunderstorms)
)
