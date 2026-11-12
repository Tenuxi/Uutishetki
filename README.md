# Uutishetki

Suomenkielinen Android-uutislukija omaan ja pienen ystäväpiirin käyttöön. Kotlin ja Jetpack Compose, Android 8.0 tai uudempi. Pysyvä applicationId: `fi.uutishetki`. Versio **0.6.0**, versionCode **9**.

Uutiset näkyvät uusimmat ensin yhteisessä virrassa. Lähdesuodatin ja pysyvä aiheiden monivalinta (oletuksena kaikki paitsi Urheilu), lyhyet RSS-kuvaukset, syötteen kuvat ja alkuperäisten artikkelien avaaminen Custom Tabsissa / selaimessa. Ei mainos-, analytiikka- tai AI-SDK:ita, tilejä eikä palvelinta. Julkaisijoiden sivujen mainoksia, evästeitä ja maksumuureja ei muuteta.

## Valmis APK

[Lataa Uutishetki 0.6.0 release-APK](https://github.com/Tenuxi/Uutishetki/releases/download/apk-0.6.0/Uutishetki-0.6.0.apk). Julkaistu 12.11.2026 samalla pysyvällä release-avaimella kuin 0.5.1. [Julkaisun tarkistukset](https://github.com/Tenuxi/Uutishetki/actions/runs/43168340272) onnistuivat. [Uusin julkaisu](https://github.com/Tenuxi/Uutishetki/releases/latest).

**Siirtyminen vanhasta debug-asennuksesta vaatii poistamisen kerran, jolloin paikalliset tiedot ja asetukset häviävät.** Tästä versiosta alkaen sama julkaisuavain ja kasvava versionCode mahdollistavat tulevat päivitykset vanhan päälle. Päivitysasennusta ei ole testattu fyysisellä laitteella. [Allekirjoitus ja varmuuskopiointi](docs/SIGNING.md).

## Rakentaminen ja asentaminen

Avaa projekti Android Studiossa tai asenna JDK 17 ja Android SDK 35 (Build Tools 35.0.0). Aseta `ANDROID_HOME` tai SDK-polku `local.properties`-tiedostoon. Gradle wrapper on mukana.

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Varsinaiset buildit tekee [GitHub Actions](https://github.com/Tenuxi/Uutishetki/actions). Main-haara ja PR:t rakentavat debug-APK:n Actions-artifactiksi. Se on kehitysversio, ei pysyvällä avaimella allekirjoitettu julkaisu; älä käytä sitä ystäville jaettavana päivitysketjuna.

Uusi release-APK löytyy onnistuneen tagityön jälkeen [Releases-sivulta](https://github.com/Tenuxi/Uutishetki/releases). Lataa puhelimeen julkaistun version APK, salli tarvittaessa APK:n avaavalle selaimelle/tiedostonhallinnalle sovellusten asentaminen ja avaa tiedosto. Uusi release asennetaan vanhan päälle. Debug-versiosta siirtyminen releaseen vaatii debug-version poistamisen (paikalliset uutiset häviävät). Yksityisen repon lataukset edellyttävät repo-oikeuksia; APK:n voi jakaa erikseen.

## Pysyvä allekirjoitus ja julkaisu

Version 0.5.1 julkaisuketju käyttää yhtä pysyvää release-keystorea ja lukittua SHA-256-varmennetta. Debug-APK:ta ei julkaista, eivätkä puuttuvat salaisuudet käynnistä varavaihtoehtoa. [Selvitys, Secrets-käyttöönotto, turvallinen varmuuskopiointi ja palautus](docs/SIGNING.md).

Vanhoissa 0.2.0-, 0.4.0- ja 0.5.0-APK:issa varmenteet olivat eri, vaikka applicationId pysyi samana ja versionCode kasvoi. Uuteen pysyvään avaimeen siirtyminen vaatii vanhan asennuksen poistamisen kerran, mikä poistaa paikalliset tiedot. Sen jälkeen samalla avaimella julkaistavat suuremman versionCoden APK:t tukevat päivitystä vanhan päälle. Laitetestausta ei ole tehty.

Julkaise jatkossa päivittämällä versio/muutosloki, commit ja push main-haaraan. Odota main-tarkistuksen onnistuminen ja tagita sama commit `apk-<version>`. Julkaisu rakentaa release-APK:n ja varmentaa sen avaimen sekä pakettimetatiedot ennen Releasesiin siirtämistä. Käytä aina samaa avainta ja aliasta.

## Uutislähteet ja rajoitteet

Katso [lähdeselvitys](docs/SOURCES.md), jossa ovat tarkistetut URL:t, metatiedot, ehdot ja kategoriakartoitus (alkuperäiset lähteet 5.11.2026, Stara ja Pelaaja 12.11.2026). Seitsemän lehden käytetyt syötteet ovat vastanneet HTTP 200 ja kelvollisella RSS:llä. Yle Häme käyttää **Kanta-Hämeen** syötettä. Ylen tarkistetuissa alueuutisissa ei ollut kuvia. Iltalehden kokoomasyöte on vain 20 uutisen ikkuna, joten aiheita voi puuttua. Sovellus ei täydennä kuvia tai tekstejä artikkelisivuja kaapimalla.

Tämä on laitteessa toimiva yksityinen RSS-lukija, ei sisällön uudelleenjulkaisupalvelu. Julkisen tai kaupallisen jakelun oikeuksia ei ole hankittu. Sanoman RSS-erityisehtojen sivujen lukeminen estyi HTTP 403:een; niiden ajantasaisia erityisehtoja ei ole varmistettu. Syötteen saatavuus ei itsessään ole uudelleenjulkaisulupa.

## Tallennus ja verkko

SQLite tallentaa enintään 300 uutista/lähde (yhteensä 1 500); onnistuneen päivityksen yhteydessä poistetaan yli 14 päivää sitten viimeksi syötteessä nähdyt rivit. Syötevirhe ei tyhjennä kantaa. Ei kokonaisia artikkeleita. Coil-kuvavälimuisti enintään 80 MiB, HTTP-välimuisti 8 MiB ja kuvien muistivälimuisti 15 % sovelluksen muistista. Android voi poistaa välimuistikuvat.

Tallennetut uutiset luetaan IO-säikeellä ennen hakua. Viisi lähdettä haetaan rinnakkain ja tallennetaan erillisinä transaktioina. Yhteys 10 s, lukeminen 15 s ja koko pyyntö 25 s; automaattiset verkkouusinnat pois. Syötevastaus enintään 2 MiB. Automaattinen päivitys etualalle tullessa aikaisintaan 5 minuutin välein, käsin vähintään 10 sekunnin välein. Palvelimen HTTP-välimuisti sallitaan 5 minuutiksi myös käsin päivitettäessä. Ei taustatyöajastinta. Vakaat lähde + GUID -avaimet (varalla linkki ilman queryä) estävät toistot ja Compose-listan avaimet säilyttävät näkyvän uutisen selauskohdan.

Syötteet ja kuvat haetaan suoraan julkaisijoilta; ne saavat normaalit verkkopyyntötiedot kuten IP-osoitteen. Pilvivarmuuskopiointi on poistettu käytöstä (`allowBackup=false`); Android 12+:n valmistajakohtaista laitteesta toiseen -siirtoa ei ole erikseen rajattu dataExtractionRules-säännöillä. Tekstin koko seuraa Androidin asetusta, suodatuspaneeli vierii ja teaser rajautuu kahdelle riville.

## Tarkistukset

JUnit testaa RSS-kentät, kuvat, HTML-siivouksen, päivämäärät, tunnisteet, URL-rajoituksen ja yhdistetyt suodattimet. GitHub Actions ajaa testit, Android Lintin ja APK-käännöksen. [Manuaalinen tarkistuslista](docs/TESTING.md) kattaa laitteen käyttötilanteet. Emulaattoria ei käynnistetä oletuksena. Katso version toteutunut tarkistustilanne muutoslokista; koodin valmistuminen ei tarkoita, että laitetestaus tai allekirjoitettu julkaisu olisi tehty.


## Ulkoasu 0.2.1

Kiinteän, läpikuultavan Roboto-yläpalkin hampurilaisvalikko avaa suodattimet ja tiedot sekä näyttää päivitysajan. Uutislista alkaa heti palkin alta; valinnat näkyvät vain suodatuspaneelissa. Harmaa listatausta, valkoiset kevyesti varjostetut kortit ja mustavalkoinen mukautuva kuvake korvaavat aiemman vihertävän ilmeen. Lähteet, suodatus ja tallennetut asetukset ovat ennallaan.

Roboto Regular on ladattu [Google Fontsin Roboto-lähdereposta](https://github.com/googlefonts/roboto-2/blob/main/src/hinted/Roboto-Regular.ttf), lisenssi [Apache 2.0](docs/Roboto-LICENSE.txt); lisenssi on mukana myös APK:n assets/licenses-hakemistossa. Nimen kirjasin ei riipu puhelinvalmistajan oletusfontista.

Vanhan 0.2.0-version allekirjoitusavain puuttuu. Version 0.2.1 julkaisu tehdään käyttäjän erikseen hyväksymänä uutena asennuksena; vanhan version poistaminen hävittää paikalliset uutiset ja valinnat.


## Sivupaneeli 0.2.2

Hampurilainen avaa oikealta liukuvan paneelin, jossa Lehdet ja Aiheet ovat suoraan valittavissa. Suodatus päivittyy sulkematta paneelia ja nykyinen valintojen tallennus säilyy. Paneelissa ovat myös Valitse kaikki, Palauta oletukset, Tietoa, versionumero ja päivitysaika. Sulje X:stä, himmennetystä taustasta tai Androidin paluutoiminnolla. Koko valintasisältö alaosan tietoineen vierii pienillä näytöillä.

## Sää ja lähialueen liikennetiedotteet (0.4.0)

Uudella asennuksella Helsingin sää näkyy heti. Automaattinen sijainti vaihtaa sään käyttäjän lähelle luvan jälkeen; paikkakunnan voi valita käsin. Aiemmat asetukset säilyvät. Digitrafficin liikenteen häiriötiedotteet näkyvät punertavina kortteina uutisten joukossa; oletussäde 10 km. Ilman sijaintia tai omaa paikkakuntavalintaa liikennetiedotteet odottavat, eivät näytä Helsinkiä käyttäjän lähialueena. Sää ja liikenne voidaan kytkeä pois erikseen. Ei taustapaikannusta tai ilmoituksia. [Tietolähteet, käyttöehdot ja rajoitteet](docs/WEATHER_AND_ALERTS.md).

## Version 0.5.0 täydennykset

Säästä avautuu myös huomisen ja ylihuomisen ennuste. Uutisvirrassa on Suomen aikaan perustuvat päiväotsikot. Varsinainen avaus päivittää ja siirtää listan alkuun kerran, ellei käyttäjä aloita selaamista; artikkelista paluu säilyttää lukukohdan. Vanha tallennettu sään piilotusvalinta säilyy, puuttuva valinta tarkoittaa oletuksena näkyvää säätä.
