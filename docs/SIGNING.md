# Pysyvä APK-allekirjoitus (0.5.1)

## Varmennettu syy 10.11.2026

GitHub Releasesista ladattujen APK:iden v2-allekirjoituslohkon varmenne ja binäärinen AndroidManifest.xml tarkistettiin. Kaikissa oli `fi.uutishetki`, `debuggable=true` ja varmenteen subject `C=US,O=Android,CN=Android Debug`. Avaimet olivat eri:

| APK | versionCode | Varmenteen SHA-256 |
| --- | --- | --- |
| 0.2.0 | 2 | `579235effeb64fac61677b8f0c23480a4c17601f39af55ecf17c47a3d52bb52f` |
| 0.4.0 | 6 | `0a4566fc4ba8d560e64b168d44682a047ec0970fd25c1648d0771b5fd21b0448` |
| 0.5.0 | 7 | `cf966879bebc4c42c7fc280c08ba77f214c33d3b279cf59b36d70b294ed25fe4` |

Aiempi apk-tagityönkulku nouti CI:n debug-artifactin ja julkaisi sen. Gradle loi runnerin debug-avaimen ilman pysyvää keystore-salaisuutta. Paikallinen debug.keystore ei vastannut yllä olevia varmenteita; sen SHA-256 on `ed3447ca65cc0609705aea12962b6eb493bf7f8f03c52a2eb9711cec2a726dab`. Aiemman avaimen puuttuminen oli selvitetty myös aiemmissa julkaisuissa. ApplicationId:n vaihtuminen tai liian pieni versionCode eivät selitä näiden julkaisujen päivitysvirhettä. Varmenteen vaihtuminen selittää sen. Laitteen varsinaista asennusvirhelokia ei luettu eikä päivitysasennusta testattu laitteella.

