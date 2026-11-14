# Laitetarkistus ennen ystäville jakamista

Tätä listaa ei ole vielä ajettu laitteella. Android 8+; lisäksi Android 15:n reunasta reunaan -asettelu.

- Ensimmäinen avaus verkossa: lataustila, kaikki viisi lähdettä, suomenkieliset ajat, kuvat ja kuvattomat kortit.
- Valitse Iltalehti + Urheilu, tyhjennä valinnat, kokeile tyhjää yhdistelmää. Valinta säilyy näytön käännössä.
- Selaa keskelle virtaa ja vedä päivitys: näkyvä uutinen ei vaihdu tarpeettomasti. Palaa selaimesta, uutisten automaattihaku ei toistu viiden minuutin sisällä.
- Avaa artikkeli: Custom Tabs tai tavallinen selain; maksumuuria ei ohiteta.
- Sulje sovellus, kytke lentotila, avaa uudelleen: tallennetut otsikot/kuvaukset heti, virhe ei tyhjennä listaa. Kuvan puuttuminen ei riko asettelua.
- Estä yhden lähteen verkkoyhteys: muut lähteet päivittyvät ja virheessä nimetään epäonnistunut lähde.
- Suuri fonttikoko, TalkBack, pieni näyttö: otsikot luettavia, suodatuspaneeli vierii, painikkeet käytettävissä.
- Asenna seuraava samalla avaimella allekirjoitettu ja suuremman versionCoden APK vanhan päälle: asennus onnistuu, paikallinen data säilyy.


## 0.2.0:n kohdennettu laitetarkistus (ei vielä ajettu)

- Ensikäynnistys: Kaikki paitsi urheilu, Digi ja luokittelemattomat mukana. Urheilu-chip lisää urheilun.
- Valitse Digi + Talous ja IS, sulje prosessi ja avaa: valinnat palaavat. Vedä päivitys: valinnat eivät muutu.
- Poista jokainen aihe: Ei aiheita valittu myös haun aikana. Valitse kaikki valitsee aiheet; Palauta oletukset palauttaa myös kaikki lähteet.
- Tarkista sama Digi-artikkeli yleis- ja lisäsyötteestä: vain yksi kortti, Digi-luokka säilyy.
- Pieni näyttö ja suuri fontti: tunnus, slogan, päivitysaika ja toimintopainikkeet luettavia. Androidin tilapalkissa tummat kuvakkeet. Kuvattoman kortin reunus erottuu.


## 0.2.1 — rajattu kooditarkistus ja laitteella tehtävät tarkistukset

Koodista tarkistettu: valikkopainike avaa DropdownMenun, suodatus/tiedot sulkevat valikon ja avaavat nykyisen paneelin/dialogin. Päivitysaika on valikossa, versio myös tietodialogissa. Suodatuksen yhteenvetoriviä ja sen listapaikkaa ei enää ole. Listan contentPadding käyttää palkin onSizeChanged-korkeutta, yläturva-aluetta ja 8 dp alkuväliä. Lista vierii palkin taakse, palkki piirretään viimeisenä. 48 dp painike, mitattu palkki ja 16 dp sivu-/korttivälit. Kuvakkeen symboli pysyy 108 dp:n adaptive-kuvakkeen keskimmäisessä turva-alueessa.

Laitteella testaamatta: avaa/sulje valikko myös takaisin-painikkeella, valitse suodattimet, avaa tiedot, tarkista päivitysaika ja vieritys palkin alta. Kokeile pientä näyttöä, suurta fonttia, vaaka-asentoa, näyttölovea sekä ele-/kolmen painikkeen navigointia. Tarkista vetopäivityksen ilmaisin ja varjojen reunat. Tarkista kuvake pyöreällä ja pyöristetyllä maskilla sekä Android 13:n teemakuvakkeella. Emulaattoria ei käynnistetty.


## 0.2.2 — sivupaneeli

