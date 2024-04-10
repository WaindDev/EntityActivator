package org.starmc.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.starmc.utils.Configurations;

import java.util.ArrayList;
import java.util.List;

public class SectorTabCompeleter implements TabCompleter {

    private static String[] completions = { "help", "cancel", "create", "remove", "list", "show", "hide" };

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        final List<String> otherCompletions = new ArrayList<>();

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(Configurations.SECTORS.getFile());
        if(args.length > 1) {
            List<String> sectorNames = new ArrayList<>();
            for(String name : yaml.getConfigurationSection("sectors").getKeys(false)) {
                sectorNames.add(name);
            }
            otherCompletions.clear();
            otherCompletions.addAll(sectorNames);
        }

        StringUtil.copyPartialMatches(args[0], List.of(completions), otherCompletions);
        return otherCompletions;
    }
}