Androidin [allekirjoitusohje](https://developer.android.com/studio/publish/app-signing): samalla applicationId:llä jaettavan APK:n päivitysketju edellyttää yhteensopivaa allekirjoitusavainta. Allekirjoitusta ei voi nollata asennetusta sovelluksesta.

## Yksi uusi pysyvä avain

Käyttäjän luvalla luotu yksi RSA-3072 PKCS12-keystore, alias **uutishetki-release**, voimassa noin 27 vuotta. Avainta ei luoda buildien yhteydessä. Alkuperäinen ja toinen paikallinen kopio ovat repon ulkopuolella yksityisessä hakemistossa:

- `~/.local/share/uutishetki/signing/uutishetki-release.p12`: salasanalla salattu yksityinen avain ja julkinen varmenne.
- `~/.local/share/uutishetki/signing/credentials.json`: storePassword, keyPassword ja alias. Tämä on salaisuus; tiedosto-oikeudet 0600, hakemisto 0700.
- `~/.local/share/uutishetki/signing/backup/`: toinen paikallinen kopio molemmista tiedostoista. Sama levy ei suojaa levyrikolta, varkaudelta tai hakemiston menetykseltä.

Julkinen varmenteen SHA-256, lukittu tiedostoon [release-certificate-sha256.txt](../config/release-certificate-sha256.txt):

```text
f93d3203097b5c8bba2d8c36a81672af53330af9df261c90169e8660db698a59
```

**Siirtyminen 0.5.0:sta tai muusta vanhalla debug-avaimella allekirjoitetusta APK:sta vaatii vanhan asennuksen poistamisen kerran. Poistaminen poistaa paikalliset asetukset ja tallennetut tiedot.** Uusi avain ei tee vanhoista asennuksista päivitettäviä. Tämän pysyvän avaimen julkaisut voidaan jatkossa asentaa toistensa päälle, kun versionCode kasvaa. Tätä ei ole testattu fyysisellä laitteella.

## GitHub Secrets: käyttöönotto ilman arvojen tulostamista

Tarvitaan GitHub CLI (`gh`), OpenSSL ja Python 3. Kirjaudu omistajan tai riittävät Secrets-oikeudet omaavalla tunnuksella:

```sh
gh auth login
gh auth status
python3 scripts/upload_signing_secrets.py
```

Suorita repon hakemistosta. Skripti tarkistaa ensin keystoren julkisen varmenteen, siirtää arvot suoraan gh-komentojen stdin-syötteeseen eikä tulosta salasanoja tai base64-avainta. Kohde on kiinteästi **Tenuxi/Uutishetki**. Älä lisää `set -x`:ää tai tulosta skriptin syötteitä. Jos kirjautuminen tai oikeudet puuttuvat, skripti keskeyttää. Olemassa olevia allekirjoitussalaisuuksia ei korvata oletuksena.

Repositorion [Actions Secrets -asetuksiin](https://github.com/Tenuxi/Uutishetki/settings/secrets/actions) tallennetaan:

| Salaisuus | Lähde (ei arvoja dokumentaatioon) |
| --- | --- |
| ANDROID_KEYSTORE_BASE64 | saman pysyvän `uutishetki-release.p12`-tiedoston base64-koodaus |
| ANDROID_KEYSTORE_PASSWORD | credentials.json: storePassword |
| ANDROID_KEY_ALIAS | credentials.json: alias, aina uutishetki-release |
| ANDROID_KEY_PASSWORD | credentials.json: keyPassword |

GitHubin `GITHUB_TOKEN` riittää buildin ja julkaisun aikana Secrets-arvojen käyttöön sekä releaseen, mutta paikallinen SSH-push-kirjautuminen ei anna Secrets-hallinnan API-oikeutta. [GitHubin Secrets-dokumentaatio](https://docs.github.com/en/rest/actions/secrets).

## Varmuuskopiointi ja palautus

1. Kopioi salattu p12 **erilliselle salatulle levylle tai luotettavaan salattuun varmuuskopiopalveluun**. Tuo credentials.json:n salasanat ja alias suojattuun salasananhallintaan; pidä salasanavarmistus erillään avaintiedoston varmuuskopiosta.
2. Varmista palautettavuus lukemalla kopion varmenne ja vertaamalla yllä olevaan sormenjälkeen. Älä poista koneen ainoaa käyttökelpoista kopiota ennen erillisen varmuuskopion palautustestiä. GitHub Secrets ei tarjoa arvojen uloslukuun varmuuskopiointikeinoa.
3. Palautuksessa kopioi **sama** keystore ja salasanat yksityiseen hakemistoon (hakemisto 0700, tiedostot 0600). Älä generoi uutta avainta äläkä muuta varmenteen lukitusta virheen ohittamiseksi.
4. Palauta Secrets-arvot komennolla `python3 scripts/upload_signing_secrets.py --directory /suojattu/palautushakemisto --replace`. Skripti hyväksyy vain lukittua varmennetta vastaavan avaimen. Osittain onnistunut ensimmäinen Secrets-siirto voidaan jatkaa samalla komennolla ja alkuperäisellä hakemistolla.
5. Jos ainoa yksityinen avain tai sen salasana katoaa, APK-päivitysketjua ei voi palauttaa pelkällä julkisella sormenjäljellä. Uusi avain edellyttää uutta kertasiirtymää. Älä koskaan lähetä avainta/salasanoja chattiin, committiin, APK:hon, artifactiin tai lokiin.

## Julkaisuketju

1. Kasvata versionName ja versionCode sekä päivitä CHANGELOG. Pushaa main ja odota saman commitin Android-tarkistuksen onnistuminen.
2. Tagita tarkistettu commit `apk-<version>` (myös `v<version>` käyttää samaa turvallista työnkulkua; älä julkaise molempia).
3. Julkaisutyö vaatii kaikki neljä salaisuutta, tarkistaa aliaksen ja keystoren julkisen varmenteen. Puuttuvat/virheelliset salaisuudet keskeyttävät ennen buildia.
4. Ajetaan `assembleRelease`, ei debug-artifactin kopiointia. Release ei ole debuggable. Keystore puretaan vain runnerin yksityiseen väliaikaistiedostoon, configuration cache on pois päältä ja tiedosto poistetaan myös epäonnistuessa.
5. `apksigner verify --verbose --print-certs` varmentaa APK:n ja `verify_signature.py` vaatii täsmälleen yhden odotetun varmenteen. `aapt` + `verify_apk.py` vaatii applicationId:n fi.uutishetki, nykyisen version/versionCoden ja ei-debug-tilan. `release_notes.py` tarkistaa versionCoden kasvun aiempiin tageihin.
6. Vasta kaikkien tarkistusten jälkeen Releasesiin lisätään release-APK ja APK:n SHA-256-tiedostotiiviste (eri asia kuin varmenteen SHA-256). Avainta tai salasanoja ei liitetä mukaan.

Normaali CI voi edelleen rakentaa debug-kehitysartifactin ilman salaisuuksia, mutta sitä ei koskaan käytetä julkaisutyössä. Vanha vaihtoehtoinen v-tagien julkaisu-job on poistettu; molemmat tagimuodot käyttävät samaa varmennettua release-polkua. Älä korvaa jo jaettua APK:ta samalla versionCodella. Epäonnistuneen, vielä julkaisemattoman tagin työn voi ajaa uudelleen salaisuuksien korjauksen jälkeen.
