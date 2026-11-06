# Sää, paikannus ja lähialueen liikennetiedotteet (0.4.0)

Tarkistettu 6.11.2026. Tämä suunnitelma korvaa aiemman Tilannehuone-selvityksen. Sovellus ei käytä Tilannehuonetta tai 112 Suomi -sovelluksen sisäisiä tietoja.

## Oletukset ja tallennetut valinnat

Uudella asennuksella sää ja lähialueen liikennetiedotteet ovat päällä, automaattinen sijainti valittuna ja säde 10 km. Helsinki (60.17, 24.94) on vain sään varapaikka. Sää haetaan heti uutisista ja lupakyselystä riippumatta. Ensikäynnistyksen perustelu kertoo sään ja liikennetiedotteiden sijaintitarpeen; Jatka sijaintilupaan avaa Androidin lupakyselyn. Perustelun ohitus ja luvan epääminen muistetaan, eikä lupaa kysytä automaattisesti uudelleen.

Aiemmin tallennettu sään päälle/pois-valinta, automaattinen tila ja paikkakunta säilyvät sellaisinaan. Liikenneasetukset tallennetaan erikseen. Käsivalinta ohittaa paikannuksen, kunnes käyttäjä valitsee automaattisen tilan. Jos automaattinen paikannus ei onnistu, sää käyttää aiemmin valittua paikkakuntaa tai Helsinkiä. Liikennetiedotteet odottavat saatua sijaintia tai käsivalintatilassa valittua paikkakuntaa: sään varapaikasta ei tehdä käyttäjän liikennesijaintia.

## Säärajapinta

