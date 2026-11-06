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
