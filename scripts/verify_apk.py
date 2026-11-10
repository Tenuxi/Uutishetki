#!/usr/bin/env python3
"""Validate a release APK's aapt output against application and version."""
import pathlib
import re
import sys


def verify_metadata(metadata, properties):
    package = re.search(r"^package: name='([^']+)' versionCode='([^']+)' versionName='([^']+)'", metadata, re.M)
    if not package or package.groups() != ('fi.uutishetki', properties['versionCode'], properties['versionName']):
        raise ValueError('Väärän sovelluksen tai version APK.')
    if re.search(r'^application-debuggable\b', metadata, re.M):
        raise ValueError('Debug-APK:ta ei saa julkaista. Tarvitaan allekirjoitettu release-APK.')


if __name__ == '__main__':
    properties = dict(line.split('=', 1) for line in pathlib.Path('version.properties').read_text().splitlines() if '=' in line)
    try:
        verify_metadata(pathlib.Path(sys.argv[1]).read_text(), properties)
    except (ValueError, OSError) as error:
        sys.exit(str(error))
    print('Release-APK:n applicationId, versionCode, versionName ja ei-debug-tila varmennettu.')
