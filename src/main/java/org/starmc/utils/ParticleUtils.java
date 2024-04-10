package org.starmc.utils;

import lombok.Getter;
import org.bukkit.Particle;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.starmc.EntityActivator;

import java.util.HashMap;
import java.util.Map;

public class ParticleUtils {

    @Getter
    private static final Map<Player, ParticleRunnable> runnables = new HashMap<>();

    public static void spawnOnEdges(BoundingBox box, Player player, Particle particle, String sectorName) {
        var task = new ParticleRunnable(box, player, particle, sectorName);
        task.runTaskTimer(EntityActivator.getInstance(), 0L, 20L);
        runnables.put(player, task);
    }

    public static class ParticleRunnable extends BukkitRunnable {

        @Getter
        @NotNull
        private final BoundingBox box;

        @Getter
        @NotNull
        private final Player player;

        @Getter
        @NotNull
        private final Particle particle;

        @Getter
        @NotNull
        private final String sectorName;

        public ParticleRunnable(
                @NotNull BoundingBox box,
                @NotNull Player player,
                @NotNull Particle particle,
                @NotNull String sectorName
        ) {
            this.box = box;
            this.player = player;
            this.particle = particle;
            this.sectorName = sectorName;
        }

        @Override
        public void run() {
            if(player == null) {
                runnables.remove(player);
                cancel();
            }
            if(!player.isOnline()) {
                runnables.remove(player);
                cancel();
            }
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(Configurations.SECTORS.getFile());
            if(yaml.getConfigurationSection("sectors." + sectorName) == null) {
                runnables.remove(player);
                cancel();
            }

            // max angle
            for(double i=box.getMaxX();i>=box.getMinX();i-=1) {
                player.spawnParticle(particle, i, box.getMinY(), box.getMaxZ(), 1);
                player.spawnParticle(particle, i, box.getMaxY(), box.getMaxZ(), 1);
            }
            for(double i=box.getMaxZ();i>=box.getMinZ();i-=1) {
                player.spawnParticle(particle, box.getMaxX(), box.getMinY(), i, 1);
                player.spawnParticle(particle, box.getMaxX(), box.getMaxY(), i, 1);
            }

            // min angle
            for(double i=box.getMinX();i<=box.getMaxX();i+=1) {
                player.spawnParticle(particle, i, box.getMinY(), box.getMinZ(), 1);
                player.spawnParticle(particle, i, box.getMaxY(), box.getMinZ(), 1);
            }

            for(double i=box.getMinZ();i<=box.getMaxZ();i+=1) {
                player.spawnParticle(particle, box.getMinX(), box.getMinY(), i, 1);
                player.spawnParticle(particle, box.getMinX(), box.getMaxY(), i, 1);
            }

            for(double i=box.getMinY();i<=box.getMaxY();i+=1) {
                player.spawnParticle(particle, box.getMaxX(), i, box.getMaxZ(), 1);
                player.spawnParticle(particle, box.getMinX(), i, box.getMinZ(), 1);
                player.spawnParticle(particle, box.getMaxX(), i, box.getMinZ(), 1);
                player.spawnParticle(particle, box.getMinX(), i, box.getMaxZ(), 1);
            }
        }
    }
}
