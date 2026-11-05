#!/usr/bin/env python3
"""Validate release metadata and extract just this version's Finnish notes."""
import argparse
import pathlib
import re
import subprocess

parser = argparse.ArgumentParser()
parser.add_argument('--check', action='store_true')
parser.add_argument('--tag')
parser.add_argument('--output')
args = parser.parse_args()

def properties(text):
    return dict(line.split('=', 1) for line in text.splitlines() if '=' in line)

version = properties(pathlib.Path('version.properties').read_text())
name, code = version['versionName'], int(version['versionCode'])
assert re.fullmatch(r'\d+\.\d+\.\d+', name), 'Virheellinen versionName'
assert code > 0, 'versionCode täytyy olla positiivinen'
notes = pathlib.Path('CHANGELOG.md').read_text()
match = re.search(r'^## ' + re.escape(name) + r'[^\n]*\n(.*?)(?=^## |\Z)', notes, re.M | re.S)
assert match and match.group(1).strip(), 'Version muutoskuvaus puuttuu'
if args.tag:
    assert args.tag in (f'v{name}', f'apk-{name}'), 'Tagi ja versionName eivät vastaa toisiaan'
    tags = subprocess.check_output(['git', 'tag', '--list'], text=True).splitlines()
    for tag in tags:
        if tag == args.tag or not re.fullmatch(r'(?:v|apk-)\d+\.\d+\.\d+', tag):
            continue
        previous = subprocess.check_output(['git', 'show', f'{tag}:version.properties'], text=True)
        assert code > int(properties(previous)['versionCode']), f'versionCode ei kasvanut tagista {tag}'
if args.output:
    pathlib.Path(args.output).write_text(match.group(1).strip() + '\n')
print(f'Versio {name} ({code}) ja muutoskuvaus kunnossa.')
