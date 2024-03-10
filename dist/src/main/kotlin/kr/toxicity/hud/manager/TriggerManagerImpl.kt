package kr.toxicity.hud.manager

import kr.toxicity.hud.api.manager.TriggerManager
import kr.toxicity.hud.api.trgger.HudBukkitEventTrigger
import kr.toxicity.hud.api.trgger.HudTrigger
import kr.toxicity.hud.api.update.BukkitEventUpdateEvent
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.PLUGIN
import kr.toxicity.hud.util.ifNull
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import java.util.UUID
import java.util.function.Function

object TriggerManagerImpl: BetterHudManager, TriggerManager {
    private val listener = object : Listener {}

    private val map = mutableMapOf<String, (ConfigurationSection) -> HudTrigger<*>>()

    override fun start() {

    }

    override fun addTrigger(name: String, trigger: Function<ConfigurationSection, HudTrigger<*>>) {
        map[name] = {
            trigger.apply(it)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun addTask(section: ConfigurationSection, task: (UpdateEvent, UUID) -> Boolean) {
        val clazz = section.getString("class").ifNull("class value not set.")
        when (val get = map[clazz].ifNull("unable to find this trigger: $clazz")(section)) {
            is HudBukkitEventTrigger<out Event> -> {
                Bukkit.getPluginManager().registerEvent(get.eventClass, listener, EventPriority.NORMAL, { _, e ->
                    if (get.eventClass.isAssignableFrom(e.javaClass)) {
                        val cast = get.eventClass.cast(e)
                        val t = (get as HudBukkitEventTrigger<Event>)
                        t.getValue(cast)?.let { uuid ->
                            val wrapper = BukkitEventUpdateEvent(cast, get.getKey(cast))
                            task(wrapper, uuid)
                        }
                    }
                }, PLUGIN)
            }
        }
    }

    override fun reload(resource: GlobalResource) {
        HandlerList.unregisterAll(listener)
    }
    override fun end() {
    }
}