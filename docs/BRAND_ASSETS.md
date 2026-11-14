# Julkaisukuvakkeet — tarkistus 14.11.2026

Kuvakkeiden alkuperä varmennettiin kunkin julkaisun oman etusivun HTML:n icon/apple-touch-icon/manifest-viitteestä. Lataukset palauttivat HTTP 200; lopulliset osoitteet, koot ja tiivisteet ovat [lähdeluettelossa](../design/publishers/sources.json). Ei kuvahakua, muistista piirrettyjä tunnuksia tai ajonaikaisia latauksia.

| Julkaisu | Virallinen viittaava sivu | Käytetty resurssi |
| --- | --- | --- |
| Iltalehti | https://www.iltalehti.fi/ | https://assets.ilcdn.fi/apple-touch-icon.png (180 × 180) |
| Ilta-Sanomat | https://www.is.fi/manifest.json | https://www.is.fi/html-assets/icons/favicon-192x192.png (192 × 192) |
| Helsingin Sanomat | https://www.hs.fi/manifest.json | https://www.hs.fi/html-assets/icons/favicon-192x192.png (192 × 192) |
| Stara | https://www.stara.fi/ | https://www.stara.fi/wp-content/uploads/2026/01/cropped-stara_logo_1024x1024-200x200.jpg (200 × 200) |
| Pelaaja | https://www.pelaaja.fi/ | https://www.pelaaja.fi/apple-touch-icon.png (180 × 180) |
| Yle Uusimaa / Yle Häme | https://yle.fi/ | Löytyi https://yle.fi/ukko/assets/img/yle-logo-180.png, mutta **ei sisälly sovellukseen**. |

Pieniin 26/30 dp kuvakealueisiin valittiin etusivujen tarjoamat korkearesoluutioiset apple-touch-/manifest-kuvat (180–200 px), ei matalaresoluutioista faviconia. WebP-muunnos on häviötön eikä kokoa, mittasuhteita tai värejä muutettu. Staran JPEG:n pikselit muunnettiin häviöttömästi WebP:ksi. Kuvakkeet piirretään `Image`-komponentilla, `ContentScale.Fit`, ilman `colorFilter`- tai Icon-tint-väritystä. Neutraali valkoinen tausta auttaa myös tummassa teemassa; tunnuksia ei leikata ympyröiksi. Sama `SourceIcon` palvelee uutiskorttia ja lehtivalintaa.

## Käyttöohjeet ja rajat

- [Ylen brändiohje](https://yle.fi/a/20-10001952) edellyttää käyttölupaa sekä kieltää logon muodon ja värien muuttamisen. Käyttäjä vahvisti 14.11.2026, ettei lupaa ole, ja pyysi näyttämään Ylen lähteet ilman logoa. Molemmat alueet näkyvät tekstiniminä; aitona esitettyä korviketta ei käytetä eikä Yle-resurssia paketoida.
- [Alma Median mediayhteydet](https://www.almamedia.fi/media/mediayhteydet/) ohjaa brändiaineistokysymykset viestintään. Iltalehden kuvakkeeseen ei löytynyt erillistä avointa uudelleenjakelulisenssiä.
- Sanoman manifestit tunnistavat nimet Ilta-Sanomat ja Helsingin Sanomat sekä vastaavat sovellustunnisteet. Niissä ei ole kuvakkeiden avointa lisenssiä. [Sanoman pressipalvelun ehdot](https://nelonenpressi.fi/files/Pressipalvelu%20k%C3%A4ytt%C3%B6ehdot%2001_10_2024.pdf) koskevat kyseisen palvelun aineistoja ja rajaavat logokäyttöä journalistiseen tarkoitukseen; niitä ei tulkita yleiseksi luvaksi näihin verkkosivukuvakkeisiin.
- Staran ja Pelaajan etusivujen kuvakeviitteistä ei löytynyt erillistä kuvakkeiden käyttölisenssiä tai julkaistua kuvakekohtaista ohjetta.

Muiden viiden julkaisun kuvakkeet ovat alkuperältään varmennettuja ja niitä käytetään ainoastaan uutislähteen tunnistamiseen käyttäjän pyytämässä lukijassa. Erillistä julkaisijakohtaista käyttölupaa ei ole hankittu eikä avointa lisenssiä väitetä. Oikeudet ja tavaramerkit kuuluvat julkaisijoille; sovellus ei ole niiden virallinen sovellus. Kuvakkeet eivät kuulu projektin omien grafiikoiden lisenssiin.
