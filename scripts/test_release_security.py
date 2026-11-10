#!/usr/bin/env python3
import unittest
from prepare_signing import signing_material, REQUIRED
from verify_apk import verify_metadata
from verify_signature import verify_signature


class ReleaseSecurityTest(unittest.TestCase):
    def test_missing_or_invalid_secrets_fail_closed(self):
        valid = {'KEYSTORE_BASE64': 'a2V5', 'SIGNING_STORE_PASSWORD': 'test-password',
                 'SIGNING_KEY_ALIAS': 'uutishetki-release', 'SIGNING_KEY_PASSWORD': 'test-password'}
        self.assertEqual(signing_material(valid), b'key')
        for name in REQUIRED:
            with self.assertRaises(ValueError):
                signing_material({**valid, name: ''})
        for change in [{'KEYSTORE_BASE64': '!!'}, {'SIGNING_KEY_ALIAS': 'androiddebugkey'}]:
            with self.assertRaises(ValueError):
                signing_material({**valid, **change})

    def test_only_expected_single_certificate_is_accepted(self):
        fingerprint = 'a1' * 32
        verify_signature('Signer #1 certificate SHA-256 digest: ' + fingerprint, fingerprint)
        for report in ['', 'Signer #1 certificate SHA-256 digest: ' + 'b2' * 32,
                       'Signer #1 certificate SHA-256 digest: ' + fingerprint + '\nSigner #2 certificate SHA-256 digest: ' + fingerprint]:
            with self.assertRaises(ValueError):
                verify_signature(report, fingerprint)
        with self.assertRaises(ValueError):
            verify_signature('Signer #1 certificate SHA-256 digest: ' + fingerprint, '')

    def test_only_intended_release_package_is_accepted(self):
        version = {'versionCode': '8', 'versionName': '0.5.1'}
        valid = "package: name='fi.uutishetki' versionCode='8' versionName='0.5.1'"
        verify_metadata(valid, version)
        for bad in [valid + '\napplication-debuggable', valid.replace("'8'", "'7'"),
                    valid.replace('fi.uutishetki', 'fi.other'), valid.replace('0.5.1', '0.5.0'), '']:
            with self.assertRaises(ValueError):
                verify_metadata(bad, version)


if __name__ == '__main__':
    unittest.main()
