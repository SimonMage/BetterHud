package kr.toxicity.hud.util

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import kotlin.math.abs

val SPACE_KEY = Key.key("$NAME_SPACE:space")
val LEGACY_SPACE_KEY = Key.key("$NAME_SPACE:legacy_space")
val BOSS_BAR_KEY = Key.key("$NAME_SPACE:bossbar")

val MINI_MESSAGE = MiniMessage.builder()
    .tags(TagResolver.standard())
    .postProcessor {
        var style = it.style()
        if (style.color() == null) style = style.color(NamedTextColor.WHITE)
        it.style(style.decorations(TextDecoration.entries.associateWith { deco ->
            if (style.hasDecoration(deco)) TextDecoration.State.TRUE else TextDecoration.State.FALSE
        }))
    }
    .build()

val DEFAULT_TEXT_DECORATION = TextDecoration.entries.associateWith {
    TextDecoration.State.FALSE
}

fun String.deserializeToComponent() = MINI_MESSAGE.deserialize(this)
fun String.toComponent() = Component.text(this).color(NamedTextColor.WHITE).decorations(DEFAULT_TEXT_DECORATION)

val EMPTY_COMPONENT: Component = Component.empty()
val EMPTY_WIDTH_COMPONENT = WidthComponent(Component.empty().color(NamedTextColor.WHITE), 0)
val EMPTY_PIXEL_COMPONENT = PixelComponent(EMPTY_WIDTH_COMPONENT, 0)
val NEW_LAYER = WidthComponent(Component.text(0xC0000.parseChar()).font(SPACE_KEY), 0)
val NEGATIVE_ONE_SPACE_COMPONENT = WidthComponent(Component.text((0xD0000 - 1).parseChar()).font(SPACE_KEY), 0)
val FORWARD_ONE_SPACE_COMPONENT = WidthComponent(Component.text((0xD0000 + 1).parseChar()).font(SPACE_KEY), 0)

fun WidthComponent.toPixelComponent(pixel: Int) = PixelComponent(this, pixel)

fun Int.parseChar(): String {
    return if (this <= 0xFFFF) this.toChar().toString()
    else {
        val t = this - 0x10000
        return "${((t ushr 10) + 0xD800).toChar()}${((t and 1023) + 0xDC00).toChar()}"
    }
}

fun Int.toSpaceComponent() = toSpaceComponent(this)
fun Int.toSpaceComponent(width: Int) = if (VERSION.version <= 18) {
    WidthComponent(Component.text((this + 0xFFC00).parseChar()).font(LEGACY_SPACE_KEY), width)
} else {
    WidthComponent(Component.text((this + 0xD0000).parseChar()).font(SPACE_KEY), width)
}