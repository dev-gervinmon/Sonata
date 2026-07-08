package com.gebbers.sonata.ui.playback

data class LyricLine(
    val time: Long,
    val text: String
)

fun parseLyrics(lyrics: String?): List<LyricLine> {
    if (lyrics == null) return emptyList()
    
    val lines = mutableListOf<LyricLine>()
    // [00:12.34] Lyric text
    val regex = Regex("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})](.*)")
    
    val lrcLines = lyrics.lines()
    var isLrc = false
    
    lrcLines.forEach { line ->
        regex.find(line)?.let { match ->
            isLrc = true
            val min = match.groupValues[1].toLong()
            val sec = match.groupValues[2].toLong()
            val ms = match.groupValues[3].toLong().let { if (it < 100) it * 10 else it }
            val time = (min * 60 + sec) * 1000 + ms
            val text = match.groupValues[4].trim()
            lines.add(LyricLine(time, text))
        }
    }
    
    if (!isLrc) {
        // Fallback for plain text lyrics
        return lrcLines.map { LyricLine(0, it) }
    }
    
    return lines.sortedBy { it.time }
}
