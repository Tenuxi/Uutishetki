# Muutosloki

## 0.2.0 – 5.11.2026

### Lisäykset
- Aiheiden monivalinta. Ensimmäisellä käynnistyksellä kaikki aiheet paitsi Urheilu, myös Digi, ovat aktiivisia. Luokittelemattomat uutiset näkyvät oletusnäkymässä.
- Lähde ja aiheet tallentuvat laitteelle ja palautuvat käynnistyksessä; uutisten päivitys ei nollaa valintoja. Tyhjä aihevalinta näyttää tyhjän tilan.
- Valitse kaikki- ja Palauta oletukset -toiminnot sekä tiivis suodatusyhteenveto.
- Digi-kategoria ja varmistetut Iltalehden Bitti- sekä Ilta-Sanomien Digitoday-syötteet. Yleis- ja aihekohtaisen syötteen artikkelit yhdistetään lähteen ja GUID:n avulla, kategoriat säilyttäen.

### Muutokset
- Lämmin harmaanvihreä listatausta, lähes valkoiset kortit ja 1 dp reunus ilman varjoa.
- Sävytetty yläosa, tumma vihreä typografinen tunnus ja pieni graafinen piste. Slogan, päivitysaika ja toiminnot yhdessä; toimintorivi voi rivittyä.
- Urheilun ja viihteen kategoriakartoitusta täydennetty syötteissä havaituilla nimillä.
- APK-julkaisu hakee saman commitin hyväksytyn buildin ja varmentaa allekirjoituksen, applicationId:n sekä versionumerot. Vanhan APK:n julkaiseminen uutena versiona estetään.

### Tarkistukset
- Bitti-, Digitoday- ja molemmat yleissyötteet tarkistettu 5.11.2026: HTTP 200, kategoriat, kuvaukset, kuvat ja tunnisteet.
- Kohdennetut testit oletuksille, monivalinnalle, lähdeyhdistelmille, tallennusmuodon palautukselle, tyhjälle valinnalle, Digi-metatiedoille ja syötteiden päällekkäisyyksille. CI-varmistus tehdään ennen APK-julkaisua.
- Ei paikallista Android-buildia, emulaattoria tai laitetestiä. Ulkoasu tarkistettu koodista.

### Tiedossa olevat puutteet
- APK on aiemman käytännön mukaisesti testiallekirjoitettu debug-paketti. Version 0.1.0 päälle asentaminen ei välttämättä onnistu eri allekirjoituksen vuoksi; sovelluksen poistaminen poistaa datan ja valinnat. Saman avaimen tavalliset päivitykset säilyttävät tallennetut valinnat.
- Kategoriakartoitus on rajallinen. Luokittelemattomat näkyvät oletuksilla ja kaikkien aiheiden valinnalla, eivät yksittäisten aiheiden rajauksissa. Urheiluksi merkitty uutinen piilotetaan aina, jos Urheilu ei ole aktiivinen.
- Aiemmat lähteiden käyttöehtojen ja offline-kuvien rajoitteet ovat edelleen voimassa.

## 0.1.0 – 5.11.2026

### Lisäykset
- Käyttäjän pyynnöstä asennettavan debug-APK:n julkaisu GitHub Releasesiin ilman allekirjoitussalaisuuksien käyttöönottoa; käytetään jo tarkistettua pakettia.
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
- [Actions-ajo 41977184639](https://github.com/Tenuxi/Uutishetki/actions/runs/41977184639) onnistui: kahdeksan JUnit-testiä, Android Lint ja debug-APK-käännös.
- Lint: 0 virhettä ja 10 varoitusta (uudempia riippuvuus-/SDK-versioita, Android 12:n dataExtractionRules ja KTX-tyylisuositukset). Tarkistuksia ei poistettu tai vaiennettu.
- [Ladattava debug-testiversio](https://github.com/Tenuxi/Uutishetki/actions/runs/41977184639/artifacts/13392685128), ei pysyvällä julkaisuavaimella allekirjoitettu release.
- Paikallista Android-käännöstä ei ajettu: ympäristössä ei ole JDK:ta tai Android SDK:ta.
- Ei emulaattori- tai fyysisen laitteen testiä. Testiallekirjoitetun APK:n julkaisu ja allekirjoitus varmennettu [julkaisuajossa 41981111738](https://github.com/Tenuxi/Uutishetki/actions/runs/41981111738). [Lataa APK](https://github.com/Tenuxi/Uutishetki/releases/download/v0.1.0/Uutishetki-0.1.0.apk).

### Tiedossa olevat puutteet
- Ylen alueuutisissa ei havaittu kuvia; Yle Häme tarkoittaa Kanta-Hämettä.
- Syötteiden rajalliset uutisikkunat ja epätäydellinen kategoriakartoitus; luokittelemattomat näkyvät Kaikki-näkymässä.
- Sanoman RSS-erityisehtojen sivut palauttivat 403; erityisehtoja ei voitu varmistaa. Käyttö rajattu yksityiseen RSS-lukemiseen, ei julkista sisältöpalvelua.
- Offline-kuvien saatavuutta ei taata. Kokonaisten artikkelien offline-lukua ei ole.
- Nykyinen ladattava APK käyttää testiallekirjoitusta. Pysyvään release-avaimeen siirtyminen voi vaatia sovelluksen poistamisen; pysyvän julkaisuavaimen käyttöönotto on erillinen jatkotoimi.
