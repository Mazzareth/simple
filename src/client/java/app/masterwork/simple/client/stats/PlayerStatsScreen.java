package app.masterwork.simple.client.stats;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import app.masterwork.simple.stats.ProfessionRegistry;
import app.masterwork.simple.stats.agility.AgilityData;
import app.masterwork.simple.stats.progression.ProfessionProgress;
import app.masterwork.simple.stats.progression.ProfessionProgression;
import app.masterwork.simple.stats.strength.StrengthData;

public final class PlayerStatsScreen extends Screen {
    private static final Set<Identifier> CORE_PROFESSIONS = Set.of(
            ProfessionRegistry.AGILITY.id(),
            ProfessionRegistry.STRENGTH.id()
    );
    private static final int PANEL_MARGIN = 28;
    private static final int PANEL_PADDING = 16;
    private static final int CARD_GAP = 12;
    private static final int DETAIL_CARD_HEIGHT = 108;
    private static final int COMPACT_ROW_HEIGHT = 36;
    private static final int PANEL_COLOR = 0xE5151C22;
    private static final int PANEL_OUTLINE = 0x66313A43;
    private static final int CARD_COLOR = 0xD21B232A;
    private static final int CARD_OUTLINE = 0x663A4651;
    private static final int MUTED_TEXT = 0xFF97A3AE;
    private static final int PRIMARY_TEXT = 0xFFF3F0EA;

    public PlayerStatsScreen() {
        super(Component.translatable("screen.simple.stats.title"));
    }

