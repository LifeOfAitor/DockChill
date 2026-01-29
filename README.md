# DockChill

Android aplikazio bat, iPhone-en “Landscape Dock”-aren inspirazioz sortua.  
Mugikorra horizontalki jartzean funtzionatzen du, informazio erabilgarria erakusten du: ordua, eguraldia eta musika.  
Gainera, ezkerreko panelean Pomodoro tenporizadore minimalista eta eskuineko panelean To-Do zerrenda sinple bat ditu.

---

## Funtzionalitateak

### Hasierako pantaila
- **Ordularia**: ordua denbora errealean eguneratzen da.
- **Eguraldia**: zure kokapena erabiliz OpenWeatherMap API-tik datuak jasotzen ditu.
- **Mugikorraren musika**: mugikorrean Spotify bezalako aplikazioak kontrolatzea, kontrol-botoiekin.

### Ezkerreko panela
- **Pomodoro Timer**: lan- eta atseden-denborak kudeatzeko tenporizadore sinplea.
- **Pomodoro Estatistikak**: lan-ikasketa saioak gordetzen dira estatistikak erakusteko. ROOM datu-basea erabiltzen du horretarako.

### Eskuineko panela
- **Zeregin-zerrenda (To-Do)**: zereginak sortu, markatu eta ezabatzeko aukera.
- Datuak lokalki gordetzen dira (Room edo SharedPreferences bidez).

---

## Teknologiak

- **Kotlin**
- **XML Layouts**
- **ViewPager2 + Fragments**
- **Retrofit + Coroutines** (API deietarako)
- **GPS eta baimenak**
- **MediaPlayer**
- **RecyclerView**
- **Room Database**
- **SharedPreferences**
- **Material Design Komponentak**
- **MVVM Arkitektura**
- **LiveData eta ViewModel**

---

## Funtzionamendua

1. Aplikazioa horizontalki irekitzen da, horrela dago diseinatuta.
2. Aplikazioa automatikoki "dock" moduan aktibatzen da.
3. Pantaila nagusian ordua, eguraldia eta musika ikusiko dituzu.
4. Ezkerreko edo eskuineko pantailara mugitu Pomodoro edo To-Do zerrendara sartzeko.


---

## Egilea

**Aitor Gaillard**

Proiektu hau ikasketa- eta esperimentazio-helburuetarako sortua da, Android garapena eta Kotlin praktikan jartzeko.

## Baliabideak

- [Weather Icons](https://github.com/Makin-Things/weather-icons)
- [WeatherAPI](https://www.weatherapi.com/)

**API erantzunaren adibidea nolako datuak lortzen dituen ikusteko:**

``` json
{
    "location": {
        "name": "Irun",
        "region": "Pais Vasco",
        "country": "Spain",
        "lat": 43.35,
        "lon": -1.7833,
        "tz_id": "Europe/Paris",
        "localtime_epoch": 1762848560,
        "localtime": "2025-11-11 09:09"
    },
    "current": {
        "last_updated_epoch": 1762848000,
        "last_updated": "2025-11-11 09:00",
        "temp_c": 17.1,
        "temp_f": 62.8,
        "is_day": 1,
        "condition": {
            "text": "Sunny",
            "icon": "//cdn.weatherapi.com/weather/64x64/day/113.png",
            "code": 1000
        },
        "wind_mph": 10.5,
        "wind_kph": 16.9,
        "wind_degree": 169,
        "wind_dir": "S",
        "pressure_mb": 1012.0,
        "pressure_in": 29.88,
        "precip_mm": 0.0,
        "precip_in": 0.0,
        "humidity": 59,
        "cloud": 0,
        "feelslike_c": 17.1,
        "feelslike_f": 62.8,
        "windchill_c": 9.7,
        "windchill_f": 49.5,
        "heatindex_c": 11.6,
        "heatindex_f": 52.9,
        "dewpoint_c": 7.0,
        "dewpoint_f": 44.5,
        "vis_km": 10.0,
        "vis_miles": 6.0,
        "uv": 0.1,
        "gust_mph": 22.1,
        "gust_kph": 35.5,
        "short_rad": 20.86,
        "diff_rad": 9.77,
        "dni": 0.0,
        "gti": 9.01
    }
}
```

---

## Lizentzia

MIT Lizentzia  
© 2025 Aitor Gaillard