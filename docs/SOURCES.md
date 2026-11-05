# Lähdeselvitys 5.11.2026

Osoitteet luettiin julkaisijoiden RSS-hakemistoista tai tunnistettiin RSS-hakemistohaulla ja varmistettiin suoraan julkaisijan palvelimelta Pythonin HTTP-asiakkaalla. Kaikki alla olevat syötteet palauttivat 200 ja RSS `channel/item` -rakenteen. Sovellus käyttää vain näitä syötteitä, ei artikkelikaavintaa tai epävirallista API:a.

| Lähde | Varmistettu syöte | Havaittu sisältö |
| --- | --- | --- |
| Iltalehti | https://www.iltalehti.fi/rss/rss.xml | 20 uutista; title, link, guid, pubDate, category, description, enclosure/media-kuva |
| Ilta-Sanomat | https://www.is.fi/rss/tuoreimmat.xml | 100 uutista; title, link, guid, pubDate, category, description, image/jpeg-enclosure |
| Yle Uusimaa | https://yle.fi/rss/t/18-147345/fi | 20 uutista; title, link, guid, pubDate, description, useita category-kenttiä; ei havaittua kuvaa |
| Yle Häme | https://yle.fi/rss/t/18-138727/fi | 20 uutista; sama rakenne kuin Uudellamaalla; Kanta-Häme, ei koko historiallisen Hämeen alue |
| Helsingin Sanomat | https://www.hs.fi/rss/tuoreimmat.xml | 100 uutista; title, link, guid, pubDate, category, description, image/jpeg-enclosure |

Iltalehden `/rss/uutiset.xml` tarkistettiin myös (200, 20 uutista), mutta toteutuksessa käytetään virallisen hakemiston `/rss/rss.xml`-kokoomasyötettä. Näin urheilu ja viihde eivät jää kokonaan ulkopuolelle. Syötteen pieni ikkuna ei takaa kaikkien uutisten kertymistä, varsinkaan harvoin avattaessa. Julkaisija voi muuttaa tai sulkea syötteen.

## Käyttöehdot ja käyttötarkoituksen rajaus

- [Iltalehden RSS-hakemisto](https://www.iltalehti.fi/rss) tarjoaa sisällöt RSS-lukijalla seurattaviksi ja kieltää otsikoiden kaupallisen käytön ilman kirjallista lupaa. Tämä sovellus on maksuton yksityinen lukija. Sivulla ei anneta erillistä yleistä kuvien uudelleenjulkaisulupaa.
- [Ylen RSS-hakemisto](https://yle.fi/rss) nimeää käytetyt alueosoitteet. [RSS-käyttöehdot](https://yle.fi/a/20-10008076) sallivat otsikkolinkityksen suoraan alkuperäiseen uutiseen, rajaavat kaupallista käyttöä ja kieltävät kuvien julkaisemisen omilla sivustoilla sekä sisällön muokkaamisen/kopioimisen. Kuvia ei haeta Ylen artikkeleista.
- [Ylen tekijänoikeusohje](https://yle.fi/a/20-10004373) sallii tekstiaineiston yksityisen käytön koti-, sukulais- ja ystäväpiirissä. Lyhennetty kuvaus ja paikallinen offline-kopio tässä sovelluksessa perustuvat tähän yksityiskäyttörajaukseen; tämä ei ole lupa saman toteutuksen julkiseen sisältöpalveluun. Alkuperäislinkit säilytetään sellaisinaan.
- [Sanoman yleiset käyttöehdot](https://oma.sanoma.fi/tuote/hs-teema/kayttoehdot) koskevat myös HS:ää ja IS:ää. Sisältö on suojattua eikä yleistä oikeutta julkiseen uudelleenjakeluun synny. [IS:n RSS-sivu](https://www.is.fi/rss/) ja [HS:n RSS-sivu](https://www.hs.fi/rss/) vastasivat tässä tarkistuksessa 403:lla, vaikka varsinaiset XML-syötteet toimivat. **RSS-erityisehtojen ajantasaisuutta ei voitu varmistaa.** Käyttö on rajattu käyttäjän pyytämään henkilökohtaiseen lukijaan; mahdollisen laajemman jakelun ja kuvien uudelleenjulkaisun lupa pitää selvittää kustantajalta.

APK ei sisällä julkaisijoiden uutisaineistoa: kukin käyttäjän laite lataa sen suoraan lähteestä. Lähteen nimi näytetään jokaisessa kortissa. Kuvia ei oteta artikkelien HTML:stä, tekstejä ei täydennetä eikä maksumuuria kierretä. Syötteiden kuvausoikeuksia ei ole erikseen lisensoitu julkiseen uudelleenjulkaisuun. Älä muuta tämän yksityisen lukijan jakelutapaa julkiseksi uutispalveluksi ilman ehtojen uutta tarkistusta.

## Kategoriakartoitus

`Topic.aliases` on kartoituksen auktoriteetti. Vain syötteen `category`-arvoja verrataan pienaakkosin; otsikosta tai kuvaustekstistä ei arvata aihetta. Yhdellä uutisella voi olla useita aiheita (Yle toimittaa useita kategorioita). Lähde- ja aihevalinta yhdistetään AND-operaatiolla. Tuntematon kategoria näkyy Kaikki-aiheessa.

| Aihe | Tunnistettavat kategoriat |
| --- | --- |
| Kotimaa | kotimaa, politiikka, kaupunki |
| Ulkomaat | ulkomaat |
| Talous | talous, taloussanomat, raha, pörssi, sijoittaminen |
| Urheilu | urheilu, jääkiekko, jaakiekko, nhl, smliiga, jalkapallo, formula 1, f1, formulat, hiihto, ralli, yleisurheilu, koripallo, tennis, salibandy, pesäpallo |
| Viihde | viihde, tv & elokuvat, tv-ja-elokuvat, tv-ja-leffat, popkulttuuri, musiikki, kuninkaalliset |

Kartoitus on tarkoituksella rajallinen: esimerkiksi HS:n artikkelin kategoria voi olla yksittäinen aihe, jota ei tässä listassa ole. Ulkomaan valtion nimestä ei päätellä Ulkomaat-aihetta. Kaikki-valinta näyttää myös nämä uutiset. Kuva on valinnainen, teaser on HTML:stä puhdistettu enintään 180 merkkiä ja näytössä enintään kaksi riviä. Puuttuva tai virheellinen päiväys jää tyhjäksi tietokannassa ja listan loppuun, ei muutu hakuajaksi.
