package net.succ.dimensionallock;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;

import java.util.stream.Collectors;

@Mod(DimensionalLock.MODID)
public class DimensionalLock {
    public static final String MODID = "dimensional_lock";
    public static final Logger LOGGER = LogUtils.getLogger();

    @EventBusSubscriber(modid = MODID)
    public static class ServerEvents {
        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            DimensionalLockCommand.register(event.getDispatcher());
        }

        // Log every dimension the server loaded (vanilla + modded) so admins know what can be locked
        @SubscribeEvent
        public static void onServerStarted(ServerStartedEvent event) {
            var server = event.getServer();
            LockedDimensions locked = LockedDimensions.get(server);
            String dims = server.levelKeys().stream()
                    .map(ResourceKey::location)
                    .map(id -> locked.isLocked(id) ? id + " (locked)" : id.toString())
                    .sorted()
                    .collect(Collectors.joining(", "));
            LOGGER.info("Found {} dimensions: {}", server.levelKeys().size(), dims);
        }
    }

    public static boolean canBeLocked(ResourceKey<Level> dimension) {
        // The overworld is the fallback players get sent to, so it can never be locked
        return dimension != Level.OVERWORLD;
    }
}
