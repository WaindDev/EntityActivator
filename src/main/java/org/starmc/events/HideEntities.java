package org.starmc.events;

import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.starmc.EntityActivator;

public class HideEntities implements Listener {

    @EventHandler
    public static void event(PlayerChunkLoadEvent event) {

        if(!EntityActivator.getAllowedWorlds().contains(event.getChunk().getWorld().getName())) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                var player = event.getPlayer();
                for (var entity : event.getChunk().getEntities()) {
                    if (!EntityActivator.getStaticEntityTypes().contains(entity.getType())) continue;
                    player.hideEntity(EntityActivator.getInstance(), entity);
                }

                for(var task : EntityActivator.getTaskList()) {
                    if(task.getEntitiesInBox().isEmpty()) task.loadSectorEntities();
                }
            }
        }.runTaskLater(EntityActivator.getInstance(), 20L);
    }
}
