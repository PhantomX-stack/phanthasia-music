#!/usr/bin/env python3
"""
Small fixer: replace Kotlin raw Regex triple-quoted usages inside a Python triple-quoted string
with an escaped string so the Python script stays valid.

Targets: /mnt/user-data/outputs/restructure_and_rebuild.py and ./restructure_and_rebuild.py
"""
import re
from pathlib import Path

candidates = [Path('/mnt/user-data/outputs/restructure_and_rebuild.py'), Path('restructure_and_rebuild.py')]
found = False
for p in candidates:
    if not p.exists():
        continue
    found = True
    s = p.read_text(encoding='utf-8')
    before = s
    # Specific fix: LRC regex that used triple quotes
    s = s.replace('val rx = Regex("""\\[(\\d+):(\\d+)\\.(\\d+)](.*)""")',
                  'val rx = Regex("\\[(\\\\d+):(\\\\d+)\\\\.(\\\\d+)](.*)")')
    # Generic pattern: Regex("""...""") -> Regex("escaped") where we escape backslashes and quotes
    def repl_generic(m):
        inner = m.group(1)
        esc = inner.replace('\\', '\\\\').replace('"', '\\"')
        return f'Regex("{esc}")'
    s = re.sub(r'Regex\(\s*"""(.*?)"""\s*\)', repl_generic, s, flags=re.S)

    if s != before:
        p.write_text(s, encoding='utf-8')
        print(f'Patched: {p} (size {len(s)} bytes)')
    else:
        print(f'No changes needed: {p}')

if not found:
    print('No candidate files found.')
    raise SystemExit(1)

# Run a quick ast.parse check if this is a Python file
import ast
try:
    ast.parse(s)
    print('✓ Syntax valid after patch')
except SyntaxError as e:
    print(f'ERROR after patch at line {e.lineno}: {e.msg}')
    raise SystemExit(2)
