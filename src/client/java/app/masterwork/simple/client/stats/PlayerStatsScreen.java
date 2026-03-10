package app.masterwork.simple.client.stats;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import app.masterwork.simple.stats.ProfessionRegistry;
import app.masterwork.simple.stats.progression.ProfessionProgress;

public final class PlayerStatsScreen extends Screen {
    private static final int PANEL_MARGIN = 8;
    private static final int PANEL_PADDING = 12;
    private static final int HEADER_HEIGHT = 46;
    private static final int FOOTER_HEIGHT = 30;
    private static final int TILE_GAP = 8;
    private static final int TILE_HEIGHT = 54;
    private static final int MIN_TILE_WIDTH = 132;
    private static final int MAX_TILE_COLUMNS = 5;
    private static final int EMPTY_STATE_HEIGHT = 92;
    private static final int SCROLLBAR_WIDTH = 4;
    private static final int SCROLLBAR_GAP = 6;
    private static final double SCROLL_STEP = 18.0D;

    private static final int BACKDROP_TOP = 0xD0111720;
    private static final int BACKDROP_BOTTOM = 0xE018202B;
    private static final int PANEL_COLOR = 0xE6121820;
    private static final int PANEL_OUTLINE = 0xAA2C3844;
    private static final int PANEL_DIVIDER = 0x663A4753;
    private static final int HEADER_COLOR = 0x66172029;
    private static final int FOOTER_COLOR = 0x5C121920;
    private static final int CARD_COLOR = 0xD1182028;
    private static final int CARD_HOVER = 0xE3202A34;
    private static final int CARD_OUTLINE = 0x7A32414D;
    private static final int MUTED_TEXT = 0xFF9BA8B5;
    private static final int PRIMARY_TEXT = 0xFFF3F0EA;
    private static final int SCROLL_TRACK = 0x44212A33;
    private static final int SCROLL_THUMB = 0x88A6B7C4;

    private double scrollOffset;
    private double maxScroll;

    public PlayerStatsScreen() {
        super(Component.translatable("screen.simple.stats.title"));
    }

