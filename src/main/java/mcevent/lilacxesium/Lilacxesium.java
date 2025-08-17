package mcevent.lilacxesium;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Lilacxesium implements ModInitializer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("Lilacxesium");

    @Override
    public void onInitialize() {
        LOGGER.info("Lilacxesium main mod initializing...");
        LOGGER.info("Lilacxesium main mod initialized successfully!");
    }
}
