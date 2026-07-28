with open('app/src/main/kotlin/com/phantasia/music/ui/LibraryScreen.kt', 'r') as f:
    content = f.read()

if content.endswith('}\n}'):
    content = content[:-2]
    with open('app/src/main/kotlin/com/phantasia/music/ui/LibraryScreen.kt', 'w') as f:
        f.write(content)