Koodista tarkistettu: sama drawerState avautuu hampurilaisesta ja tyhjän aihevalinnan painikkeesta. Sulkemiskuvake kutsuu close(), Materialin scrim hoitaa taustasulkemisen ja drawerState-parametrillinen ModalDrawerSheet paluutoiminnon. Vain paneelin mekaniikka käyttää RTL-suuntaa; sisältö säilyttää normaalin suunnan. Taustan saavutettavuuspuu piilotetaan paneelin ollessa auki. Valintapainikkeet kutsuvat nykyisiä ViewModelin metodeja sulkematta paneelia. Lähde ja aiheet tallentuvat edelleen SharedPreferencesiin. Footer on LazyColumnin viimeinen kohta eikä peitä valintoja. Paneeli enintään 360 dp leveä, taustaa jää vähintään 40 dp; safeDrawing-insetit ja 48 dp painikkeet.

Laitteella testaamatta: avaa paneeli, valitse useita aiheita ja vaihda lehteä, varmista paneelin pysyvän auki. Sulje X:stä, taustasta ja paluulla. Avaa uudelleen ja käynnistä sovellus uudelleen: valinnat säilyvät. Testaa Valitse kaikki sekä Palauta oletukset. Tarkista Tietoa/versionumero/päivitysaika vierittämällä. Kokeile pientä näyttöä, suurta tekstiä, TalkBackia ja Androidin elepaluuta. Emulaattoria ei käynnistetty.

## 0.3.0 — sää

Seitsemän uutta logiikkatestiä: oletukset ja asetusten JSON-palautus, manuaalisen sijainnin toiminta ilman lupaa, automaattisen sijainnin lupavaatimus, säävastauksen ja välimuistin luku, vanha/tuleva/virheellinen tieto, paikkakuntahaun rajaukset ja koordinaattien pyöristys. Nykyiset uutistestit säilytetty. XML-rakenne ja julkaisumetatiedot tarkistetaan paikallisesti; testit, Lint ja APK GitHub Actionsissa.

Koodista tarkistettu: lupa vain automaattisen sijainnin painikkeesta, sää pois oletuksena, eri SharedPreferences kuin uutissuodattimet, näkyvyyteen sidottu peruutettava haku, 15 minuutin verkkoväli, 5 minuutin virheväli, manuaalipaikka ilman lupaa, säävirhe ei estä uutisten latausta. Mitattu yläpalkki antaa listalle tilan; sääpainikkeen leveys rajattu ja nimi joustaa.

Ei laite-/emulaattoritestiä: tarkista luvan hyväksyminen/epääminen/peruminen asetuksista, paikannus pois laitteesta, manuaalinen Helsinki-haku, lentotila, prosessin uudelleenkäynnistys, siirtyminen taustalle, sää pois/päälle, pienin näyttö ja suurin fontti, TalkBack sekä säätietojen ja paneelin vieritys. Etäisyyssuodatusta tai epätarkkoja tapahtumasijainteja ei testata, koska hälytyslähteen puuttuessa näitä toimintoja ei toteutettu.

