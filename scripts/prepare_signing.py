#!/usr/bin/env python3
"""Decode existing Actions secrets into a private temporary file; never create keys."""
import base64
import os
import pathlib
import sys

REQUIRED = ('KEYSTORE_BASE64', 'SIGNING_STORE_PASSWORD', 'SIGNING_KEY_ALIAS', 'SIGNING_KEY_PASSWORD')


def signing_material(env):
    missing = [name for name in REQUIRED if not env.get(name)]
    if missing:
        raise ValueError('Allekirjoitussalaisuuksia puuttuu: ' + ', '.join(missing) + '. Katso docs/SIGNING.md. Debug-varavaihtoehtoa ei käytetä.')
    if env['SIGNING_KEY_ALIAS'] != 'uutishetki-release':
        raise ValueError('Väärä avainalias.')
    try:
        material = base64.b64decode(env['KEYSTORE_BASE64'], validate=True)
    except ValueError:
        raise ValueError('Keystoren base64-koodaus on virheellinen.') from None
    if not material:
        raise ValueError('Keystore on tyhjä.')
    return material


if __name__ == '__main__':
    try:
        data = signing_material(os.environ)
        path = pathlib.Path(os.environ['RUNNER_TEMP']) / 'uutishetki-release.p12'
        fd = os.open(path, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
        with os.fdopen(fd, 'wb') as stream:
            stream.write(data)
    except (ValueError, OSError, KeyError) as error:
        sys.exit('::error::' + str(error))
    print('Pysyvä keystore valmisteltu yksityiseen väliaikaistiedostoon.')
