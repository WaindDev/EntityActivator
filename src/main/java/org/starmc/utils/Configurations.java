package org.starmc.utils;

import lombok.Getter;
import org.starmc.EntityActivator;
import org.starmc.StarMC;

import java.io.File;

public enum Configurations {

    SECTORS(StarMC.getConfigManager().getFile(EntityActivator.getInstance(), "sectors.yml")),
    MESSAGES(StarMC.getConfigManager().getFile(EntityActivator.getInstance(), "messages.yml"));

    @Getter
    private final File file;

    Configurations(File file) {
        this.file = file;
    }
}
