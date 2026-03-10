package app.masterwork.simple.client.stats;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import app.masterwork.simple.stats.ProfessionRegistry;
import app.masterwork.simple.stats.agility.AgilityData;
import app.masterwork.simple.stats.progression.ProfessionProgress;
import app.masterwork.simple.stats.progression.ProfessionProgression;
import app.masterwork.simple.stats.strength.StrengthData;

public final class ProfessionDisplayRegistry {
    private static final int DEFAULT_ACCENT = 0xFF7AA2C2;
    private static final Map<Identifier, ProfessionDisplaySpec> SPECS = new LinkedHashMap<>();

    static {
        register(ProfessionRegistry.AGILITY.id(), 0xFF4FB0A1, ProfessionDisplayRegistry::agilityDetails);
        register(ProfessionRegistry.STRENGTH.id(), 0xFFCC9A52, ProfessionDisplayRegistry::strengthDetails);
        register(ProfessionRegistry.QUARRYING.id(), 0xFF8A98A8, progress -> List.of());
        register(ProfessionRegistry.MINING.id(), 0xFFBE6255, progress -> List.of());
        register(ProfessionRegistry.WOODCUTTING.id(), 0xFF6F9A63, progress -> List.of());
    }

    private ProfessionDisplayRegistry() {
    }

    public static void register(Identifier professionId, int accent, Function<ProfessionProgress, List<Component>> detailsFactory) {
        SPECS.put(
                Objects.requireNonNull(professionId, "professionId"),
                new ProfessionDisplaySpec(accent, Objects.requireNonNull(detailsFactory, "detailsFactory"))
        );
    }

    public static int accent(Identifier professionId) {
        ProfessionDisplaySpec spec = SPECS.get(professionId);
        return spec != null ? spec.accent() : DEFAULT_ACCENT;
    }

    public static List<Component> tooltip(Identifier professionId, Component label, ProfessionProgress progress) {
        ArrayList<Component> lines = new ArrayList<>();
        lines.add(label.copy().withStyle(ChatFormatting.WHITE));
        lines.add(Component.translatable("screen.simple.stats.level", progress.level()).withStyle(ChatFormatting.GRAY));

        if (progress.level() >= ProfessionProgression.MAX_LEVEL) {
            lines.add(Component.translatable("screen.simple.stats.max_level").withStyle(ChatFormatting.DARK_GRAY));
        } else {
            lines.add(
                    Component.translatable("screen.simple.stats.xp", progress.xp(), ProfessionProgression.xpToNextLevel(progress.level()))
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }

        String descriptionKey = "screen.simple.stats.tooltip." + professionId.getNamespace() + "." + professionId.getPath();

        if (I18n.exists(descriptionKey)) {
            lines.add(Component.translatable(descriptionKey).withStyle(ChatFormatting.GRAY));
        }

        ProfessionDisplaySpec spec = SPECS.get(professionId);
        List<Component> details = spec != null ? spec.detailsFactory().apply(progress) : List.of();

        if (details.isEmpty()) {
            lines.add(Component.translatable("screen.simple.stats.tooltip.no_details").withStyle(ChatFormatting.DARK_GRAY));
            return List.copyOf(lines);
        }

        lines.add(Component.translatable("screen.simple.stats.tooltip.details").withStyle(ChatFormatting.GOLD));
        lines.addAll(details);
        return List.copyOf(lines);
    }

    private static List<Component> agilityDetails(ProfessionProgress progress) {
        AgilityData data = AgilityData.fromProgress(progress);
        int accent = accent(ProfessionRegistry.AGILITY.id());
        return List.of(
                tinted(Component.translatable("screen.simple.stats.bonus.movement_speed", formatPercent(data.movementSpeedBonus())), accent),
                tinted(Component.translatable("screen.simple.stats.bonus.step_height", formatDecimal(data.stepHeightBonus())), accent),
                tinted(Component.translatable("screen.simple.stats.bonus.safe_fall", formatDecimal(data.safeFallBonus())), accent)
        );
    }

    private static List<Component> strengthDetails(ProfessionProgress progress) {
        StrengthData data = StrengthData.fromProgress(progress);
        int accent = accent(ProfessionRegistry.STRENGTH.id());
        return List.of(
                tinted(Component.translatable("screen.simple.stats.bonus.attack_damage", formatDecimal(data.attackDamageBonus())), accent),
                tinted(Component.translatable("screen.simple.stats.bonus.attack_knockback", formatDecimal(data.attackKnockbackBonus())), accent),
                tinted(Component.translatable("screen.simple.stats.bonus.mining_efficiency", formatPercent(data.miningEfficiencyBonus())), accent)
        );
    }

    private static Component tinted(Component component, int color) {
        return component.copy().withStyle(style -> style.withColor(color));
    }

    private static String formatPercent(double value) {
        return String.format(java.util.Locale.ROOT, "%.1f%%", value * 100.0D);
    }

    private static String formatDecimal(double value) {
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    private record ProfessionDisplaySpec(int accent, Function<ProfessionProgress, List<Component>> detailsFactory) {
    }
}