    @Override
    protected void init() {
        Layout layout = layout();
        int buttonY = layout.panelBottom() - FOOTER_HEIGHT + 5;

        this.addRenderableWidget(
                Button.builder(Component.translatable("screen.simple.stats.refresh"), button -> ClientPlayerStats.requestRefresh())
                        .bounds(layout.panelRight() - 90, layout.panelY() + 12, 74, 20)
                        .tooltip(Tooltip.create(Component.translatable("screen.simple.stats.refresh.tooltip")))
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(Component.translatable("gui.done"), button -> onClose())
                        .bounds(layout.panelRight() - 90, buttonY, 74, 20)
                        .build()
        );

        ClientPlayerStats.requestRefresh();
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBlurredBackground(graphics);
        graphics.fillGradient(0, 0, this.width, this.height, BACKDROP_TOP, BACKDROP_BOTTOM);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Layout layout = layout();
        List<ProfessionEntry> professions = orderedProfessions();

        GridMetrics metrics = gridMetrics(layout, professions.size(), false);
        this.maxScroll = Math.max(0.0D, metrics.contentHeight() - layout.bodyHeight());

        if (this.maxScroll > 0.0D) {
            metrics = gridMetrics(layout, professions.size(), true);
            this.maxScroll = Math.max(0.0D, metrics.contentHeight() - layout.bodyHeight());
        }

        this.scrollOffset = Mth.clamp(this.scrollOffset, 0.0D, this.maxScroll);

        graphics.fill(layout.panelX(), layout.panelY(), layout.panelRight(), layout.panelBottom(), PANEL_COLOR);
        graphics.renderOutline(layout.panelX(), layout.panelY(), layout.panelWidth(), layout.panelHeight(), PANEL_OUTLINE);
        graphics.fill(layout.panelX() + 1, layout.panelY() + 1, layout.panelRight() - 1, layout.panelY() + HEADER_HEIGHT, HEADER_COLOR);
        graphics.fill(layout.panelX() + 1, layout.panelBottom() - FOOTER_HEIGHT, layout.panelRight() - 1, layout.panelBottom() - 1, FOOTER_COLOR);
        graphics.fill(layout.panelX() + 1, layout.bodyY() - 1, layout.panelRight() - 1, layout.bodyY(), PANEL_DIVIDER);
        graphics.fill(layout.panelX() + 1, layout.bodyBottom(), layout.panelRight() - 1, layout.bodyBottom() + 1, PANEL_DIVIDER);

        renderHeader(graphics, layout);
        renderFooter(graphics, layout);

        graphics.enableScissor(layout.bodyX(), layout.bodyY(), layout.bodyX() + metrics.contentWidth(), layout.bodyBottom());
        ProfessionTile hoveredTile = renderBody(graphics, layout, metrics, professions, mouseX, mouseY, (int) Math.round(this.scrollOffset));
        graphics.disableScissor();

        if (this.maxScroll > 0.0D) {
            renderScrollbar(graphics, layout, metrics.contentHeight());
        }

        if (hoveredTile != null) {
            ProfessionEntry entry = hoveredTile.entry();
            graphics.setComponentTooltipForNextFrame(
                    this.font,
                    ProfessionDisplayRegistry.tooltip(entry.id(), professionLabel(entry.id()), entry.progress()),
                    mouseX,
                    mouseY
            );
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.maxScroll <= 0.0D) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        Layout layout = layout();

        if (!layout.containsBody((int) mouseX, (int) mouseY)) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        this.scrollOffset = Mth.clamp(this.scrollOffset - (verticalAmount * SCROLL_STEP), 0.0D, this.maxScroll);
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderHeader(GuiGraphics graphics, Layout layout) {
        int titleY = layout.panelY() + 10;
        int subtitleY = titleY + 13;
        graphics.drawString(this.font, this.title, layout.contentX(), titleY, PRIMARY_TEXT, false);
        graphics.drawString(this.font, Component.translatable("screen.simple.stats.subtitle"), layout.contentX(), subtitleY, MUTED_TEXT, false);

        Component status = ClientPlayerStats.isLoading()
                ? Component.translatable("screen.simple.stats.status.refreshing")
                : Component.translatable("screen.simple.stats.status.live");
        int statusWidth = this.font.width(status);
        int chipX = layout.panelRight() - 100 - statusWidth;
        int chipY = layout.panelY() + 15;
        int chipWidth = statusWidth + 12;
        int chipColor = ClientPlayerStats.isLoading() ? 0x334B7184 : 0x33326B62;
        int chipOutline = ClientPlayerStats.isLoading() ? 0x995C8CA6 : 0x9961B4A5;
        graphics.fill(chipX, chipY - 3, chipX + chipWidth, chipY + 10, chipColor);
        graphics.renderOutline(chipX, chipY - 3, chipWidth, 13, chipOutline);
        graphics.drawString(this.font, status, chipX + 6, chipY, 0xFFD9E8E5, false);
    }

    private void renderFooter(GuiGraphics graphics, Layout layout) {
        graphics.drawString(
                this.font,
                Component.translatable("screen.simple.stats.footer_hint"),
                layout.contentX(),
                layout.panelBottom() - 17,
                MUTED_TEXT,
                false
        );
    }

    private ProfessionTile renderBody(
            GuiGraphics graphics,
            Layout layout,
            GridMetrics metrics,
            List<ProfessionEntry> professions,
            int mouseX,
            int mouseY,
            int scroll
    ) {
        int originX = layout.bodyX();
        int originY = layout.bodyY() - scroll;

        if (professions.isEmpty()) {
            renderEmptyState(graphics, originX, originY + 4, metrics.contentWidth());
            return null;
        }

        ProfessionTile hovered = null;

        for (int index = 0; index < professions.size(); index++) {
            ProfessionTile tile = professionTile(originX, originY, metrics, index, professions.get(index));
            boolean isHovered = tile.contains(mouseX, mouseY);
            renderProfessionTile(graphics, tile, isHovered);

            if (isHovered) {
                hovered = tile;
            }
        }

        return hovered;
    }

    private void renderEmptyState(GuiGraphics graphics, int x, int y, int width) {
        graphics.fill(x, y, x + width, y + EMPTY_STATE_HEIGHT, CARD_COLOR);
        graphics.renderOutline(x, y, width, EMPTY_STATE_HEIGHT, CARD_OUTLINE);

        Component message = ClientPlayerStats.isLoading()
                ? Component.translatable("screen.simple.stats.loading")
                : Component.translatable("screen.simple.stats.empty");
        graphics.drawCenteredString(this.font, message, x + (width / 2), y + 18, PRIMARY_TEXT);

        List<FormattedCharSequence> lines = this.font.split(Component.translatable("screen.simple.stats.empty_hint"), width - 32);
        int lineY = y + 38;

        for (FormattedCharSequence line : lines) {
            graphics.drawString(this.font, line, x + 16, lineY, MUTED_TEXT, false);
            lineY += 10;
        }
    }

    private void renderProfessionTile(GuiGraphics graphics, ProfessionTile tile, boolean hovered) {
        ProfessionEntry entry = tile.entry();
        int accent = ProfessionDisplayRegistry.accent(entry.id());
        int fillColor = hovered ? CARD_HOVER : CARD_COLOR;
        int outlineColor = hovered ? accent : CARD_OUTLINE;

        graphics.fill(tile.x(), tile.y(), tile.right(), tile.bottom(), fillColor);
        graphics.fill(tile.x(), tile.y(), tile.right(), tile.y() + 3, accent);
        graphics.renderOutline(tile.x(), tile.y(), tile.width(), tile.height(), outlineColor);

        Component label = professionLabel(entry.id());
        graphics.drawString(
                this.font,
                Component.literal(trimLabel(label.getString(), tile.width() - 16)),
                tile.x() + 8,
                tile.y() + 8,
                PRIMARY_TEXT,
                false
        );

        String value = Integer.toString(entry.progress().level());
        graphics.drawCenteredString(this.font, value, tile.x() + (tile.width() / 2), tile.y() + 24, accent);
    }

    private GridMetrics gridMetrics(Layout layout, int itemCount, boolean showScrollbar) {
        int contentWidth = layout.bodyContentWidth(showScrollbar);
        int columns = Math.max(1, Math.min(MAX_TILE_COLUMNS, (contentWidth + TILE_GAP) / (MIN_TILE_WIDTH + TILE_GAP)));
        int tileWidth = Math.max(96, (contentWidth - ((columns - 1) * TILE_GAP)) / columns);
        int rows = Math.max(1, (Math.max(itemCount, 1) + columns - 1) / columns);
        int contentHeight = itemCount == 0
                ? EMPTY_STATE_HEIGHT
                : (rows * TILE_HEIGHT) + (Math.max(0, rows - 1) * TILE_GAP);
        return new GridMetrics(contentWidth, columns, tileWidth, contentHeight);
    }

    private ProfessionTile professionTile(int originX, int originY, GridMetrics metrics, int index, ProfessionEntry entry) {
        int column = index % metrics.columns();
        int row = index / metrics.columns();
        int x = originX + column * (metrics.tileWidth() + TILE_GAP);
        int y = originY + row * (TILE_HEIGHT + TILE_GAP);
        return new ProfessionTile(x, y, metrics.tileWidth(), TILE_HEIGHT, entry);
    }

    private void renderScrollbar(GuiGraphics graphics, Layout layout, int contentHeight) {
        int trackX = layout.bodyRight() - SCROLLBAR_WIDTH;
        int trackY = layout.bodyY() + 2;
        int trackHeight = layout.bodyHeight() - 4;
        graphics.fill(trackX, trackY, trackX + SCROLLBAR_WIDTH, trackY + trackHeight, SCROLL_TRACK);

        int thumbHeight = Math.max(18, Math.round(trackHeight * (layout.bodyHeight() / (float) contentHeight)));
        int travel = Math.max(1, trackHeight - thumbHeight);
        int thumbY = trackY + (int) Math.round((this.scrollOffset / this.maxScroll) * travel);
        graphics.fill(trackX - 1, thumbY, trackX + SCROLLBAR_WIDTH + 1, thumbY + thumbHeight, SCROLL_THUMB);
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

    private Component professionLabel(Identifier id) {
        String key = "profession." + id.getNamespace() + "." + id.getPath();

        if (I18n.exists(key)) {
            return Component.translatable(key);
        }

        return Component.literal(titleCase(id.getPath()));
    }

    private String trimLabel(String value, int maxWidth) {
        if (this.font.width(value) <= maxWidth) {
            return value;
        }

        String trimmed = this.font.plainSubstrByWidth(value, Math.max(0, maxWidth - this.font.width("...")));
        return trimmed + "...";
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

    private Layout layout() {
        int availableWidth = Math.max(280, this.width - (PANEL_MARGIN * 2));
        int availableHeight = Math.max(180, this.height - (PANEL_MARGIN * 2));
        int panelWidth = Math.min(960, availableWidth);
        int panelHeight = availableHeight;
        int panelX = (this.width - panelWidth) / 2;
        int panelY = (this.height - panelHeight) / 2;
        return new Layout(panelX, panelY, panelWidth, panelHeight);
    }

    private record ProfessionEntry(Identifier id, ProfessionProgress progress) {
    }

    private record GridMetrics(int contentWidth, int columns, int tileWidth, int contentHeight) {
    }

    private record ProfessionTile(int x, int y, int width, int height, ProfessionEntry entry) {
        private int right() {
            return x + width;
        }

        private int bottom() {
            return y + height;
        }

        private boolean contains(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= right() && mouseY >= y && mouseY <= bottom();
        }
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

        private int bodyX() {
            return contentX();
        }

        private int bodyRight() {
            return contentRight();
        }

        private int bodyY() {
            return panelY + HEADER_HEIGHT;
        }

        private int bodyBottom() {
            return panelBottom() - FOOTER_HEIGHT;
        }

        private int bodyHeight() {
            return bodyBottom() - bodyY();
        }

        private int bodyContentWidth(boolean showScrollbar) {
            return Math.max(96, contentWidth() - (showScrollbar ? SCROLLBAR_WIDTH + SCROLLBAR_GAP : 0));
        }

        private boolean containsBody(int mouseX, int mouseY) {
            return mouseX >= bodyX() && mouseX <= bodyRight() && mouseY >= bodyY() && mouseY <= bodyBottom();
        }
    }
}
