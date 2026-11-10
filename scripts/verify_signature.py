#!/usr/bin/env python3
"""Check apksigner --print-certs output against the pinned public certificate."""
import pathlib
import re
import sys


def verify_signature(report, expected):
    expected = expected.strip().lower()
    if not re.fullmatch(r'[0-9a-f]{64}', expected):
        raise ValueError('Odotettu SHA-256-varmenne puuttuu tai on virheellinen.')
    digests = re.findall(r'^Signer #\d+ certificate SHA-256 digest: ([0-9a-fA-F]+)\s*$', report, re.M)
    if len(digests) != 1 or digests[0].lower() != expected:
        raise ValueError('APK:n allekirjoitusvarmenne ei vastaa pysyvää julkaisuvarmennetta. Julkaisu keskeytetty.')


if __name__ == '__main__':
    try:
        verify_signature(pathlib.Path(sys.argv[1]).read_text(), pathlib.Path('config/release-certificate-sha256.txt').read_text())
    except (ValueError, OSError) as error:
        sys.exit(str(error))
    print('APK:n pysyvä SHA-256-allekirjoitusvarmenne varmennettu.')
