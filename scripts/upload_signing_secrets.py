#!/usr/bin/env python3
"""One-time local upload of an existing key. Never generates or prints secrets."""
import argparse
import base64
import hashlib
import json
import os
import pathlib
import shutil
import subprocess
import sys


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--directory', type=pathlib.Path, default=pathlib.Path.home() / '.local/share/uutishetki/signing')
    parser.add_argument('--replace', action='store_true', help='Korvaa olemassa olevat salaisuudet vain saman varmennetun avaimen palautuksessa')
    args = parser.parse_args()
    if not shutil.which('gh'):
        sys.exit('GitHub CLI puuttuu. Asenna gh ja suorita gh auth login. Älä lähetä tokenia chattiin.')
    root = pathlib.Path(__file__).resolve().parent.parent
    creds = json.loads((args.directory / 'credentials.json').read_text())
    key = args.directory / 'uutishetki-release.p12'
    if creds['alias'] != 'uutishetki-release':
        sys.exit('Väärä avainalias. Salaisuuksia ei muutettu.')
    env = os.environ.copy()
    env['UUTISHETKI_STORE_PASSWORD'] = creds['storePassword']
    pem = subprocess.run(['openssl', 'pkcs12', '-in', str(key), '-clcerts', '-nokeys', '-passin', 'env:UUTISHETKI_STORE_PASSWORD'],
                         env=env, capture_output=True, check=True).stdout
    der = subprocess.run(['openssl', 'x509', '-outform', 'DER'], input=pem, capture_output=True, check=True).stdout
    expected = (root / 'config/release-certificate-sha256.txt').read_text().strip()
    if hashlib.sha256(der).hexdigest() != expected:
        sys.exit('Paikallinen avain ei vastaa lukittua varmennetta. Salaisuuksia ei muutettu.')
    repo = 'Tenuxi/Uutishetki'
    listing = subprocess.run(['gh', 'secret', 'list', '--repo', repo, '--json', 'name'], capture_output=True, check=True)
    existing = {entry['name'] for entry in json.loads(listing.stdout)}
    values = {
        'ANDROID_KEYSTORE_BASE64': base64.b64encode(key.read_bytes()),
        'ANDROID_KEYSTORE_PASSWORD': creds['storePassword'].encode(),
        'ANDROID_KEY_ALIAS': creds['alias'].encode(),
        'ANDROID_KEY_PASSWORD': creds['keyPassword'].encode(),
    }
    if existing.intersection(values) and not args.replace:
        sys.exit('Allekirjoitussalaisuuksia on jo olemassa. Selvitä niiden varmenne ennen korvaamista; palautuksessa käytä --replace.')
    for name, value in values.items():
        subprocess.run(['gh', 'secret', 'set', name, '--repo', repo], input=value, capture_output=True, check=True)
        print(name + ': tallennettu (arvoa ei tulosteta).')
    print('Neljä pysyvän allekirjoituksen salaisuutta tallennettu.')


if __name__ == '__main__':
    try:
        main()
    except subprocess.CalledProcessError:
        sys.exit('Varmennus tai Secrets-kirjoitus epäonnistui. Tarkista gh auth status, repon Secrets-oikeudet ja paikalliset avaintiedostot. Arvoja ei tulostettu. Osittaisen tallennuksen jatkaminen: --replace samalla varmennetulla avaimella.')
    except (OSError, ValueError, KeyError):
        sys.exit('Paikalliset avaintiedostot puuttuvat tai ovat virheelliset. Arvoja ei tulostettu.')
