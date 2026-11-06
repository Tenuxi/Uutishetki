# Muutosloki

## 0.4.0 – 6.11.2026

- Sää näkyy uudella asennuksella heti Helsingin säällä. Yläpalkissa paikkakunta, sääkuvake ja lämpötila tai lataustila.
- Automaattinen sijainti oletuksena; ensikäynnistyksen perustelu ja käyttäjän käynnistämä Android-lupakysely. Luvan epäämistä ei kysytä uudelleen automaattisesti. Käsivalinta ohittaa paikannuksen, aiemmat asetukset säilyvät.
- Lähialueen liikennetiedotteet Fintrafficin Digitraffic v2 -rajapinnasta. Oletuksena päällä, säde 10 km; valinnat 5/10/25/50 km ja pois-kytkentä tallennetaan. Helsinki on vain sään varapaikka, ei liikenteen oletussijainti.
- Hillityt punertavat tiedotekortit uutisten sekaan julkaisuajan mukaan; lisätiedoissa alkuperäiset ohjeet, päivitysaika, lähteen linkki ja lisenssi. Näkyvyys ei riipu uutissuodattimista.
- Etäisyys lähimpään todellisen piste-, viiva- tai aluegeometrian kohtaan. Päättyneet ja perutut poistuvat, uudet versiot korvaavat vanhat. Puuttuvista sijainneista ei keksitä etäisyyksiä.
- Viiden minuutin liikennevälimuisti ja erilliset haut; hakuhäiriö piilottaa varmentamattomat tiedotteet. Ei taustaseurantaa, sijaintihistoriaa tai ilmoituksia.
- [Tietolähteet, ehdot ja rajoitteet](docs/WEATHER_AND_ALERTS.md) päivitetty. Tilannehuone-suunnitelma korvattu Digitrafficilla. [Actions 42176131959](https://github.com/Tenuxi/Uutishetki/actions/runs/42176131959) läpäisi 34 testiä (11 liikenne-, 8 sää- ja 15 nykyistä uutis-/suodatustestiä), Android Lintin ja APK-käännöksen. Ensimmäisen ajon käännösvirhe korjattu ennen onnistunutta ajoa. [Julkaisu 42176564380](https://github.com/Tenuxi/Uutishetki/actions/runs/42176564380) varmisti allekirjoituksen, applicationId:n, version 0.4.0 ja versionCoden 6. [Lataa APK](https://github.com/Tenuxi/Uutishetki/releases/download/apk-0.4.0/Uutishetki-0.4.0.apk). Ei emulaattoria tai paikallista Android-buildia.
- Nykyinen testiallekirjoituspolku säilyy; vanhan avaimen puuttuessa aiempi asennus voi vaatia poistamisen paikallisine tietoineen.

## 0.3.0 – 6.11.2026

- Vapaaehtoinen paikallinen sää yläpalkkiin: sääkuvake, lämpötila ja napautettavat lisätiedot lähdemerkintöineen.
- Sivupaneelissa sään päälle/pois-valinta, automaattinen likimääräinen sijainti ja paikkakunnan haku käsin. Asetukset säilyvät käynnistysten välillä.
- Sijaintilupa pyydetään vain käyttäjän valinnasta. Ei taustapaikannusta tai sijaintihistoriaa. Sää haetaan uutisista riippumatta 15 minuutin välein; vanhentunut tai epäonnistunut tieto ei näytä ajantasaiselta.
- Open-Meteon maksuton ei-kaupallinen säärajapinta ja GeoNames-paikkakuntahaku. Lähteet, käyttöehdot, päivitysvälit ja rajoitteet: [selvitys](docs/WEATHER_AND_ALERTS.md).
- Hälytysintegraatio jäi toteuttamatta: sopivaa avointa rajapintaa, uudelleenjulkaisuoikeutta ja riittävän tarkkoja tapahtumapaikkoja ei voitu varmistaa. Paneelissa tieto esteestä ja linkki Tilannehuone.fi:hin; ei keksittyjä hälytyksiä.
- Seitsemän kohdennettua säälogiikan testiä. Ei paikallista Android-buildia tai emulaattoria. Laitteen lupadialogi, paikannus, kosketukset ja suuret tekstikoot jäävät laitteella tarkistettaviksi. [Actions-ajo 42173109551](https://github.com/Tenuxi/Uutishetki/actions/runs/42173109551) läpäisi kaikki 22 testiä, Android Lintin ja APK-käännöksen. [Julkaisuajo 42173372593](https://github.com/Tenuxi/Uutishetki/actions/runs/42173372593) varmisti allekirjoituksen ja version 0.3.0 / versionCode 5. [Lataa APK](https://github.com/Tenuxi/Uutishetki/releases/download/apk-0.3.0/Uutishetki-0.3.0.apk).
- ApplicationId ja nykyinen julkaisutyönkulku säilyvät. Testiallekirjoituksen vaihtuminen voi edellyttää aiemman asennuksen poistamista ja poistaa sen paikalliset tiedot.

## 0.2.2 – 5.11.2026

### Muutokset
- Hampurilaisvalikko avaa oikealta liukuvan valkoisen sivupaneelin pudotusvalikon ja erillisen suodatusnäkymän sijaan. Uutisnäkymä himmenee paneelin taustalla.
- Lehdet valitaan leveistä painikkeista ja aiheet pyöristetyistä monivalintapainikkeista. Valittu vaihtoehto erottuu siniharmaasta taustasta ja valintamerkistä.
- Valinnat päivittävät listaa heti sulkematta paneelia. Nykyinen lähdevalinta, monivalinta ja käynnistysten välillä säilyvät asetukset ovat ennallaan.
- Valitse kaikki ja Palauta oletukset ovat suoraan paneelissa. Tietoa, versionumero ja päivitysaika ovat vieritettävän sisällön alaosassa.
- Paneeli sulkeutuu sulkemiskuvakkeella, taustan napautuksella, paluutoiminnolla tai pyyhkäisyllä. Kiinteä sulkemispainike, turva-alueet, vähintään 48 dp kosketusalueet, vieritys ja rivittyvät tekstit huomioitu.
- Muu uutisnäkymä, uutislähteet, suodatuslogiikka ja applicationId eivät muutu.

### Tarkistukset
- Koodista tarkistettu paneelin avaus ja sulkemisreitit, heti vaikuttavat valinnat ilman sulkemista, nykyisen ViewModelin tallennuskutsut sekä footerin pysyminen samassa vieritettävässä sisällössä.
- Käytetyt drawer-API:t tarkistettu käytössä olevan Material 3 -version lähdekoodista. XML-resurssit ja muutoskuvauksen rakenne tarkistettu paikallisesti.
- [Actions-ajo 42013418958](https://github.com/Tenuxi/Uutishetki/actions/runs/42013418958) läpäisi 15 nykyistä testiä, Android Lintin ja APK-käännöksen. Ei uusia ulkoasuyksikkötestejä tai paikallista Android-buildia.
- [Julkaisuajo 42013755129](https://github.com/Tenuxi/Uutishetki/actions/runs/42013755129) onnistui ja varmisti allekirjoituksen, applicationId:n, version 0.2.2 sekä versionCoden 4. [Lataa APK](https://github.com/Tenuxi/Uutishetki/releases/download/apk-0.2.2/Uutishetki-0.2.2.apk).
- Ei emulaattori- tai laitetestiä. Paneelin animaatio, kosketukset, TalkBack, suuri fonttikoko ja pieni näyttö jäävät laitteella varmistettaviksi.

### Rajoitteet
- APK julkaistaan nykyisellä testiallekirjoituspolulla. Eri avaimen vuoksi aiempi asennus voi vaatia poistamisen, mikä poistaa paikalliset tiedot ja valinnat. Sovelluksen valintojen tallennuslogiikka säilyy ennallaan.

## 0.2.1 – 5.11.2026

### Muutokset
- Kompakti, vieritettäessä paikallaan pysyvä yläpalkki: musta Roboto-nimi, 48 dp:n hampurilaispainike, hieman läpikuultava vaalea tausta ja ohut tumma alareuna.
- Suodattimet, tiedot, versionumero ja viimeisin päivitysaika löytyvät valikosta. Päänäkymän slogan, erilliset toimintopainikkeet ja suodatusyhteenveto poistettu tilavarauksineen.
- Listan alkuväli seuraa palkin mitattua korkeutta ja Androidin turva-alueita. Päivityksen ilmaisin näkyy palkin alapuolella.
- Neutraali vaaleanharmaa listatausta, valkoiset kortit, hillitty sinertävä korostus ja 2 dp:n pehmeä korttivarjo. Ohut reunus ja pyöristetyt kulmat säilyvät.
- Mustavalkoinen vektorimuotoinen käynnistyskuvake mukautuvilla rajauksilla ja Android 13+:n teemakuvaketuella.
- Uutislähteet, suodatuslogiikka, tallennus ja applicationId ovat ennallaan.

### Tarkistukset
- Valikon avaus/sulkeminen, reitit suodattimiin ja tietoihin, päivitysaika, 48 dp kosketusalue ja listan yläväli tarkistettu koodista.
- XML-resurssien rakenne tarkistettu. Ei uusia ulkoasuyksikkötestejä eikä paikallista Android-buildia; [Actions-ajo 42011336514](https://github.com/Tenuxi/Uutishetki/actions/runs/42011336514) läpäisi 15 nykyistä testiä, Android Lintin ja APK-käännöksen.
- [Julkaisuajo 42011735261](https://github.com/Tenuxi/Uutishetki/actions/runs/42011735261) onnistui; APK:n allekirjoitus, applicationId, versionName 0.2.1 ja versionCode 3 varmennettu. [Lataa APK](https://github.com/Tenuxi/Uutishetki/releases/download/apk-0.2.1/Uutishetki-0.2.1.apk).
- Ei emulaattori- tai laitetestiä: todellinen vieritys, läpikuultavuus, suuret fontit ja käynnistyskuvakkeen laitekohtainen rajaus jäävät laitteella varmistettaviksi.

### Asentaminen ja rajoitteet
- Version 0.2.0 alkuperäistä allekirjoitusavainta ei ole tallessa. Käyttäjä hyväksyi erillisen asennuksen vanhan päälle asentuvan päivityksen sijaan. APK julkaistaan nykyisellä testiallekirjoituspolulla; vanhan sovelluksen poistaminen voi olla tarpeen ja poistaa samalla paikallisen datan sekä valinnat.
- Sovellus itsessään ei nollaa tallennettuja valintoja. Niiden säilymistä poistamisen yli ei voida luvata. Pysyvän avaimen käyttöönotto jää erilliseksi jatkotoimeksi.

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
- Kohdennetut testit oletuksille, monivalinnalle, lähdeyhdistelmille, tallennusmuodon palautukselle, tyhjälle valinnalle, Digi-metatiedoille ja syötteiden päällekkäisyyksille. [Actions-tarkistus 41982653524](https://github.com/Tenuxi/Uutishetki/actions/runs/41982653524) onnistui: 15 testiä, Android Lint ja APK-käännös.
- [Julkaisuajo 41982979155](https://github.com/Tenuxi/Uutishetki/actions/runs/41982979155) varmisti APK:n allekirjoituksen, applicationId:n, version 0.2.0 ja versionCoden 2. [Lataa APK](https://github.com/Tenuxi/Uutishetki/releases/download/apk-0.2.0/Uutishetki-0.2.0.apk).
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
