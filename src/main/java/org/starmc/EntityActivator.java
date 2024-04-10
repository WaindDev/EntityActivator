package org.starmc;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BoundingBox;
import org.javatuples.Pair;
import org.starmc.commands.Sector;
import org.starmc.commands.SectorTabCompeleter;
import org.starmc.events.HideEntities;
import org.starmc.events.RemoveLocker;
import org.starmc.utils.Configurations;
import org.starmc.utils.ShowEntitiesRunnable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EntityActivator extends JavaPlugin {

    @Getter
    private static EntityActivator instance;

    @Getter
    private static YamlConfiguration yaml;

    @Getter
    private static final List<EntityType> staticEntityTypes = new ArrayList<>();

    @Getter
    private static final List<ShowEntitiesRunnable> taskList = new ArrayList<>();

    @Getter
    private static final List<String> allowedWorlds = new ArrayList<>();

    @Override
    public void onEnable() {
        instance = this;

        yaml = YamlConfiguration.loadConfiguration(Configurations.SECTORS.getFile());

        try {
            for(var strEntity : yaml.getStringList("static_entities")) {
                var entityType = EntityType.valueOf(strEntity);
                staticEntityTypes.add(entityType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for(var world : yaml.getStringList("allowed_worlds")) {
            if(Bukkit.getWorld(world) != null) allowedWorlds.add(world);
        }

        getServer().getPluginManager().registerEvents(new HideEntities(), this);
        createRunnables();

        getServer().getPluginCommand("sector").setExecutor(new Sector());
        getServer().getPluginCommand("sector").setTabCompleter(new SectorTabCompeleter());
        getServer().getPluginManager().registerEvents(new RemoveLocker(), this);
    }

    private void createRunnables() {
        var sectorCS = yaml.getConfigurationSection("sectors");
        for(String section : sectorCS.getKeys(false)) {
            String firstPoint = sectorCS.getString(section + "." + "first_point");
            String secondPoint = sectorCS.getString(section + "." + "second_point");

            var firstPointLoc = firstPoint.strip().split(",");
            var secondPointLoc = secondPoint.strip().split(",");
            var world = Bukkit.getWorld(sectorCS.getString(section + "." + "world"));

            if(!allowedWorlds.contains(world.getName())) continue;

            ShowEntitiesRunnable task = new ShowEntitiesRunnable(world,
                    toBoundingBox(
                            fromStringArray(world, firstPointLoc),
                            fromStringArray(world, secondPointLoc)
                    ),
                    section
            );
            task.runTaskTimer(this, 0L, 20L);
            taskList.add(task);
        }
    }

    public static void removeSectorRunnable(String name) {
        for(var runnable : taskList) {
            if(runnable.getSectorName().equals(name)) runnable.stop();
        }
    }

    public static YamlConfiguration createSector(String name, Location loc1, Location loc2) {
        var sectorCS = yaml.getConfigurationSection("sectors");
        var section = sectorCS.createSection(name);
        section.set("world", loc1.getWorld().getName());
        section.set("first_point", locToString(loc1));
        section.set("second_point", locToString(loc2));
        try {
            yaml.save(Configurations.SECTORS.getFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
        ShowEntitiesRunnable task = new ShowEntitiesRunnable(loc1.getWorld(),
                toBoundingBox(
                        loc1,
                        loc2
                ),
                name
        );
        task.loadSectorEntities();
        task.runTaskTimer(EntityActivator.getInstance(), 0L, 20L);
        taskList.add(task);

        return yaml;
    }

    private static String locToString(Location location) {
        StringBuilder builder = new StringBuilder();
        builder.append(location.getBlockX()).append(",").append(location.getBlockY()).append(",").append(location.getBlockZ());
        return builder.toString();
    }

    public static Location fromStringArray(World world, String[] str) {
        return new Location(world, Integer.parseInt(str[0]), Integer.parseInt(str[1]), Integer.parseInt(str[2]));
    }

    private static BoundingBox toBoundingBox(Location from, Location to) {
        var block1 = from.getBlock();
        var block2 = to.getBlock();

        return BoundingBox.of(block1, block2);
    }

    @Override
    public void onDisable() {

    }
}
