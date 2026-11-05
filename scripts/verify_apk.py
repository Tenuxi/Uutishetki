#!/usr/bin/env python3
"""Validate aapt output against the intended application and version."""
import pathlib
import re
import sys
properties = dict(line.split('=', 1) for line in pathlib.Path('version.properties').read_text().splitlines() if '=' in line)
metadata = pathlib.Path(sys.argv[1]).read_text()
package = re.search(r"^package: name='([^']+)' versionCode='([^']+)' versionName='([^']+)'", metadata, re.M)
assert package, 'APK:n pakettitietoja ei löytynyt'
assert package.groups() == ('fi.uutishetki', properties['versionCode'], properties['versionName']), 'Väärän sovelluksen tai version APK'
print('APK:n applicationId, versionCode ja versionName varmennettu.')
