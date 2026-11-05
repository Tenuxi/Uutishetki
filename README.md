# Uutishetki

Suomenkielinen Android-uutislukija omaan ja pienen ystäväpiirin käyttöön. Kotlin ja Jetpack Compose, Android 8.0 tai uudempi. Pysyvä applicationId: `fi.uutishetki`. Versio **0.1.0**, versionCode **1**.

Uutiset näkyvät uusimmat ensin yhteisessä virrassa. Lähde- ja aihesuodattimet, lyhyet RSS-kuvaukset, syötteen kuvat ja alkuperäisten artikkelien avaaminen Custom Tabsissa / selaimessa. Ei mainos-, analytiikka- tai AI-SDK:ita, tilejä eikä palvelinta. Julkaisijoiden sivujen mainoksia, evästeitä ja maksumuureja ei muuteta.

## Rakentaminen ja asentaminen

Avaa projekti Android Studiossa tai asenna JDK 17 ja Android SDK 35 (Build Tools 35.0.0). Aseta `ANDROID_HOME` tai SDK-polku `local.properties`-tiedostoon. Gradle wrapper on mukana.

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Varsinaiset buildit tekee [GitHub Actions](https://github.com/Tenuxi/Uutishetki/actions). Main-haara ja PR:t rakentavat debug-APK:n Actions-artifactiksi. Se on kehitysversio, ei pysyvällä avaimella allekirjoitettu julkaisu; älä käytä sitä ystäville jaettavana päivitysketjuna.

Julkaisu-APK löytyy onnistuneen tagityön jälkeen [Releases-sivulta](https://github.com/Tenuxi/Uutishetki/releases). Lataa puhelimeen `Uutishetki-0.1.0.apk`, salli tarvittaessa APK:n avaavalle selaimelle/tiedostonhallinnalle sovellusten asentaminen ja avaa tiedosto. Uusi release asennetaan vanhan päälle. Debug-versiosta siirtyminen releaseen vaatii debug-version poistamisen (paikalliset uutiset häviävät). Yksityisen repon lataukset edellyttävät repo-oikeuksia; APK:n voi jakaa erikseen.

## Pysyvä allekirjoitusavain — kertaluonteinen käyttöönotto

Allekirjoitusavainta ei luoda automaattisesti workflow'ssa eikä tallenneta repoon. Luo avain omalla luotetulla koneellasi **repon ulkopuolelle**, ja säilytä salattu varmuuskopio avaimesta sekä salasanoista. Käytä samaa avainta jokaisessa versiossa.

```sh
keytool -genkeypair -v -keystore "$HOME/uutishetki-release.jks" \
  -alias uutishetki -keyalg RSA -keysize 3072 -validity 10000
```

Keytool kysyy salasanat vuorovaikutteisesti. Aseta repon **Settings → Secrets and variables → Actions → New repository secret**:

| Secret | Arvo |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | Avaintiedoston base64-koodaus |
| `ANDROID_KEYSTORE_PASSWORD` | Avaintiedoston salasana |
| `ANDROID_KEY_ALIAS` | `uutishetki` |
| `ANDROID_KEY_PASSWORD` | Avainkohtainen salasana (PKCS12-avaimella yleensä sama kuin tiedoston) |

GitHub CLI:llä base64-salaisuuden voi syöttää tulostamatta sitä ruudulle:

```sh
base64 -w 0 "$HOME/uutishetki-release.jks" | gh secret set ANDROID_KEYSTORE_BASE64 --repo Tenuxi/Uutishetki
gh secret set ANDROID_KEYSTORE_PASSWORD --repo Tenuxi/Uutishetki
gh secret set ANDROID_KEY_ALIAS --repo Tenuxi/Uutishetki
gh secret set ANDROID_KEY_PASSWORD --repo Tenuxi/Uutishetki
```

Kolme viimeistä komentoa kysyvät arvon. macOS:ssä jätä `base64 -w 0` -komennosta `-w 0` pois. Älä liitä avainta tai salasanoja chattiin, lokiin tai committiin. GitHubin automaattinen `GITHUB_TOKEN` riittää releaseen; henkilökohtaista julkaisutokenia ei tarvita. Repon on sallittava Actions ja workflow'n `contents: write` -oikeus julkaisu-jobille.

## Uuden version julkaiseminen

1. Tee yhtenäinen muutoskokonaisuus. Päivitä `version.properties`: korjaus `0.1.1`, ominaisuus `0.2.0`, aina suurempi versionCode.
2. Päivitä suomenkielinen `CHANGELOG.md`, myös tarkistukset ja tunnetut puutteet. Aja tarpeelliset kohdennetut tarkistukset; CI hoitaa käännöksen, parseri-/suodatustestit ja lintin.
3. Commitoi versionumero viestissä ja pushaa main. Odota Actionsin onnistumista.
4. Kun allekirjoitussalaisuudet on asetettu, tee tagi samalle tarkistetulle commitille:

```sh
git tag -a v0.1.0 -m 'Uutishetki 0.1.0'
git push origin v0.1.0
```

Tagi käynnistää tarkistukset ja allekirjoitetun APK:n julkaisun. Workflow tarkistaa tagin, versionCoden kasvun, muutoskuvauksen ja APK:n allekirjoituksen. Releaseen liitetään kyseisen version muutoskuvaus ja SHA-256. Tavalliset commitit eivät julkaise, eikä workflow tee committeja tai tageja. Jos salaisuuksia puuttuu, julkaisu epäonnistuu selkeään virheeseen; lisää salaisuudet ja aja epäonnistuneet jobit uudelleen. Älä siirrä julkaistuja tageja tai vaihda avainta.

## Uutislähteet ja rajoitteet

Katso [lähdeselvitys](docs/SOURCES.md), jossa ovat 5.11.2026 tarkistetut URL:t, metatiedot, ehdot ja kategoriakartoitus. Kaikki viisi lähdettä vastasivat HTTP 200 ja kelvollisella RSS:llä. Yle Häme käyttää **Kanta-Hämeen** syötettä. Ylen tarkistetuissa alueuutisissa ei ollut kuvia. Iltalehden kokoomasyöte on vain 20 uutisen ikkuna, joten aiheita voi puuttua. Sovellus ei täydennä kuvia tai tekstejä artikkelisivuja kaapimalla.

Tämä on laitteessa toimiva yksityinen RSS-lukija, ei sisällön uudelleenjulkaisupalvelu. Julkisen tai kaupallisen jakelun oikeuksia ei ole hankittu. Sanoman RSS-erityisehtojen sivujen lukeminen estyi HTTP 403:een; niiden ajantasaisia erityisehtoja ei ole varmistettu. Syötteen saatavuus ei itsessään ole uudelleenjulkaisulupa.

## Tallennus ja verkko

SQLite tallentaa enintään 300 uutista/lähde (yhteensä 1 500); onnistuneen päivityksen yhteydessä poistetaan yli 14 päivää sitten viimeksi syötteessä nähdyt rivit. Syötevirhe ei tyhjennä kantaa. Ei kokonaisia artikkeleita. Coil-kuvavälimuisti enintään 80 MiB, HTTP-välimuisti 8 MiB ja kuvien muistivälimuisti 15 % sovelluksen muistista. Android voi poistaa välimuistikuvat.

Tallennetut uutiset luetaan IO-säikeellä ennen hakua. Viisi lähdettä haetaan rinnakkain ja tallennetaan erillisinä transaktioina. Yhteys 10 s, lukeminen 15 s ja koko pyyntö 25 s; automaattiset verkkouusinnat pois. Syötevastaus enintään 2 MiB. Automaattinen päivitys etualalle tullessa aikaisintaan 5 minuutin välein, käsin vähintään 10 sekunnin välein. Palvelimen HTTP-välimuisti sallitaan 5 minuutiksi myös käsin päivitettäessä. Ei taustatyöajastinta. Vakaat lähde + GUID -avaimet (varalla linkki ilman queryä) estävät toistot ja Compose-listan avaimet säilyttävät näkyvän uutisen selauskohdan.

Syötteet ja kuvat haetaan suoraan julkaisijoilta; ne saavat normaalit verkkopyyntötiedot kuten IP-osoitteen. Pilvivarmuuskopiointi on poistettu käytöstä (`allowBackup=false`); Android 12+:n valmistajakohtaista laitteesta toiseen -siirtoa ei ole erikseen rajattu dataExtractionRules-säännöillä. Tekstin koko seuraa Androidin asetusta, suodatuspaneeli vierii ja teaser rajautuu kahdelle riville.

## Tarkistukset

JUnit testaa RSS-kentät, kuvat, HTML-siivouksen, päivämäärät, tunnisteet, URL-rajoituksen ja yhdistetyt suodattimet. GitHub Actions ajaa testit, Android Lintin ja APK-käännöksen. [Manuaalinen tarkistuslista](docs/TESTING.md) kattaa laitteen käyttötilanteet. Emulaattoria ei käynnistetä oletuksena. Katso version toteutunut tarkistustilanne muutoslokista; koodin valmistuminen ei tarkoita, että laitetestaus tai allekirjoitettu julkaisu olisi tehty.
