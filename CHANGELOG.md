# Muutosloki

## 0.1.0 – 5.11.2026

### Lisäykset
- Ensimmäinen suomenkielinen Android-versio, vaalea Compose-käyttöliittymä ja viiden lähteen yhteinen uutisvirta.
- Kuvalliset ja kuvattomat uutiskortit, lyhyet RSS-kuvaukset, suomalaiset ajat ja alkuperäislinkit selaimeen.
- Yhdistetyt lähde- ja aihesuodattimet alhaalta avautuvassa paneelissa.
- SQLite-offline-otsikot ja -kuvaukset, rajatut välimuistit, rinnakkaiset haut, virhetilat ja vetopäivitys.
- Version tietonäkymä, Gradle wrapper sekä GitHub Actionsin tarkistus- ja allekirjoitettu APK-julkaisupolku.

### Korjaukset
- Android 8.0:n teemasta poistettu API 27:ää vaatinut navigaatiopalkin asetus; reunapalkit hoitaa Activityn yhteensopiva edge-to-edge-toteutus.
- CI käyttää Ubuntu 24.04:n valmista Android SDK:ta. Pelkät Markdown-muutokset eivät käynnistä uutta main-buildia.

### Tarkistukset
- Viiden tuotantokäyttöön valitun RSS-osoitteen HTTP 200 ja XML-rakenne tarkistettu 5.11.2026.
- Kahdeksan parseri- ja suodatustestiä sekä debug-APK-käännös läpäisivät Actions-ajon. Lintin havaitsema API 27 -teema-asetus korjattu; korjauksen CI-varmistus käynnissä.
- Paikallista Android-käännöstä ei ajettu: ympäristössä ei ole JDK:ta tai Android SDK:ta.
- Ei emulaattori- tai fyysisen laitteen testiä. Allekirjoitettua APK-julkaisua ei vielä varmistettu.

### Tiedossa olevat puutteet
- Ylen alueuutisissa ei havaittu kuvia; Yle Häme tarkoittaa Kanta-Hämettä.
- Syötteiden rajalliset uutisikkunat ja epätäydellinen kategoriakartoitus; luokittelemattomat näkyvät Kaikki-näkymässä.
- Sanoman RSS-erityisehtojen sivut palauttivat 403; erityisehtoja ei voitu varmistaa. Käyttö rajattu yksityiseen RSS-lukemiseen, ei julkista sisältöpalvelua.
- Offline-kuvien saatavuutta ei taata. Kokonaisten artikkelien offline-lukua ei ole.
- Pysyvä julkaisuavain ja neljä GitHub Secrets -arvoa on otettava käyttöön README:n mukaan ennen ensimmäistä julkaisutagia.
