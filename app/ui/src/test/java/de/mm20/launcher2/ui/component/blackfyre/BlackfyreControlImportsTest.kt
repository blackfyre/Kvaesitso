package de.mm20.launcher2.ui.component.blackfyre

import org.junit.Assert
import org.junit.Test
import java.io.File

/**
 * Fork (blackfyre-components): every call site of a swapped Material 3 control uses the fork's
 * Blackfyre keycap instead. Fails after an upstream merge adds one; `mise run controls` fixes it.
 * Keep [Swapped] identical to SWAPPED in scripts/blackfyre_controls.py.
 */
class BlackfyreControlImportsTest {
    private val Swapped = listOf(
        "AlertDialog", "AssistChip", "Button", "FilledIconButton", "FilledTonalButton",
        "FilledTonalIconButton", "FilterChip", "FloatingActionButton", "InputChip",
        "OutlinedButton", "SmallFloatingActionButton", "ToggleButton",
    )
    private val m3 = "androidx.compose.material3"
    private val fork = "de.mm20.launcher2.ui.component.blackfyre"

    private fun sources(): List<File> {
        // Unit tests run in the module directory (app/ui).
        val root = File("src/main/java")
        Assert.assertTrue("Sources not found from ${File(".").absolutePath}", root.isDirectory)
        val forkDir = File(root, fork.replace('.', '/')).canonicalFile
        return root.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .filterNot { it.canonicalFile.startsWith(forkDir) }
            .toList()
    }

    @Test
    fun noSwappedMaterialControlIsImported() {
        val offenders = sources().flatMap { file ->
            val text = file.readText()
            Swapped.filter { Regex("^import ${Regex.escape(m3)}\\.$it$", RegexOption.MULTILINE).containsMatchIn(text) }
                .map { "${file.path}: $it" }
        }
        Assert.assertTrue("Run `mise run controls`:\n" + offenders.joinToString("\n"), offenders.isEmpty())
    }

    @Test
    fun starImportersCallTheForkControls() {
        val offenders = sources().flatMap { file ->
            val text = file.readText()
            if (!Regex("^import ${Regex.escape(m3)}\\.\\*$", RegexOption.MULTILINE).containsMatchIn(text)) {
                return@flatMap emptyList()
            }
            Swapped.filter { name ->
                Regex("(?<![\\w.])$name\\s*[({]").containsMatchIn(text) &&
                    !Regex("^import ${Regex.escape(fork)}\\.$name$", RegexOption.MULTILINE).containsMatchIn(text)
            }.map { "${file.path}: $it" }
        }
        Assert.assertTrue("Run `mise run controls`:\n" + offenders.joinToString("\n"), offenders.isEmpty())
    }
}