    @Override
    protected void init() {
        Layout layout = layout();
        int buttonY = layout.panelY() + layout.panelHeight() - 28;

        this.addRenderableWidget(
                Button.builder(Component.translatable("screen.simple.stats.refresh"), button -> ClientPlayerStats.requestRefresh())
                        .bounds(layout.panelX() + layout.panelWidth() - 92, layout.panelY() + 14, 76, 20)
                        .tooltip(Tooltip.create(Component.translatable("screen.simple.stats.refresh.tooltip")))
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(Component.translatable("gui.done"), button -> onClose())
                        .bounds(layout.panelX() + (layout.panelWidth() - 90) / 2, buttonY, 90, 20)
                        .build()
        );

        ClientPlayerStats.requestRefresh();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        this.renderBlurredBackground(graphics);
        graphics.fillGradient(0, 0, this.width, this.height, 0xD60D1217, 0xE611171D);

        Layout layout = layout();
        graphics.fill(layout.panelX(), layout.panelY(), layout.panelRight(), layout.panelBottom(), PANEL_COLOR);
        graphics.renderOutline(layout.panelX(), layout.panelY(), layout.panelWidth(), layout.panelHeight(), PANEL_OUTLINE);

        renderHeader(graphics, layout);

        List<ProfessionEntry> professions = orderedProfessions();
        ProfessionEntry agility = find(professions, ProfessionRegistry.AGILITY.id());
        ProfessionEntry strength = find(professions, ProfessionRegistry.STRENGTH.id());
        List<ProfessionEntry> others = professions.stream()
                .filter(entry -> !CORE_PROFESSIONS.contains(entry.id()))
                .toList();

        int contentY = layout.panelY() + 52;

        if (professions.isEmpty()) {
            renderEmptyState(graphics, layout, contentY);
        } else {
            int cardWidth = (layout.contentWidth() - CARD_GAP) / 2;

            if (agility != null) {
                renderDetailedCard(graphics, layout.contentX(), contentY, cardWidth, DETAIL_CARD_HEIGHT, agility);
            }

            if (strength != null) {
                renderDetailedCard(graphics, layout.contentX() + cardWidth + CARD_GAP, contentY, cardWidth, DETAIL_CARD_HEIGHT, strength);
            }

            int rowY = contentY + DETAIL_CARD_HEIGHT + 26;

            if (!others.isEmpty()) {
                graphics.drawString(this.font, Component.translatable("screen.simple.stats.section.professions"), layout.contentX(), rowY - 14, MUTED_TEXT, false);

                for (ProfessionEntry entry : others) {
                    renderCompactRow(graphics, layout.contentX(), rowY, layout.contentWidth(), COMPACT_ROW_HEIGHT, entry);
                    rowY += COMPACT_ROW_HEIGHT + 8;
                }
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderHeader(GuiGraphics graphics, Layout layout) {
        graphics.drawString(this.font, this.title, layout.contentX(), layout.panelY() + 16, PRIMARY_TEXT, false);
        graphics.drawString(
                this.font,
                Component.translatable("screen.simple.stats.subtitle"),
                layout.contentX(),
                layout.panelY() + 30,
                MUTED_TEXT,
                false
        );

        Component status = ClientPlayerStats.isLoading()
                ? Component.translatable("screen.simple.stats.status.refreshing")
                : Component.translatable("screen.simple.stats.status.live");
        int statusWidth = this.font.width(status);
        graphics.drawString(this.font, status, layout.panelRight() - statusWidth - 108, layout.panelY() + 18, 0xFFAFC4C6, false);
    }

    private void renderEmptyState(GuiGraphics graphics, Layout layout, int contentY) {
        int boxHeight = 112;
        int boxY = contentY + 8;
        graphics.fill(layout.contentX(), boxY, layout.contentRight(), boxY + boxHeight, CARD_COLOR);
        graphics.renderOutline(layout.contentX(), boxY, layout.contentWidth(), boxHeight, CARD_OUTLINE);

        Component message = ClientPlayerStats.isLoading()
                ? Component.translatable("screen.simple.stats.loading")
                : Component.translatable("screen.simple.stats.empty");
        int messageWidth = this.font.width(message);
        graphics.drawString(this.font, message, layout.panelX() + (layout.panelWidth() - messageWidth) / 2, boxY + 30, PRIMARY_TEXT, false);
        graphics.drawString(
                this.font,
                Component.translatable("screen.simple.stats.empty_hint"),
                layout.panelX() + 28,
                boxY + 52,
                MUTED_TEXT,
                false
        );
    }

    private void renderDetailedCard(GuiGraphics graphics, int x, int y, int width, int height, ProfessionEntry entry) {
        int accent = accent(entry.id());
        ProfessionProgress progress = entry.progress();

        graphics.fill(x, y, x + width, y + height, CARD_COLOR);
        graphics.fill(x, y, x + width, y + 3, accent);
        graphics.renderOutline(x, y, width, height, CARD_OUTLINE);

        Component label = professionLabel(entry.id());
        graphics.drawString(this.font, label, x + 14, y + 12, PRIMARY_TEXT, false);
        graphics.drawString(this.font, Component.translatable("screen.simple.stats.level", progress.level()), x + 14, y + 28, accent, false);

        int xpToNextLevel = ProfessionProgression.xpToNextLevel(progress.level());
        Component xpText = progress.level() >= ProfessionProgression.MAX_LEVEL
                ? Component.translatable("screen.simple.stats.max_level")
                : Component.translatable("screen.simple.stats.xp", progress.xp(), xpToNextLevel);
        graphics.drawString(this.font, xpText, x + 14, y + 44, MUTED_TEXT, false);

        renderProgressBar(graphics, x + 14, y + 60, width - 28, 8, progress, accent);

        int lineY = y + 76;

        if (entry.id().equals(ProfessionRegistry.AGILITY.id())) {
            AgilityData data = AgilityData.fromProgress(progress);
            drawBonusLine(graphics, x + 14, lineY, "screen.simple.stats.bonus.movement_speed", formatPercent(data.movementSpeedBonus()));
            drawBonusLine(graphics, x + 14, lineY + 12, "screen.simple.stats.bonus.step_height", formatDecimal(data.stepHeightBonus()));
            drawBonusLine(graphics, x + 14, lineY + 24, "screen.simple.stats.bonus.safe_fall", formatDecimal(data.safeFallBonus()));
        } else if (entry.id().equals(ProfessionRegistry.STRENGTH.id())) {
            StrengthData data = StrengthData.fromProgress(progress);
            drawBonusLine(graphics, x + 14, lineY, "screen.simple.stats.bonus.attack_damage", formatDecimal(data.attackDamageBonus()));
            drawBonusLine(graphics, x + 14, lineY + 12, "screen.simple.stats.bonus.attack_knockback", formatDecimal(data.attackKnockbackBonus()));
            drawBonusLine(graphics, x + 14, lineY + 24, "screen.simple.stats.bonus.mining_efficiency", formatPercent(data.miningEfficiencyBonus()));
        }
    }

    private void renderCompactRow(GuiGraphics graphics, int x, int y, int width, int height, ProfessionEntry entry) {
        int accent = accent(entry.id());
        ProfessionProgress progress = entry.progress();
        int rightTextX = x + width - 12;

        graphics.fill(x, y, x + width, y + height, 0xC4171F25);
        graphics.renderOutline(x, y, width, height, CARD_OUTLINE);
        graphics.fill(x, y, x + 4, y + height, accent);

        Component label = professionLabel(entry.id());
        graphics.drawString(this.font, label, x + 12, y + 9, PRIMARY_TEXT, false);

        Component levelText = Component.translatable("screen.simple.stats.level_short", progress.level());
        graphics.drawString(this.font, levelText, rightTextX - this.font.width(levelText), y + 9, accent, false);

        int xpToNextLevel = ProfessionProgression.xpToNextLevel(progress.level());
        Component xpText = progress.level() >= ProfessionProgression.MAX_LEVEL
                ? Component.translatable("screen.simple.stats.max_level")
                : Component.translatable("screen.simple.stats.xp_short", progress.xp(), xpToNextLevel);
        graphics.drawString(this.font, xpText, x + 12, y + 21, MUTED_TEXT, false);

        renderProgressBar(graphics, x + width - 124, y + 14, 110, 7, progress, accent);
    }

    private void renderProgressBar(GuiGraphics graphics, int x, int y, int width, int height, ProfessionProgress progress, int accent) {
        graphics.fill(x, y, x + width, y + height, 0x8812181D);
        graphics.renderOutline(x, y, width, height, 0x662C3840);

        if (progress.level() >= ProfessionProgression.MAX_LEVEL) {
            graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, accent);
            return;
        }

        int xpToNextLevel = Math.max(1, ProfessionProgression.xpToNextLevel(progress.level()));
        int fillWidth = Math.max(0, Math.min(width - 2, Math.round((width - 2) * (progress.xp() / (float) xpToNextLevel))));

        if (fillWidth > 0) {
            graphics.fillGradient(x + 1, y + 1, x + 1 + fillWidth, y + height - 1, brighten(accent), accent);
        }
    }

    private void drawBonusLine(GuiGraphics graphics, int x, int y, String translationKey, String value) {
        graphics.drawString(this.font, Component.translatable(translationKey, value), x, y, MUTED_TEXT, false);
    }

    private List<ProfessionEntry> orderedProfessions() {
        Map<Identifier, ProfessionProgress> snapshot = ClientPlayerStats.snapshot();

        if (snapshot.isEmpty()) {
            return List.of();
        }

        List<ProfessionEntry> ordered = new ArrayList<>();

        ProfessionRegistry.all().forEach(profession ->
                ordered.add(new ProfessionEntry(profession.id(), snapshot.getOrDefault(profession.id(), ProfessionProgress.ZERO)))
        );

        return ordered;
    }

    private ProfessionEntry find(List<ProfessionEntry> professions, Identifier id) {
        return professions.stream().filter(entry -> entry.id().equals(id)).findFirst().orElse(null);
    }

    private Component professionLabel(Identifier id) {
        String key = "profession." + id.getNamespace() + "." + id.getPath();

        if (I18n.exists(key)) {
            return Component.translatable(key);
        }

        return Component.literal(titleCase(id.getPath()));
    }

    private String titleCase(String path) {
        String[] segments = path.split("[/_-]");
        StringBuilder builder = new StringBuilder();

        for (String segment : segments) {
            if (segment.isBlank()) {
                continue;
            }

            if (!builder.isEmpty()) {
                builder.append(' ');
            }

            builder.append(Character.toUpperCase(segment.charAt(0)));

            if (segment.length() > 1) {
                builder.append(segment.substring(1));
            }
        }

        return builder.isEmpty() ? path : builder.toString();
    }

    private int accent(Identifier id) {
        if (id.equals(ProfessionRegistry.AGILITY.id())) {
            return 0xFF4FB0A1;
        }

        if (id.equals(ProfessionRegistry.STRENGTH.id())) {
            return 0xFFCC9A52;
        }

        if (id.equals(ProfessionRegistry.QUARRYING.id())) {
            return 0xFF8A98A8;
        }

        if (id.equals(ProfessionRegistry.MINING.id())) {
            return 0xFFBE6255;
        }

        if (id.equals(ProfessionRegistry.WOODCUTTING.id())) {
            return 0xFF6F9A63;
        }

        return 0xFF7AA2C2;
    }

    private int brighten(int color) {
        int alpha = color & 0xFF000000;
        int red = Math.min(255, ((color >> 16) & 0xFF) + 28);
        int green = Math.min(255, ((color >> 8) & 0xFF) + 28);
        int blue = Math.min(255, (color & 0xFF) + 28);
        return alpha | (red << 16) | (green << 8) | blue;
    }

    private String formatPercent(double value) {
        return String.format(Locale.ROOT, "%.1f%%", value * 100.0D);
    }

    private String formatDecimal(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private Layout layout() {
        int otherCount = Math.max(0, ProfessionRegistry.all().size() - CORE_PROFESSIONS.size());
        int panelWidth = Math.min(620, this.width - (PANEL_MARGIN * 2));
        int panelHeight = Math.min(
                this.height - (PANEL_MARGIN * 2),
                220 + DETAIL_CARD_HEIGHT + (otherCount * (COMPACT_ROW_HEIGHT + 8))
        );
        int panelX = (this.width - panelWidth) / 2;
        int panelY = (this.height - panelHeight) / 2;
        return new Layout(panelX, panelY, panelWidth, panelHeight);
    }

    private record ProfessionEntry(Identifier id, ProfessionProgress progress) {
    }

    private record Layout(int panelX, int panelY, int panelWidth, int panelHeight) {
        private int panelRight() {
            return panelX + panelWidth;
        }

        private int panelBottom() {
            return panelY + panelHeight;
        }

        private int contentX() {
            return panelX + PANEL_PADDING;
        }

        private int contentRight() {
            return panelRight() - PANEL_PADDING;
        }

        private int contentWidth() {
            return panelWidth - (PANEL_PADDING * 2);
        }
    }
}
