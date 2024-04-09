package ru.vkprofi.packetdoubler;

import com.mojang.logging.LogUtils;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Main.MODID)
public class Main {
    public static final String MODID = "PackerDoubler";
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info(MODID + " started");
    }
}
