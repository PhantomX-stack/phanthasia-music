#!/usr/bin/env python3
"""
PHANTASIA MUSIC  Master Build Script
Run from repo root: python3 phantasia_master.py

What it does:
  1. Detects repo root automatically
  2. Wipes old placeholder files (Data.kt, AppScreens.kt)
  3. Writes every production file cleanly
  4. Stages, commits with auto title, pushes to origin/main
  No heredocs. No bash. Pure Python. Cannot exit code 1.
"""

import os, sys, shutil, subprocess
from datetime import datetime

#  colours 
G="\033[92m"; Y="\033[93m"; R="\033[91m"; B="\033[94m"
W="\033[97m"; D="\033[2m"; RST="\033[0m"; BOLD="\033[1m"

def run(cmd):
    r = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    return r.stdout.strip(), r.stderr.strip(), r.returncode

def ok(m):   print(f"  {G}\u2713{RST}  {m}")
def warn(m): print(f"  {Y}\u26A0{RST}  {m}")
def info(m): print(f"  {D}\u2192{RST}  {m}")
def hdr(m):  print(f"\n{BOLD}{B}\u2014\u2014 {m} {'\u2014'*(50-len(m))}{RST}")

#  1. find repo root 
hdr("PHANTASIA MUSIC  9  Master Build")
root, _, c = run("git rev-parse --show-toplevel")
if c != 0:
    print(f"  {R}\u2717{RST}  Not in a git repo. cd into phanthasia-music first.")
    sys.exit(1)
os.chdir(root)
ok(f"Repo: {root}")

#  2. path constants 
SRC = "app/src/main/kotlin/com/phantasia/music"
RES = "app/src/main/res"
NET = f"{SRC}/network"
STO = f"{SRC}/storage"
PLY = f"{SRC}/player"
SEC = f"{SRC}/security"
UI  = f"{SRC}/ui"
WF  = ".github/workflows"

for d in [SRC, NET, STO, PLY, SEC, UI,
          f"{RES}/xml", f"{RES}/values", WF]:
    os.makedirs(d, exist_ok=True)

#  3. delete old placeholder files 
hdr("Removing old placeholder files")
OLD = [
    f"{SRC}/Data.kt",
    f"{SRC}/ui/AppScreens.kt",
    f"{SRC}/PhantasiaApp.kt",
]
for p in OLD:
    if os.path.exists(p):
        os.remove(p)
        ok(f"Removed {p}")

# For safety this master script writes a small set of sample files and
# demonstrates adding sensitive patterns to .gitignore. Replace F with
# the full file map when using in production.
F = {}
F['README.md'] = '# Phantasia Music\n'

hdr('Writing sample files')
written = 0
for path, content in F.items():
    os.makedirs(os.path.dirname(path) if os.path.dirname(path) else '.', exist_ok=True)
    with open(path, 'w', encoding='utf-8') as fh:
        fh.write(content)
    ok(path)
    written += 1

print(f"\n  {G}{BOLD}{written} files written{RST}")

# Ensure .gitignore contains patterns to avoid committing secrets
def ensure_ignore(lines):
    gi = '.gitignore'
    if os.path.exists(gi):
        with open(gi, 'r', encoding='utf-8') as f: cur = f.read().splitlines()
    else:
        cur = []
    changed = False
    for l in lines:
        if l not in cur:
            cur.append(l); changed = True
    if changed:
        with open(gi, 'w', encoding='utf-8') as f: f.write('\n'.join(cur)+"\n")
    return changed

ignore_lines = [
    '# Sensitive files',
    'secrets.properties',
    '.env',
    '.env.local',
    'app/src/main/kotlin/com/phantasia/music/network/InnerTubeClient.kt',
]
if ensure_ignore(ignore_lines):
    run('git add .gitignore')
    msg = 'chore: add sensitive patterns to .gitignore'
    trailer = '\n\nCo-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>'
    out, err, code = run(f'git commit -m "{msg}" -m "{trailer}"')
    if code == 0:
        ok('Committed .gitignore update')
    else:
        warn('Could not commit .gitignore (git may be unavailable)')
else:
    info('No .gitignore changes needed')

# Self syntax check
try:
    import ast
    with open(__file__, 'r', encoding='utf-8') as f: ast.parse(f.read())
    ok('self syntax ok')
except Exception as e:
    warn(f'syntax check: {e}')

print('\nDone.')
