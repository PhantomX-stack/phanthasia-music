#!/usr/bin/env python3
"""
Patch the generated `restructure_and_rebuild.py` by replacing Kotlin raw-string
Regex usages (triple-quoted strings using three double-quotes) with normal
escaped string literals so the Python script remains syntactically valid
when it embeds Kotlin code.

Usage: python3 scripts/patch_restructure_script.py
If the generated script is found under /mnt/user-data/outputs/restructure_and_rebuild.py
it will be patched and written to ./restructure_and_rebuild.py in the workspace.
"""
import re
from pathlib import Path
import ast
import sys

candidates = [Path('/mnt/user-data/outputs/restructure_and_rebuild.py'), Path('restructure_and_rebuild.py')]
src_path = None
for p in candidates:
    if p.exists():
        src_path = p
        break

if not src_path:
    print('No source file found at /mnt/user-data/outputs/restructure_and_rebuild.py or ./restructure_and_rebuild.py')
    print('Place the generated script at one of those paths and re-run this patcher.')
    sys.exit(1)

text = src_path.read_text(encoding='utf-8')

def replace_triple_regex(s: str) -> str:
    # Replace Regex("""...""") patterns with Regex("...") where backslashes are escaped
    def repl(m):
        inner = m.group(1)
        esc = inner.replace('\\', '\\\\').replace('"', '\\"')
        return 'Regex("' + esc + '")'
    s2 = re.sub(r'Regex\(\s*"""(.*?)"""\s*\)', repl, s, flags=re.S)

    # Also handle occurrences of triple-quoted strings used with Regex through concatenation
    s2 = s2.replace('Regex("""', 'Regex("').replace('""")', '")')
    return s2

fixed = replace_triple_regex(text)

out = Path('restructure_and_rebuild.py')
out.write_text(fixed, encoding='utf-8')
print(f'Wrote patched file: {out} ({len(fixed)} bytes)')

try:
    ast.parse(fixed)
    print('✓ Python syntax valid for patched file')
except SyntaxError as e:
    print(f'ERROR: SyntaxError at line {e.lineno}: {e.msg}')
    # Print a small context snippet for debugging
    lines = fixed.splitlines()
    for i in range(max(0, e.lineno-3), min(len(lines), e.lineno+2)):
        mark = '>>>' if i+1 == e.lineno else '   '
        print(f"{mark} {i+1}: {lines[i]}")
    sys.exit(2)
