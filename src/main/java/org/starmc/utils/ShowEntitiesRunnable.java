package org.starmc.utils;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.starmc.EntityActivator;

import java.util.ArrayList;
import java.util.List;

public class ShowEntitiesRunnable extends BukkitRunnable {

    @NotNull
    private final List<Player> playersInBox = new ArrayList<>();

    @NotNull
    @Getter
    private final List<Entity> entitiesInBox = new ArrayList<>();

    @NotNull
    private final World world;

    @NotNull
    private final BoundingBox box;

    @NotNull
    private final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(Configurations.SECTORS.getFile());

    @NotNull
    @Getter
    private final String sectorName;

    public ShowEntitiesRunnable(@NotNull World world, @NotNull BoundingBox box, @NotNull String sectorName) {
        this.world = world;
        this.box = box;
        this.sectorName = sectorName;
    }

    public void loadSectorEntities() {
        for(var entity : world.getEntities()) {
            if(box.contains(entity.getLocation().toVector())) {
                if(EntityActivator.getStaticEntityTypes().contains(entity.getType())) {
                    entitiesInBox.add(entity);
                }
            }
        }
    }

    public void stop() {
        cancel();

        for(var entity : entitiesInBox) {
            for(var players : playersInBox) {
                players.hideEntity(EntityActivator.getInstance(), entity);
            }
        }
    }

    @Override
    public void run() {
        for(var player : world.getPlayers()) {
            if(player == null) continue;
            if(!player.isOnline()) continue;

            if(playersInBox.contains(player)) {
                if(player == null || !player.isOnline()) {
                    playersInBox.remove(player);
                    continue;
                }

                if(!box.contains(player.getLocation().toVector())) {
                    playersInBox.remove(player);
                    for(var entity : entitiesInBox) {
                        if(player.canSee(entity)) player.hideEntity(EntityActivator.getInstance(), entity);
                    }
                }
                continue;
            }

            if(box.contains(player.getLocation().toVector())) {
                playersInBox.add(player);
            }
        }

        for(var player : playersInBox) {
            if(player == null || !player.isOnline()) continue;

            for(var entity : entitiesInBox) {
                if(!player.canSee(entity)) player.showEntity(EntityActivator.getInstance(), entity);
            }
        }
    }
}
