#!/usr/bin/env kotlin

/**
 * This script is used to update the Android Studio releases page.
 * At first, it fetches the list of Android Studio updates from the official `updates.xml` file.
 * Parsed list is used to generate the Markdown table.
 * The actual IntelliJ IDEA release version is obtained with the help of the JetBrains Data Services API.
 */
@file:DependsOn("it.skrape:skrapeit:1.2.1")
@file:DependsOn("org.jsoup:jsoup:1.13.1")
@file:DependsOn("net.swiftzer.semver:semver:1.1.2")
@file:DependsOn("org.simpleframework:simple-xml:2.7.1")
@file:DependsOn("org.json:json:20211205")

import it.skrape.core.htmlDocument
import net.swiftzer.semver.SemVer
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import org.simpleframework.xml.core.Persister
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.zip.ZipFile

val RELEASES_FILE_PATH_MD = "topics/_generated/android_studio_releases.md"
val RELEASES_FILE_PATH_XML = "topics/_generated/android_studio_releases.xml"
val INTELLIJ_RELEASES = "https://www.jetbrains.com/intellij-repository/releases/"
val ANDROID_STUDIO_HOST = "https://developer.android.com"

val platformBuildToVersionMapping = INTELLIJ_RELEASES.fetch { content ->
  htmlDocument(content) {
    findAll("h2:contains(com.jetbrains.intellij.idea) + table tbody tr").mapNotNull { tr ->
      val (version, build) = tr.findAll("td:nth-child(odd)").map { SemVer.parse(it.text) }
      (build to version).takeIf { version.major > 2000 }
    }.toMap().toSortedMap()
  }
}

val frameUrl = "$ANDROID_STUDIO_HOST/studio/archive".fetch { content ->
  htmlDocument(content) {
    findFirst("devsite-iframe iframe[src]").attribute("src")
  }
}.let { "$ANDROID_STUDIO_HOST/$it" }

frameUrl.fetch { content ->
  val contentFile = file(RELEASES_FILE_PATH_XML)
  val current = contentFile.takeIf { it.length() > 0 }?.let {
    Persister().read(Content::class.java, it)
  } ?: Content()

  val nameToPlatformBuildMapping = current.items.associate {
    it.name to it.platformBuild
  }

  htmlDocument(content) {
    findAll("section.expandable").take(5).map { item ->
      val title = item.findFirst("p").text
      val (name, version, channel, date) =
              """^([\w ]+ \(?([\d.]+)\)? ?(?:(\w+) \d+)?) (\w+ \d+, \d+)$""".toRegex()
                      .find(title)?.groupValues?.drop(1)
                      ?: emptyList()

      println("# $name")
      val platformBuild = nameToPlatformBuildMapping[name]?.let(SemVer::parse) ?: run {
        item.findFirst(".downloads a[href$=.zip]").attribute("href").resolveBuild()
      }
      val platformVersion = platformBuildToVersionMapping[platformBuild] ?: run {
        platformBuildToVersionMapping.entries.find { it.value < platformBuild }?.value
      }

      println("  version='${version}'")
      println("  platformBuild='${platformBuild}'")
      println("  platformVersion='${platformVersion}'")

      Item(name, version, channel.lowercase(), platformBuild.toString(), platformVersion.toString(), date)
    }.let { Content(current.version + 1, it) }
  }.also {
    Persister().write(it, contentFile)
  }.also { (_, items) ->
    """
    <chunk id="releases_table">

    | Android Studio | Channel | Release Date | IntelliJ IDEA Build Number | IntelliJ IDEA Release Version |
    |----------------|---------|--------------|----------------------------|-------------------------------|

    """.trimIndent() + items.joinToString("\n") {
      "| ${it.name} | ${it.channel} | ${it.date} | ${it.platformBuild} | ${it.platformVersion} |"
    } + "\n\n</chunk>".let {
      file(RELEASES_FILE_PATH_MD).writeText(it)
    }
  }
}

fun <T> String.fetch(block: (String) -> T) = URL(this).openStream().use { inputStream ->
  block(inputStream.readBytes().toString(Charsets.UTF_8))
}

fun <T> String.download(block: (File) -> T) = URL(this).openStream().use { inputStream ->
  BufferedInputStream(inputStream).use { bis ->
    File.createTempFile("android-studio", ".zip").also(File::deleteOnExit).let { tempFile ->
      FileOutputStream(tempFile).use { outputStream ->
        println("  Downloading $this to $tempFile")
        val data = ByteArray(1024)
        var count: Int
        while (bis.read(data, 0, data.size).also { count = it } != -1) {
          outputStream.write(data, 0, count)
        }
      }
      block(tempFile)
    }
  }
}

fun String.resolveBuild() = download { file ->
  ZipFile(file).use { zip ->
    zip.getEntry("android-studio/build.txt").let { entry ->
      zip.getInputStream(entry).use { inputStream ->
        inputStream.readBytes().toString(Charsets.UTF_8)
      }
    }.split("-").last().split(".").take(3).joinToString(".").let(SemVer.Companion::parse)
  }.also {
    file.delete()
  }
}

fun file(path: String) = File(System.getenv("GITHUB_WORKSPACE") ?: "../../").resolve(path).also(File::createNewFile)

@Root(strict = false)
data class Content(
        @field:Attribute var version: Int = 1,
        @field:ElementList(inline = true, required = false) var items: List<Item> = mutableListOf(),
)

data class Item(
        var name: String = "",
        var version: String = "",
        var channel: String = "",
        var platformBuild: String? = null,
        var platformVersion: String? = null,
        var date: String = "",
)