- [Open-Meteo Forecast API](https://open-meteo.com/en/docs): `https://api.open-meteo.com/v1/forecast`, current-kentät temperature_2m, weather_code, is_day; Unix-aika ja Celsius. Mallipohjainen arvio, ei paikallinen mittaus. Säätieto ja lähdemerkinnät avautuvat yläpalkista.
- [Open-Meteo Geocoding](https://open-meteo.com/en/docs/geocoding-api): suomenkielinen FI-paikkakuntahaku; GeoNamesin PPL-alkuiset asutut paikat, ei lentokenttiä. Paikkakunnan keskipisteellä tarkoitetaan palvelun nimettyä paikkapistettä, ei hallinnollisen alueen geometrista keskipistettä.
- [Käyttöehdot](https://open-meteo.com/en/terms): maksuton ei-kaupallinen käyttö sopii nykyiseen mainoksettomaan henkilökohtaiseen sovellukseen. Alle 600 pyyntöä minuutissa, 5 000 tunnissa ja 10 000 vuorokaudessa. CC BY 4.0; Open-Meteo-, GeoNames- ja lisenssilinkit sovelluksessa. Ei maksullista tilausta tai API-avainta. Kaupallinen käyttö vaatii ehtojen uuden arvioinnin.

Säähaku enintään 15 minuutin välein, virheen uudelleenyritys vähintään viiden minuutin kuluttua; paikan vaihto aloittaa uuden haun. Vain viimeisin säätieto säilytetään levyllä. Havainto yli 60 min tai hakuaika yli 30 min vanha, tulevaisuuden aika tai hakuvirhe estää lämpötilan esittämisen ajantasaisena. Lisätiedoissa vanha tieto merkitään erikseen. Ensihakua kuvaa kolmen pisteen lataustila; puuttuvaa lämpötilaa ei keksitä.

## Digitrafficin liikennetiedotteet

- Virallinen [tieliikenteen dokumentaatio](https://www.digitraffic.fi/tieliikenne/) ja [OpenAPI](https://tie.digitraffic.fi/swagger/openapi.json), tarkistettu versio `2026.11.2-1`. Tuettu Simppeli JSON **v2**: `https://tie.digitraffic.fi/api/traffic-message/v2/traffic-announcements`. Todellinen HTTPS-vastaus ja skeema tarkistettu. V2 ei hyväksy vanhan dokumentaatioesimerkin includeAreaGeometry-parametria; geometria sisältyy perusvastaukseen.
- GeoJSON FeatureCollection; situationId vakaa tunniste, version ja versionTime päivitykselle, releaseTime alkuperäiselle julkaisulle. V2:n pienaakkosiset situationType/trafficAnnouncementType-arvot huomioitu. Vain traffic announcement: general, preliminary accident report, accident report ja unconfirmed observation. Ei erillisiä tietyö-, painorajoitus- tai erikoiskuljetushakuja.
- Ensin valitaan kunkin tunnisteen uusin versio, sitten poistetaan ended/retracted sekä closed/canceled-tilat. timeAndDuration.startTime/endTime rajaa voimassaolon; päättyminen tarkistetaan näkyvissä ollessa 15 sekunnin välein. Uusi täysi tilannekuva korvaa välimuistin, joten lähteestä poistuneet tiedotteet poistuvat myös sovelluksesta. Puuttuva endTime tarkoittaa seurantatiedotteen odottamista, ei keksittyä päättymisaikaa.
- Geometriat Point, MultiPoint, LineString, MultiLineString, Polygon ja MultiPolygon. Etäisyys tieviivan lähimpään janan kohtaan isoympyrälaskennalla; alueen sisällä nolla ja ulkona lähin reuna, aukot huomioiden. Kuntakeskipistettä tai geometrian keskipistettä ei käytetä tapahtumapaikkana. Puuttuvat/virheelliset geometriat ja pelkkänä pisteenä esitetyt alueviitteet jätetään pois. Alueen sisällä nolla tarkoittaa alueeseen kuulumista, ei onnettomuutta käyttäjän kohdalla.
- Säteet 5/10/25/50 km; suodatus paikallisesti koko tilannekuvasta. Käyttäjän koordinaatteja ei lähetetä Digitrafficiin. Etäisyys on suoraviivainen arvio lähdegeometriaan, ei ajomatka. Laitteen likimääräisyys vaikuttaa arvioon; kilometrit pyöristetään eikä esitetä desimaalitarkkuutta.
- Kortit järjestetään releaseTime-ajan mukaan yhdessä uutisten kanssa, versionTime näytetään erikseen. Lehti- ja aihevalinnat eivät suodata liikennettä. Lähteen tekstiä ei tiivistetä AI:lla tai kirjoiteta uudelleen. Kortin rajatun tekstin saa kokonaan lisätiedoista. Aineiston additionalInformation-kentän kelvollinen HTTPS-linkki tarjotaan sellaisenaan; tapahtumalinkeille ei keksitä osoitteita.

## Käyttöehdot, kuormitus ja välimuisti

[Digitrafficin käyttöehdot](https://www.digitraffic.fi/kayttoehdot/): CC BY 4.0. Kortissa Fintraffic / Digitraffic, lisätiedoissa ja asetuksissa linkki alkuperäiseen aineistosivuun ja lisenssiin. Lähteen teksti ja lähettäjä säilyvät; sovelluksen laskema etäisyys tunnistetaan erikseen. Testin `traffic-v2-sample.json` on yhden muuttamattoman tiedotteen otos 6.11.2026 haetusta vastauksesta, sama lähde ja lisenssi.

[Rajapinnan käyttöohje](https://www.digitraffic.fi/tuki/ohjeita/): gzip-pakkaus vaaditaan (OkHttp hoitaa automaattisesti), Digitraffic-User: Uutishetki/versio ilman henkilötietoja. Ilman tunnisteotsikkoa yleisraja on 60 pyyntöä/min/IP; tunnisteella suurempi sallittu määrä ei ole kiinteänä lukuna luvattu. Sovellus hakee yhden koko maan tilannekuvan enintään viiden minuutin välein vain näkyvissä ja vain kun liikenne on päällä ja sijainti tiedossa. Säteenvaihto käyttää samaa välimuistia. 429/503-vastauksen Retry-After huomioidaan. Ei palvelinta, taustatyötä, push-ilmoituksia tai ääniä.

Yksi viimeisin liikennetilannekuva tallennetaan atomisesti tiedostoon. Enintään 8 MiB vastaus; verkkovirhe, jäsennysvirhe tai yli viisi minuuttia vanha tilannekuva piilottaa aiemmat kortit ja näyttää ilmoituksen. Tiedotteen päivitysaika voi olla vanha, vaikka tilannekuva on juuri varmennettu; ajat näytetään erikseen. Lista ei ole kattava tai takuuvarmasti reaaliaikainen hälytyspalvelu.

## Paikannus ja tietosuoja

Vain ACCESS_COARSE_LOCATION, ei tarkkaa tai taustasijaintilupaa. Kertapaikannus näkyvissä enintään viiden minuutin välein, 15 sekunnin aikaraja. Enintään viisi minuuttia vanha sijainti ja enintään 2 km:n tarkkuusarvio hyväksytään; muuten käyttäjälle kerrotaan käsivalinnasta. Automaattisen sijainnin koordinaatit ovat vain muistissa ja tyhjentyvät taustalle siirryttäessä. Sään viimeisin välimuisti sisältää pyöristetyn paikan, ei sijaintihistoriaa.

Sääkoordinaatit ja paikannimihaku pyöristetään kahteen desimaaliin. Laitteen Geocoder hakee paikannimen parhaansa mukaan; nimeä ei keksitä. Open-Meteo saa pyynnön koordinaatit/paikannimen ja IP-osoitteen; sen ehtojen palvelinlokien säilytys on enintään 90 päivää. Sääasiakkaan koordinaatti-URL:eja ei tallenneta HTTP-levyvälimuistiin. Digitraffic saa IP-osoitteen ja sovellustunnisteen mutta ei käyttäjän sijaintia. Uutistoiminnot eivät odota sää-, paikannus- tai liikennepyyntöjä.

## Julkaisun rajoite

Pysyvä applicationId säilyy. Aiemman julkaisun allekirjoitusavainta ei ole tallessa; nykyinen hyväksytty apk-tagityönkulku julkaisee CI:n testiallekirjoitetun APK:n. Avaimen säilymistä tai vanhan päälle asentumista ei voida luvata. Uusi asennus voi vaatia aiemman poistamisen, mikä poistaa paikalliset asetukset. Sovellus itse ei nollaa tallennettuja asetuksia päivityksessä.
