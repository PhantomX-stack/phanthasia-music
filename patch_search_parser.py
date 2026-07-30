import re

with open('/app/app/src/main/kotlin/com/phantasia/music/network/SearchParser.kt', 'r') as file:
    content = file.read()

# Make the internal loops inside parse() robust so one malformed item doesn't crash the whole list
parse_block = r'''        try {
            val sections = root.obj("contents")\?\.obj\("tabbedSearchResultsRenderer"\)
                \?\.arr\("tabs"\)\?\.idx\(0\)\?\.obj\("tabRenderer"\)\?\.obj\("content"\)
                \?\.obj\("sectionListRenderer"\)\?\.arr\("contents"\) \?: return out
            for \(section in sections\) {
                val items = section\.obj\("musicShelfRenderer"\)\?\.arr\("contents"\) \?: continue
                for \(item in items\) {
                    val r = item\.obj\("musicResponsiveListItemRenderer"\) \?: continue
                    parseItem\(r\)\?\.let \{ out\.add\(it\) \}
                }
            }
        } catch \(_: Exception\) \{\}'''

parse_replace = '''        try {
            val sections = root.obj("contents")?.obj("tabbedSearchResultsRenderer")
                ?.arr("tabs")?.idx(0)?.obj("tabRenderer")?.obj("content")
                ?.obj("sectionListRenderer")?.arr("contents") ?: return out
            for (section in sections) {
                try {
                    val items = section.obj("musicShelfRenderer")?.arr("contents") ?: continue
                    for (item in items) {
                        try {
                            val r = item.obj("musicResponsiveListItemRenderer") ?: continue
                            parseItem(r)?.let { out.add(it) }
                        } catch (e: Exception) {
                            // Skip malformed item
                        }
                    }
                } catch (e: Exception) {
                    // Skip malformed section
                }
            }
        } catch (_: Exception) {}'''

content = re.sub(parse_block, parse_replace, content)

with open('/app/app/src/main/kotlin/com/phantasia/music/network/SearchParser.kt', 'w') as file:
    file.write(content)
