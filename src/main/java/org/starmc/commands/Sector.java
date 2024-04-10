package org.starmc.commands;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.starmc.EntityActivator;
import org.starmc.utils.Configurations;
import org.starmc.utils.Messages;
import org.starmc.utils.ParticleUtils;

import java.util.*;

public class Sector implements CommandExecutor {

    @Getter
    private static final Map<Player, Location> locationLocker = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        YamlConfiguration messages =
                YamlConfiguration.loadConfiguration(Configurations.MESSAGES.getFile());

        if(!(sender instanceof Player player)) {
            Messages.sendMessage(sender, messages.getString("not-player"));
            return true;
        }

        if(!player.hasPermission(messages.getString("sector-command-permission"))) {
            Messages.sendMessage(player, messages.getString("not-have-permission"));
            return true;
        }

        YamlConfiguration sectors =
                YamlConfiguration.loadConfiguration(Configurations.SECTORS.getFile());

        if(args.length == 1) {
            if(args[0].equalsIgnoreCase("cancel")) {
                if(!locationLocker.containsKey(player)) {
                    Messages.sendMessage(player, messages.getString("not-creating-sector"));
                    return true;
                }

                locationLocker.remove(player);
                Messages.sendMessage(player, messages.getString("cancel-succ"));
                return true;
            } else if(args[0].equalsIgnoreCase("list")) {
                Messages.sendMessage(player, messages.getString("sector-list"));

                for(var sectorName : sectors.getConfigurationSection("sectors").getKeys(false)) {
                    Messages.sendMessage(player, messages.getString("sector-element").formatted(sectorName));
                }
                return true;
            } else if(args[0].equalsIgnoreCase("hide")) {
                if(!ParticleUtils.getRunnables().containsKey(player)) {
                    Messages.sendMessage(player, messages.getString("selected-sector-is-null"));
                    return true;
                }

                ParticleUtils.getRunnables().get(player).cancel();
                ParticleUtils.getRunnables().remove(player);
                Messages.sendMessage(player, messages.getString("sector-hide"));

                return true;
            }

            Messages.sendMessage(player, messages.getStringList("help-message"));
            return true;
        } else if(args.length == 0) {
            Messages.sendMessage(player, messages.getStringList("help-message"));
            return true;
        }

        if(args[0].equalsIgnoreCase("create")) {

            if (locationLocker.get(player) == null) {
                locationLocker.put(player, player.getLocation());
                Messages.sendMessage(player, messages.getString("first-point-succ"));
            } else {
                if (!locationLocker.get(player).getWorld().getName().equals(player.getWorld().getName())) {
                    locationLocker.remove(player);
                    Messages.sendMessage(player, messages.getString("not-same-world"));
                    return true;
                }

                String sectorName = "";

                for(int i=1;i<args.length;i++) {
                    if (args[i].contains(".")) {
                        Messages.sendMessage(player, messages.getString("not-valid-name"));
                        return true;
                    }
                    if(args.length-1 == i) {
                        sectorName += args[i];
                        break;
                    }
                    sectorName += args[i] + " ";
                }

                if (sectors.getConfigurationSection("sectors." + sectorName) != null) {
                    Messages.sendMessage(player, messages.getString("sector-already-exists"));
                    return true;
                }

                sectors = EntityActivator.createSector(sectorName, locationLocker.get(player), player.getLocation());
                locationLocker.remove(player);
                Messages.sendMessage(player, messages.getString("sector-created"));
            }
            return true;
        } else if(args[0].equalsIgnoreCase("remove")) {
            String sectorName = "";

            for(int i=1;i<args.length;i++) {
                if (args[i].contains(".")) {
                    Messages.sendMessage(player, messages.getString("not-valid-name"));
                    return true;
                }
                if(args.length-1 == i) {
                    sectorName += args[i];
                    break;
                }
                sectorName += args[i] + " ";
            }

            if(sectors.getConfigurationSection("sectors." + sectorName) == null) {
                Messages.sendMessage(player, messages.getString("sector-not-exists"));
                return true;
            }

            sectors.set("sectors." + sectorName, null);
            try {
                sectors.save(Configurations.SECTORS.getFile());
            } catch (Exception e) {
                e.printStackTrace();
            }
            EntityActivator.removeSectorRunnable(sectorName);
            Messages.sendMessage(player, messages.getString("sector-removed"));
            return true;
        } else if(args[0].equalsIgnoreCase("show")) {
            String sectorName = "";

            for(int i=1;i<args.length;i++) {
                if (args[i].contains(".")) {
                    Messages.sendMessage(player, messages.getString("not-valid-name"));
                    return true;
                }
                if(args.length-1 == i) {
                    sectorName += args[i];
                    break;
                }
                sectorName += args[i] + " ";
            }

            if(sectors.getConfigurationSection("sectors." + sectorName) == null) {
                Messages.sendMessage(player, messages.getString("sector-not-exists"));
                return true;
            }
            if(ParticleUtils.getRunnables().containsKey(player)) {
                ParticleUtils.getRunnables().get(player).cancel();
                ParticleUtils.getRunnables().remove(player);
            }

            var world = Bukkit.getWorld(sectors.getString("sectors." + sectorName + ".world"));
            var loc1 = EntityActivator.fromStringArray(
                    world,
                    sectors.getString("sectors." + sectorName + ".first_point").split(",")
            );
            var loc2 = EntityActivator.fromStringArray(
                    world,
                    sectors.getString("sectors." + sectorName + ".second_point").split(",")
            );
            ParticleUtils.spawnOnEdges(
                    BoundingBox.of(world.getBlockAt(loc1), world.getBlockAt(loc2)),
                    player,
                    Particle.VILLAGER_HAPPY,
                    sectorName
            );

            Messages.sendMessage(player, messages.getString("sector-show"));
            return true;
        }

        Messages.sendMessage(player, messages.getString("not-correct-args"));
        return true;
    }
}
