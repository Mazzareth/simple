package app.masterwork.simple.skills;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.server.level.ServerPlayer;

public final class SkillCommands {
    private static final SimpleCommandExceptionType INVALID_SKILL_ID = new SimpleCommandExceptionType(Component.translatable("commands.simple.skills.error.invalid"));
    private static final SimpleCommandExceptionType UNKNOWN_SKILL = new SimpleCommandExceptionType(Component.translatable("commands.simple.skills.error.unknown"));
    private static boolean registered;

    private SkillCommands() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    Commands.literal("simple")
                            .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                            .then(Commands.literal("skills")
                                    .then(Commands.literal("bxp")
                                            .then(Commands.literal("add")
                                                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                            .executes(context -> addBxp(
                                                                    context,
                                                                    IntegerArgumentType.getInteger(context, "amount")
                                                            )))))
                                    .then(Commands.literal("unlock")
                                            .then(Commands.argument("skill", StringArgumentType.string())
                                                    .executes(SkillCommands::unlockSkill)))
                                    .then(Commands.literal("reset")
                                            .executes(context -> reset(context.getSource()))))
            );
        });
    }

    private static int addBxp(CommandContext<CommandSourceStack> context, int amount) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        SkillProfile profile = PlayerSkills.awardBxp(player, amount);
        context.getSource().sendSuccess(
                () -> Component.translatable(
                        "commands.simple.skills.bxp_added",
                        amount,
                        profile.bXp(),
                        SkillProgression.ep(profile)
                ),
                false
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int unlockSkill(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Identifier skillId;

        try {
            skillId = Identifier.parse(StringArgumentType.getString(context, "skill"));
        } catch (IllegalArgumentException exception) {
            throw INVALID_SKILL_ID.create();
        }

        if (!SkillRegistry.contains(skillId)) {
            throw UNKNOWN_SKILL.create();
        }

        SkillProfile profile = PlayerSkills.unlock(player, skillId);
        SkillDefinition definition = SkillRegistry.byId(skillId).orElseThrow();
        context.getSource().sendSuccess(
                () -> Component.translatable(
                        "commands.simple.skills.unlocked",
                        Component.translatable(definition.nameKey()),
                        SkillProgression.ep(profile)
                ),
                false
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int reset(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        PlayerSkills.reset(player);
        source.sendSuccess(() -> Component.translatable("commands.simple.skills.reset"), false);
        return Command.SINGLE_SUCCESS;
    }
}
