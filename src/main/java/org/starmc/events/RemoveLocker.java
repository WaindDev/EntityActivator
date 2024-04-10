package org.starmc.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.starmc.commands.Sector;
import org.starmc.utils.ParticleUtils;

public class RemoveLocker implements Listener {

    @EventHandler
    public static void event(PlayerQuitEvent event) {
        if(Sector.getLocationLocker().containsKey(event.getPlayer())) {
            Sector.getLocationLocker().remove(event.getPlayer());
        }
    }
}
