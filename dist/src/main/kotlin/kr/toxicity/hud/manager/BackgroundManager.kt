package kr.toxicity.hud.manager

import kr.toxicity.hud.background.HudBackground
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import net.kyori.adventure.audience.Audience
import java.io.File

//TODO replace it to proper background in the future.
object BackgroundManager : BetterHudManager {
    private val backgroundMap = HashMap<String, HudBackground>()

    override fun start() {

    }

    fun getBackground(name: String) = backgroundMap[name]

    override fun reload(sender: Audience, resource: GlobalResource) {
        val folder = DATA_FOLDER.subFolder("backgrounds")
        backgroundMap.clear()
        folder.forEach {
            if (it.extension == "yml") {
                runWithExceptionHandling(sender, "Unable to load this yml: ${it.name}") {
                    val yaml = it.toYaml()
                    val name = it.nameWithoutExtension
                    val backgroundFolder = folder.subFolder(name)
                    fun getImage(imageName: String) = File(backgroundFolder, "$imageName.png")
                        .ifNotExist("this image doesn't exist: $imageName.png in $name")
                        .toImage()
                        .removeEmptyWidth()
                        .ifNull("this image is empty: $imageName.png in $name").apply {
                            PackGenerator.addTask(resource.textures + "${"background_${name}_$imageName".encodeKey()}.png") {
                                image.toByteArray()
                            }
                        }
                    backgroundMap.putSync("background", name) {
                        HudBackground(
                            it.path,
                            name,
                            getImage("left"),
                            getImage("right"),
                            getImage("body"),
                            PixelLocation(yaml)
                        )
                    }
                }
            }
        }
    }

    override fun end() {
    }
}