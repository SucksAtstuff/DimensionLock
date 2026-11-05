package net.succ.dimensionallock;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Comparator;
import java.util.List;

/**
 * /dimlock list
 * /dimlock lock <dimension>
 * /dimlock unlock <dimension>
 */
public class DimensionalLockCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("dimlock")
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("list").executes(DimensionalLockCommand::list))
                .then(Commands.literal("lock")
                        .then(Commands.argument("dimension", DimensionArgument.dimension())
                                .executes(DimensionalLockCommand::lock)))
                .then(Commands.literal("unlock")
                        .then(Commands.argument("dimension", DimensionArgument.dimension())
                                .executes(DimensionalLockCommand::unlock))));
    }

    private static int list(CommandContext<CommandSourceStack> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        LockedDimensions locked = LockedDimensions.get(server);

        ctx.getSource().sendSuccess(() -> Component.literal("Dimensions:").withStyle(ChatFormatting.GOLD), false);
        server.levelKeys().stream()
                .sorted(Comparator.comparing(ResourceKey::location))
                .forEach(key -> {
                    boolean isLocked = locked.isLocked(key.location());
                    Component line = Component.literal(" - " + key.location() + " ")
                            .append(isLocked
                                    ? Component.literal("[LOCKED]").withStyle(ChatFormatting.RED)
                                    : Component.literal("[open]").withStyle(ChatFormatting.GREEN));
                    ctx.getSource().sendSuccess(() -> line, false);
                });
        return server.levelKeys().size();
    }

    private static int lock(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerLevel level = DimensionArgument.getDimension(ctx, "dimension");
        ResourceLocation id = level.dimension().location();

        if (!DimensionalLock.canBeLocked(level.dimension())) {
            ctx.getSource().sendFailure(Component.literal("The overworld can't be locked."));
            return 0;
        }
        if (!LockedDimensions.get(ctx.getSource().getServer()).lock(id)) {
            ctx.getSource().sendFailure(Component.literal(id + " is already locked."));
            return 0;
        }

        // Kick out anyone currently inside (copy the list, teleporting modifies it)
        List<ServerPlayer> inside = List.copyOf(level.players());
        inside.forEach(DimensionEvents::sendToOverworld);
        ctx.getSource().sendSuccess(() -> Component.literal("Locked " + id
                + (inside.isEmpty() ? "." : " and moved " + inside.size() + " player(s) out.")), true);
        return 1;
    }

    private static int unlock(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ResourceLocation id = DimensionArgument.getDimension(ctx, "dimension").dimension().location();
        if (!LockedDimensions.get(ctx.getSource().getServer()).unlock(id)) {
            ctx.getSource().sendFailure(Component.literal(id + " isn't locked."));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.literal("Unlocked " + id + "."), true);
        return 1;
    }
}
