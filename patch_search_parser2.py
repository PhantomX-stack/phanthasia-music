import re

with open('/app/app/src/main/kotlin/com/phantasia/music/network/SearchParser.kt', 'r') as file:
    content = file.read()

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
                        }
                    }
                } catch (e: Exception) {
                }
            }
        } catch (_: Exception) {}'''

content = re.sub(
    r'        try \{\n            val sections = root\.obj\("contents"\)\?\.obj\("tabbedSearchResultsRenderer"\)\n                \?\.arr\("tabs"\)\?\.idx\(0\)\?\.obj\("tabRenderer"\)\?\.obj\("content"\)\n                \?\.obj\("sectionListRenderer"\)\?\.arr\("contents"\) \?: return out\n            for \(section in sections\) \{\n                val items = section\.obj\("musicShelfRenderer"\)\?\.arr\("contents"\) \?: continue\n                for \(item in items\) \{\n                    val r = item\.obj\("musicResponsiveListItemRenderer"\) \?: continue\n                    parseItem\(r\)\?\.let \{ out\.add\(it\) \}\n                \}\n            \}\n        \} catch \(_: Exception\) \{\}',
    parse_replace,
    content
)

with open('/app/app/src/main/kotlin/com/phantasia/music/network/SearchParser.kt', 'w') as file:
    file.write(content)
