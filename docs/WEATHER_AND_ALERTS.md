# Sää ja lähialueen hälytystietojen selvitys

Tarkistettu 6.11.2026. Sää on vapaaehtoinen ja oletuksena pois. Uutiset ja niiden suodatus toimivat ilman sijaintilupaa.

## Sää ja paikkakunnat

- [Open-Meteo Forecast API](https://open-meteo.com/en/docs): `https://api.open-meteo.com/v1/forecast`, current-kentät temperature_2m, weather_code ja is_day. Ajat Unix-sekunteina, lämpötila Celsius-asteina. Kyse on mallipohjaisesta arviosta, ei paikallisesta mittauksesta. Helsinki-esimerkkipyyntö ja vastausyksiköt tarkistettu HTTPS-haulla.
- [Paikkakuntahaku](https://open-meteo.com/en/docs/geocoding-api): `https://geocoding-api.open-meteo.com/v1/search`, FI-rajaus, suomenkieliset nimet. Vain asutut paikat (PPL-alkuiset GeoNames-luokat), ei lentokenttiä tai saaria. Paikkakunta tarkoittaa hakupalvelun paikkapistettä, ei kunnan hallinnollisen alueen geometrista keskipistettä.
- [Käyttöehdot](https://open-meteo.com/en/terms): maksuton rajapinta yksityiseen ei-kaupalliseen käyttöön; alle 600 pyyntöä minuutissa, 5 000 tunnissa, 10 000 vuorokaudessa ja 300 000 kuukaudessa. Nykyinen mainokseton henkilökohtainen sovellus sopii tähän; kaupallinen käyttö edellyttää uutta ehtojen arviointia. Ei maksullista tilausta tai API-avainta.
- Säätiedon lisenssi CC BY 4.0; sovelluksen säätiedoissa ja asetuksissa Open-Meteo-, lisenssi- ja GeoNames-linkit. Lämpötila pyöristetään kokonaisasteiksi ja WMO-koodit kuvataan suomeksi.

## Päivitys, tietosuoja ja virheet

Sää haetaan uutisista riippumattomalla asynkronisella pyynnöllä enintään 15 minuutin välein sovelluksen ollessa näkyvissä. Virheen jälkeen vähintään viiden minuutin uudelleenyritysväli. Käyttäjän asetuksen vaihto käynnistää uuden haun. Paikkakuntahaku lähetetään vain Hae-painikkeesta. Ei sijaintipalvelua, työtä tai ajastinta taustalla. Keskeneräinen säähaku ja kertapaikannus perutaan näkyvyyden päättyessä.

Automaattinen sijainti pyytää vain likimääräisen ACCESS_COARSE_LOCATION-luvan käyttäjän painikkeesta. Luvan epääminen ei estä käsin valintaa tai uutisia. Paikannuksella on 15 sekunnin aikaraja; enintään viisi minuuttia vanha ja enintään 25 km tarkkuusarvion sijainti kelpaa sääarvioon. Koordinaatit pyöristetään kahteen desimaaliin ennen verkkopyyntöä. Laitteen Geocoder-palvelu hakee paikannimen parhaansa mukaan; puuttuvaa nimeä ei keksitä. Tämä tarkkuusraja koskee vain säätä, ei tapahtumien etäisyyksiä.

Open-Meteo näkee pyynnön koordinaatit/paikannimen ja IP-osoitteen; sen ehtojen mukainen palvelinlokien säilytys on enintään 90 päivää. Laitteen paikannimipalvelu voi käyttää verkkoyhteyttä. Sovellukseen ei tallenneta sijaintihistoriaa: asetuksissa yksi käsin valittu paikka ja välimuistissa vain viimeisin säätieto sijainteineen. Sääasiakkaan HTTP-levyvälimuisti on pois käytöstä, jotta koordinaatti-URL:eista ei kerry historiaa. Asetuksen vaihto tai sään poiskytkentä poistaa viimeisen säätiedon.

Yläpalkki näyttää lämpötilan vain, jos havainto on enintään 60 ja haku enintään 30 minuuttia vanha eikä hakutilassa ole virhettä. Tulevaisuuden ajat, puuttuvat kentät, väärät yksiköt ja epäkelvot lämpötilat hylätään. Muuten näkyy viiva ja lisätiedoissa virhe tai selvästi vanhaksi merkitty aiempi tieto. Paikannuksen virhe ei vaihda käyttäjää hiljaisesti toiseen kaupunkiin.

## Hälytystietojen este

Hälytysintegraatiota ei julkaista tässä versiossa. Ei esimerkkitapahtumia, HTML-kaavintaa, kilometrisuodatinta tai näennäisesti toimivaa päälle-kytkintä. Paneeli kertoo esteestä ja tarjoaa tavallisen linkin Tilannehuone.fi-sivustolle.

- [Tilannehuone.fi:n ohje](https://www.tilannehuone.fi/ohje.php) ja [hälytyskartan ohje](https://www.tilannehuone.fi/halytys_ohje.php): käyttökelpoista avointa uudelleenjulkaisurajapintaa ja sen lupaehtoja ei saatu varmennettua. Kartan paikka on tyypillisesti kunnan keskusta, tarkennettu paikka merkitään erikseen. Sivuston karttapiste ei siis sellaisenaan kelpaa luotettavaan 5–50 km tapahtumarajaukseen. Ei sekoiteta Tilannehuone.net-palveluun. Rajapinnan pysyvää tapahtumatunnistetta, päivittyvää aikaa/tyyppiä, alkuperäislinkkiä ja tarkkuusmetatietoa ei voitu varmistaa.
- [Sisäministeriön tiedote 22.9.2026](https://valtioneuvosto.fi/-/1410869/pelastustoimen-mediapalvelun-ennakkorekisteroityminen-on-avattu-22.9.2026-klo-10-00): uusi pelastustoimen mediapalvelu edellyttää hyväksyttyä median rekisteröitymistä; vanha Peto-media suljetaan siirtymässä. Vanhoja syöteosoitteita ei oleteta toimiviksi eikä avoimiksi.
- [Kymenlaakson hyvinvointialueen tiedote](https://www.sttinfo.fi/tiedote/72271437/pelastustoimen-halytystehtavien-automaattiset-ensitiedotteet-paattyvat-kymenlaaksossa-tiedottaminen-jatkuu-katkotta?lang=fi&publisherId=69820505): automaattiset ensitiedotteet päättyivät 1.9.2026; valikoiva tiedottaminen ei ole kattava tapahtumasyöte tarkkoine koordinaatteineen.
- [Ilmatieteen laitoksen varoitusrajapinta](https://www.ilmatieteenlaitos.fi/varoitusten-latauspalvelun-pikaohje) koskee alueellisia säävaroituksia, ei tavoiteltuja pelastustoimen hälytystapahtumia.

Jatkototeutus edellyttää varmennettua API-/syötesopimusta, uudelleenjulkaisulupaa, vakaita tunnisteita ja tapahtuman sijainnin tarkkuutta kuvaavaa tietoa. Kuntakeskipisteistä ei lasketa näennäisen tarkkoja tapahtumaetäisyyksiä.
