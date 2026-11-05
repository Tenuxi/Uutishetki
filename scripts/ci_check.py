#!/usr/bin/env python3
"""Expose actionable compiler/test errors as check annotations, preserving exit status."""
import subprocess
import sys
from pathlib import Path

result = subprocess.run(
    ['./gradlew', 'testDebugUnitTest', 'lintDebug', 'assembleDebug', '--no-daemon', '--console=plain'],
    stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True,
)
print(result.stdout)
if result.returncode:
    relevant = [line for line in result.stdout.splitlines()
                if any(marker in line for marker in ('e: ', 'error:', 'FAIL', 'What went wrong', 'Execution failed', '> ', 'Caused by:'))]
    diagnostic = '\n'.join(relevant)[-12000:] or result.stdout[-6000:]
    # Include JUnit failures when the Gradle summary only says tests failed.
    for path in Path('app/build/test-results').glob('**/TEST-*.xml'):
        import xml.etree.ElementTree as ET
        for failure in ET.parse(path).getroot().iter('failure'):
            diagnostic += '\n' + failure.attrib.get('message', '')[:1500]
    diagnostic = diagnostic.replace('%', '%25').replace('\r', '%0D').replace('\n', '%0A')
    print(f'::error title=Android-tarkistus::{diagnostic}')
sys.exit(result.returncode)