0.3.0:n tulos: [Actions 42173109551](https://github.com/Tenuxi/Uutishetki/actions/runs/42173109551) onnistui; HTML-testiraportissa 22 testiä, 0 virhettä, 0 ohitettua (7 säätestiä ja 15 nykyistä). Android Lint ja APK-käännös onnistuivat. [Julkaisu 42173372593](https://github.com/Tenuxi/Uutishetki/actions/runs/42173372593) varmisti APK:n allekirjoituksen ja pakettimetatiedot.

## 0.4.0 — sääoletukset ja liikennetiedotteet

Kohdennetut yksikkötestit: Helsingin säävarapaikka erillään liikennesijainnista, paikannetun ja käsin valitun paikan etusija, aiemman pois-valinnan säilyminen; liikenteen asetusten palautus, etäisyys pisteeseen/viivan lähimpään kohtaan/alueeseen ja aukkoihin, puuttuva tai virheellinen geometria, alueen keskipisteen hylkääminen, päättyneet ja perutut versiot, vakaa korttiavain ja alkuperäinen julkaisuaika, välimuistin vanheneminen ja hakuhäiriö, lähteen ohjeiden säilyminen sekä varmennettu v2-vastausotos.

Koodista tarkistettu: lupa pyydetään ensikäynnistyksen perustelun Jatka-painikkeesta, päätös tallennetaan ennen Android-dialogia. Helsingin sää ja uutiset alkavat rinnalla. Lupaa ei tarvita käsivalintaan; automaattinen paikkatieto tyhjenee taustalla. Liikenne ei käytä sään varapaikkaa. Lehti-/aihevalinnat koskevat vain uutisia. Sää/paikannus/liikenne ovat erillisiä lifecycle-sidottuja coroutineja. Sijaintiasetukset ovat käytettävissä myös sään ollessa pois päältä.

Laitteella testaamatta: ensiasennuksen perustelu ja Android-lupa (hyväksy/epää/peru asetuksista), paikannuksen onnistuminen ja aikakatkaisu, käsipaikan haku, prosessin uudelleenkäynnistys, offline/online, paneelin ja tiedotedialogin vieritys, TalkBack, pienet näytöt ja suuret fontit. Ei emulaattoria, paikallista Android-buildia tai ulkoasuyksikkötestejä.

0.4.0:n toteutuneet tarkistukset: ensimmäisen Actions-ajon säätietodialogin käännösvirhe korjattiin. [Korjattu ajo 42176131959](https://github.com/Tenuxi/Uutishetki/actions/runs/42176131959) onnistui: 34 testiä, 0 virhettä, 0 ohitettua; Android Lint ja APK-käännös läpi. [Julkaisuajo 42176564380](https://github.com/Tenuxi/Uutishetki/actions/runs/42176564380) varmisti allekirjoituksen ja pakettimetatiedot. Laitetarkistuksia ei ajettu.

## 0.5.0 — ennuste, päiväryhmät ja avaus

Kohdennetut testit: puuttuvan/vanhan/nimenomaisen säävalinnan palautus, huominen/ylihuominen ja välimuistin palautus, puuttuvat ennustearvot ja väärät yksiköt; Suomen kesä-/talviajan päivärajat ja vaihtuvat Tänään/Eilen-otsikot; yhden avauksen kertasiirto, käyttäjän peruutus, artikkelista paluu, prosessin palautus selaimesta ja vanhan haun valmistuminen uuden avauksen aikana.

Koodista tarkistettu: MainActivityn start/stop muodostaa avauskerran, ei jokaista resume-tapahtumaa. Ulos sovelluksesta avattu linkki merkitään ennen avausta ja merkki säilyy ViewModelissa sekä instanssin tallennustilassa. Sää/valikko ovat saman Activityn näkymiä. Avauksen haku säilyttää välimuistin ja odottaa jo käynnissä olevan haun mutexia; myöhempi päivitys ei anna uutta siirtolupaa. Käyttäjän vieritys peruu luvan nested-scroll-yhteydestä. Päiväotsikot ovat tavallisia LazyColumn-kohtia, eivät kiinteitä palkkeja.

Laitteella testaamatta: todellinen selaimen/Custom Tabsin paluu, Home/viimeisimmät sovellukset, eleet haun aikana, näytön kääntö ja prosessin tappaminen selaimessa, TalkBack-vieritys, keskiyön ylitys avoimessa sovelluksessa, ennustedialogin vieritys ja suuret fontit. Ei emulaattoria tai paikallista Android-buildia.

0.5.0:n tulos: [Actions 42770435779](https://github.com/Tenuxi/Uutishetki/actions/runs/42770435779) läpäisi 44 testiä, 0 virhettä ja 0 ohitettua (3 uutta ennustetestiä ja 7 avaus-/päiväryhmätestiä). Android Lint ja APK-käännös onnistuivat. [Julkaisu 42770826633](https://github.com/Tenuxi/Uutishetki/actions/runs/42770826633) varmisti allekirjoituksen ja pakettimetatiedot. Laitetarkistuksia ei ajettu.

## 0.5.1 — pysyvä allekirjoitus

Aiemmista 0.2.0-, 0.4.0- ja 0.5.0-julkaisuista luettu APK v2 -lohkon varmenteet ja binäärisen manifestin package/versionCode/versionName/debuggable. Eri debug-varmenteet varmistettu, applicationId sama ja versionCode kasvava. Paikallinen debug-varmenne ei vastannut niitä.

Kolme kohdennettua Python-testiä: salaisuuksien puuttuminen, väärä alias/base64, puuttuva/väärä/ylimääräinen varmenne sekä debug- tai väärän sovelluksen/version APK hylätään. Positiiviset tapaukset hyväksytään. YAML-jäsennys ja julkaisumetatiedot tarkistettu paikallisesti. Varsinainen APK rakennetaan Actionsissa, ei paikallisesti. Fyysistä päivitysasennusta tai tietojen säilymistä laitteella ei ole testattu.

0.5.1:n main-tarkistus: [Actions 42772338669](https://github.com/Tenuxi/Uutishetki/actions/runs/42772338669) onnistui. Lisäksi alkuperäisen keystoren ja paikallisen varakopion varmenteet palautettiin OpenSSL:llä ja SHA-256 verrattiin lukitukseen; tiedosto-oikeudet 0600. Release-käännös ja valmiin release-APK:n varmenne odottavat Secrets-käyttöönottoa. Päivitysasennusta laitteella ei testattu.

11.11.2026: [Release-ajo 42976338371](https://github.com/Tenuxi/Uutishetki/actions/runs/42976338371) onnistui. Pysyvä keystore ja valmis release-APK vastasivat lukittua varmennetta; apksigner varmisti allekirjoituksen ja aapt-tarkistus applicationId:n, versionCoden 8, version 0.5.1 sekä ei-debug-tilan. Väliaikainen runner-avain poistettu. Releasesista ladatun APK:n tiedostotiiviste, v2-varmenne ja manifestimetatiedot tarkistettu erikseen. Ei paikallista Android-buildia tai laitetestiä.


## 0.6.0 — uudet lehdet ja sääpaneeli

Kohdennetut testit: Staran kategoriat ja luokittelemattomat uutiset, Pelaajan syötekohtainen Digi-luokka, RSS-otsikko/kuvaus/aika ja kuvattomuus, saman artikkelin yhdistäminen, Kaikki-valinta ja rajatun lähteen säilyminen, sijainnin alkuperän päätteleminen todellisesta valinnasta ja välimuistipalautus, sääasteiden pyöristys/puuttuminen sekä päivitysrivin Helsinki-päiväraja kesällä ja talvella. Aiemmat ennusteen päivämäärä- ja sadetodennäköisyystestit säilyvät.

Koodista tarkistettu: paneelin rasti ja Dialogin paluutoiminto sulkevat paneelin; ratas avaa nykyisen sääasetukset sisältävän sivupaneelin. Paneeli käyttää safeDrawing-rajoja, rajattua korkeutta, vierivää sisältöä ja erillistä otsikkoriviä. Ennustekortit pinoutuvat alle 360 dp sisältöleveydellä tai fontScale-arvon ylittäessä 1,3. Säätiedon alkuperä tallentuu yhdessä saman haun tietojen kanssa, ei päätellä jälkikäteen asetuksista. Vanha välimuisti ilman alkuperää haetaan uudelleen, asetuksia muuttamatta. Nykyiset suodattimet ja tallennusformaatti säilyvät.

Paikallisesti vain XML-jäsennys, diff- ja julkaisumetatietojen tarkistus. Testit, Lint ja APK-käännös Actionsissa. Ei emulaattoria tai paikallista Android-buildia. Laitteella testaamatta: dialogin todellinen asettelu pienellä näytöllä/vaaka-asennossa/suurimmalla fontilla, TalkBack, ratas/paluu/rasti, päivitysasennus 0.5.1:n päälle sekä asetusten säilyminen oikealla laitteella.

0.6.0:n [Actions-tarkistus 43168097454](https://github.com/Tenuxi/Uutishetki/actions/runs/43168097454) onnistui: 48 Android-yksikkötestiä, 0 virhettä, 0 ohitettua. Lisäksi kolme julkaisusuojauksen Python-testiä, Android Lint ja koekäännös läpäistiin.

[Julkaisuajo 43168340272](https://github.com/Tenuxi/Uutishetki/actions/runs/43168340272) onnistui samalla pysyvällä varmenteella kuin 0.5.1. Julkaistusta APK:sta tarkistettiin lisäksi latauksen SHA-256-tiiviste, APK v2 -varmenne, applicationId `fi.uutishetki`, versionCode 9, versionName 0.6.0 ja ei-debug-tila. [Lataa 0.6.0](https://github.com/Tenuxi/Uutishetki/releases/download/apk-0.6.0/Uutishetki-0.6.0.apk). Fyysistä päivitysasennusta ei testattu.


## 0.7.0 — koko sovelluksen teema ja ulkoasu

Kolme teemalogiikan testiä: puuttuva/tuntematon tallennettu arvo seuraa järjestelmää, kaikki enum-nimet palautuvat, vaalea/tumma ohittavat järjestelmävalinnan ja sama SYSTEM-valinta reagoi järjestelmäteeman vaihtoon. SharedPreferences `appearance/mode` on erillinen uutis-, sää- ja liikenneasetuksista; valinta kirjoitetaan heti. Compose `isSystemInDarkTheme()` seuraa järjestelmän Configuration-muutoksia. Teeman vaihto ei vaihda NewsScreenin identiteettiä eikä kutsu suodatus-, päivitys- tai vieritysmetodeja.

Kevyt tarkistus: resurssien XML ja julkaisumetadata jäsentyivät. Molempien palettien pääteksti-, lisäteksti-, valinta-, liikenne- ja korostustekstien kontrastit laskettiin: tarkistetut parit vähintään 6,37:1. Uutiskuvien renderöintiin ei lisätty värisuodatusta. Kortit ilman kuvaa jättävät kuva-alueen kokonaan pois. Lehtipainikkeiden koko nimi rivittyy ilman ellipsiä; kirjainmerkki ja valintamerkki ovat erillisellä rivillä. Ruudukko vaihtuu yhteen sarakkeeseen alle 280 dp sisältöleveydellä tai fontScale > 1,2. Sääennusteet pinoutuvat alle 320 dp leveydellä tai fontScale > 1,3. Vieritys, kosketusalueet ja safeDrawing-insetit säilyvät.

Havainnekuvia ei ollut tämän tehtävän liitteenä; toteutus perustuu kirjallisiin vaatimuksiin. Mukana toimitettavia vektorisääkuvakkeita käytetään eri säätiloille, kirkkaalle yölle kuuta. Ei ajonaikaista kuvagenerointia tai uusia riippuvuuksia.

Ei visuaalista laite- tai emulaattoritestiä eikä paikallista Android-buildia. Laitteella testaamatta: järjestelmäteeman vaihtuminen avoinna, valinnan palautuminen prosessin tappamisen jälkeen, lukukohdan pysyminen todellisessa teemanvaihdossa, pienin näyttö/suurin tekstikoko/vaaka-asento/TalkBack, Androidin järjestelmäpalkit ja päivitysasennus. Kontrastilaskenta ei korvaa näitä tarkistuksia. Varsinaiset testit, Lint ja build tehdään Actionsissa.

0.7.0:n [Actions-tarkistus 43585314206](https://github.com/Tenuxi/Uutishetki/actions/runs/43585314206) onnistui: 51 Android-yksikkötestiä, 0 virhettä, 0 ohitettua; lisäksi kolme julkaisusuojauksen Python-testiä, Android Lint ja koekäännös läpäistiin. [Release-ajo 43585503803](https://github.com/Tenuxi/Uutishetki/actions/runs/43585503803) onnistui pysyvällä release-avaimella. Lopullisesta ladatusta APK:sta tarkistettiin SHA-256-tiedostotiiviste, APK v2 -varmenne, applicationId `fi.uutishetki`, versio 0.7.0, versionCode 10 ja ei-debug-tila. [Lataa APK](https://github.com/Tenuxi/Uutishetki/releases/download/apk-0.7.0/Uutishetki-0.7.0.apk). Ei laitetestiä.


## 0.7.1 — ulkoasun tarkennus ja sääkuvitus

Visuaalisesti katsottu: 12 säävektorin koontikuva vaalealla ja tummalla pinnalla (Chrome, `design/weather/preview.png`). Pilvet ovat täytettyjä, varjostettuja ja kerroksellisia; aurinko/kuu, päivä-/yöpuolipilvinen, sade/tihku, lumi/räntä, ukkonen ja sumu erottuvat. SVG:n polut ja absoluuttiset gradientit vastaavat XML-resursseja. Tämä tarkistus koskee grafiikkaa, ei koko sovelluksen renderöityä ulkoasua.

Koodista tarkistettu: lähdepainikkeen vaakarivi, kiinteä 30 dp lähdemerkki, loppuleveyden saava nimi, tekstimittauksen perusteella vaihtuva sarakemäärä ja yhteinen intrinsic-rivikorkeus (minimi 56 dp). Uutiskortin merkki 24 dp. Sivupaneelin otsikko on LazyColumnin ulkopuolella ja vieritysalueella on clipToBounds, ensimmäisen sisällön 8 dp yläväli ja safeDrawing-insetit. Ulkoasu-valinta avaa kolme vaihtoehtoa samaan vieritysalueeseen. Aihetoiminnot ovat pieniä tekstipainikkeita, edelleen vähintään 48 dp kosketusalueella. Ennustekortit rinnakkain vähintään 280 dp sisältöleveydellä ja fontScale <= 1,3; muuten allekkain. Pääsää pinoutuu erittäin suurella tekstillä. Otsikoita ei rajata; uutiskuville ei lisätty värisuodatusta.

Ympäristöstä ei löytynyt Android SDK:ta, adb/emulaattoria tai projektin Compose-kuvakaappaus-/Preview-renderöintityönkulkua. Ei paikallista Android-buildia tai emulaattoria. Varsinaista uutisvirtaa, paneelia ja säädialogia ei renderöity puhelimen kuvasuhteella; niiden tarkka rivittyminen, leikkautuminen, pienen näytön/suuren fontin toiminta ja Android-vektorien piirtyminen jäävät laitteella tarkistettaviksi. Tehtävän liite sisälsi vain tekstitiedoston, joten vertailua hyväksyttyyn havainnekuvaan tai käyttäjän kuvakaappauksiin ei voitu tehdä. Ulkoasua ei väitetä varmennetuksi tavoitekuvaa vasten.

Nykyiset toiminnalliset testit säilyvät; uusia väri-/sommitteluyksikkötestejä ei lisätty. XML, julkaisumetadata ja diff tarkistetaan paikallisesti, testit/Lint/build Actionsissa. Sama pysyvä julkaisuvarmenne ja applicationId.

0.7.1:n [Actions-tarkistus 43601651874](https://github.com/Tenuxi/Uutishetki/actions/runs/43601651874) onnistui: 51 yksikkötestiä, 0 virhettä ja 0 ohitettua; julkaisusuojauksen Python-testit, Android Lint ja koekäännös läpäistiin. [Julkaisu 43601942900](https://github.com/Tenuxi/Uutishetki/actions/runs/43601942900) onnistui pysyvällä avaimella. Ladatun APK:n SHA-256, v2-varmenne, applicationId `fi.uutishetki`, versio 0.7.1, versionCode 11 ja ei-debug-tila tarkistettu. [APK](https://github.com/Tenuxi/Uutishetki/releases/download/apk-0.7.1/Uutishetki-0.7.1.apk). Buildin onnistuminen ei korvaa yllä puuttuviksi kirjattuja koko käyttöliittymän visuaalisia tarkistuksia.


## 0.7.2 — julkaisukuvakkeet ja maisemataustat

Virallisten sivujen icon/manifest-viitteet, HTTP 200 -lataukset, kuvien koot ja alkuperätiivisteet dokumentoitu `docs/BRAND_ASSETS.md` ja `design/publishers/sources.json`. Yle jätettiin pois käyttäjän vahvistaman luvan puuttumisen vuoksi. Muiden julkaisijoiden avoimia kuvakelisenssejä ei väitetä.

Visuaalisesti tarkistettu generoidut vaalea/tumma maisema sekä Chrome-resurssikoonti (`design/landscape/resource-preview.png`): kuvakkeiden alkuperäisvärit 26/30 px koossa, neutraali pinta tummassa teemassa, 360 px teemat ja 280 px suuremmat lähdenimet, yhtenäinen maisema ja ennustepintojen luettavuus. Tämä on resurssien HTML-sommittelu, ei natiivin Android-toteutuksen kuvakaappaus. Koodista varmistettu Image ilman tint/colorFilteriä, ContentScale.Fit logoille ja Crop taustalle, yksi sääpaneelin vieritysalue, todelliset tekstit taustan päällä ja maiseman häivytys ennen päivitysriviä.

Android SDK / Compose-renderöinti ei ole käytettävissä. Ei paikallista Android-buildia tai emulaattoria. Todellinen fonttiskaalaus, pienet näytöt, järjestelmäinsetit, rivitys, dialogin vieritys ja päivitysasennus ovat laitteella testaamatta. Nykyiset toiminnalliset testit/Lint/build ajetaan Actionsissa; ei uusia pelkkiä ulkoasutestejä.

0.7.2:n [Actions-tarkistus 43610891541](https://github.com/Tenuxi/Uutishetki/actions/runs/43610891541) onnistui: 51 yksikkötestiä, 0 virhettä, 0 ohitettua, julkaisusuojauksen Python-testit sekä Android Lint ja koekäännös läpäistiin. [Release-ajo 43611115362](https://github.com/Tenuxi/Uutishetki/actions/runs/43611115362) onnistui. Julkaistusta APK:sta tarkistettiin SHA-256, pysyvä v2-varmenne, applicationId `fi.uutishetki`, versio 0.7.2, versionCode 12 ja ei-debug-tila. [APK](https://github.com/Tenuxi/Uutishetki/releases/download/apk-0.7.2/Uutishetki-0.7.2.apk). Ei laitetestiä.


## 0.8.0 — sääanimaatiot

Kolme kohdennettua logiikkatestiä: WMO-sade/tihku/lumi/ukkonen-kartoitus, selkeän/pilvisen/sumuisen ero sekä puuttuva/tuntematon/vanhentunut sää ilman nykyisen sään animaatiota. Ei uusia väri- tai sommittelutestejä.

Koodista tarkistettu: sama nykyisen tiedon tuoreustarkistus ohjaa yläpalkkia ja paneelin taustaa, vanha pääkuva jää liikkumattomaksi. Ennustekuvat ja kaikki tekstit pysyvät staattisina. Animaatiot piirretään Canvas/graphicsLayer-vaiheessa; kellon lukeminen ei muuta layoutia joka ruudulla. Enintään 28 sääpartikkelia taustalla ja 3 kuvakkeessa, noin 30 fps enimmäistahti. LaunchedEffect + repeatOnLifecycle(RESUMED) keskeyttää kellon taustalla; paneelin poistuminen compositionista peruuttaa sen efektit. ContentObserver seuraa Androidin ANIMATOR_DURATION_SCALE-asetusta ja ValueAnimator.areAnimatorsEnabled estää kellosilmukan, kun animaatiot on poistettu käytöstä. Havaitsija poistetaan compositionin mukana. Ukkosessa ei vilkkuvia valoefektejä.

Ei paikallista Android-buildia tai emulaattoria. Androidin animaation sujuvuutta, laitteen virrankulutusta, todellista taustalle siirtymistä ja järjestelmäasetuksen vaihtoa sekä päivitysasennusta ei testattu laitteella. Nykyinen ympäristö ei sisällä natiivin Compose-renderöinnin työkalua. Testit, Lint ja APK-käännös Actionsissa.

0.8.0:n [Actions-tarkistus 43612674899](https://github.com/Tenuxi/Uutishetki/actions/runs/43612674899) onnistui: 54 yksikkötestiä, 0 virhettä, 0 ohitettua; julkaisusuojauksen testit, Lint ja koekäännös läpäistiin. [Release-ajo 43612918242](https://github.com/Tenuxi/Uutishetki/actions/runs/43612918242) onnistui. Julkaistun latauksen SHA-256, pysyvä APK v2 -varmenne, applicationId `fi.uutishetki`, versio 0.8.0, versionCode 13 ja ei-debug-tila tarkistettu. [APK](https://github.com/Tenuxi/Uutishetki/releases/download/apk-0.8.0/Uutishetki-0.8.0.apk). Laitetestiä ei tehty.
