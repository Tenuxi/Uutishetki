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
