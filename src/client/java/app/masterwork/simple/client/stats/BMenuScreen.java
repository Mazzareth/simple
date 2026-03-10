package app.masterwork.simple.client.stats;

import java.util.ArrayList;
import java.util.EnumMap;
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

import app.masterwork.simple.skills.SkillDefinition;
import app.masterwork.simple.skills.SkillRegistry;
import app.masterwork.simple.skills.SkillTier;
import app.masterwork.simple.stats.ProfessionRegistry;
import app.masterwork.simple.stats.progression.ProfessionProgress;

public final class BMenuScreen extends Screen {
    private static final int PANEL_MARGIN = 8;
    private static final int PANEL_PADDING = 12;
    private static final int HEADER_HEIGHT = 62;
    private static final int FOOTER_HEIGHT = 30;
    private static final int TILE_GAP = 8;
    private static final int SUMMARY_TILE_HEIGHT = 42;
    private static final int SKILL_TILE_HEIGHT = 62;
    private static final int PROFESSION_TILE_HEIGHT = 54;
    private static final int MIN_SUMMARY_TILE_WIDTH = 108;
    private static final int MIN_SKILL_TILE_WIDTH = 148;
    private static final int MIN_PROFESSION_TILE_WIDTH = 132;
    private static final int MAX_SKILL_COLUMNS = 4;
    private static final int MAX_PROFESSION_COLUMNS = 5;
    private static final int SECTION_GAP = 12;
    private static final int SECTION_HEADER_HEIGHT = 14;
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

    private Tab activeTab = Tab.SKILLS;
    private double skillsScrollOffset;
    private double statsScrollOffset;
    private double skillsMaxScroll;
    private double statsMaxScroll;

    private Button skillsTabButton;
    private Button statsTabButton;

    public BMenuScreen() {
        super(Component.translatable("screen.simple.b_menu.title"));
    }

    @Override
    protected void init() {
        Layout layout = layout();
        int buttonY = layout.panelBottom() - FOOTER_HEIGHT + 5;
        int tabY = layout.panelY() + 36;
        int tabWidth = 68;

        this.skillsTabButton = this.addRenderableWidget(
                Button.builder(Component.translatable("screen.simple.b_menu.tab.skills"), button -> switchTab(Tab.SKILLS))
                        .bounds(layout.contentX(), tabY, tabWidth, 20)
                        .build()
        );

        this.statsTabButton = this.addRenderableWidget(
                Button.builder(Component.translatable("screen.simple.b_menu.tab.stats"), button -> switchTab(Tab.STATS))
                        .bounds(layout.contentX() + tabWidth + 6, tabY, tabWidth, 20)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(Component.translatable("screen.simple.b_menu.refresh"), button -> requestRefresh())
                        .bounds(layout.panelRight() - 90, layout.panelY() + 12, 74, 20)
                        .tooltip(Tooltip.create(Component.translatable("screen.simple.b_menu.refresh.tooltip")))
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(Component.translatable("gui.done"), button -> onClose())
                        .bounds(layout.panelRight() - 90, buttonY, 74, 20)
                        .build()
        );

        refreshTabButtons();
        requestRefresh();
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBlurredBackground(graphics);
        graphics.fillGradient(0, 0, this.width, this.height, BACKDROP_TOP, BACKDROP_BOTTOM);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Layout layout = layout();

        graphics.fill(layout.panelX(), layout.panelY(), layout.panelRight(), layout.panelBottom(), PANEL_COLOR);
        graphics.renderOutline(layout.panelX(), layout.panelY(), layout.panelWidth(), layout.panelHeight(), PANEL_OUTLINE);
        graphics.fill(layout.panelX() + 1, layout.panelY() + 1, layout.panelRight() - 1, layout.panelY() + HEADER_HEIGHT, HEADER_COLOR);
        graphics.fill(layout.panelX() + 1, layout.panelBottom() - FOOTER_HEIGHT, layout.panelRight() - 1, layout.panelBottom() - 1, FOOTER_COLOR);
        graphics.fill(layout.panelX() + 1, layout.bodyY() - 1, layout.panelRight() - 1, layout.bodyY(), PANEL_DIVIDER);
        graphics.fill(layout.panelX() + 1, layout.bodyBottom(), layout.panelRight() - 1, layout.bodyBottom() + 1, PANEL_DIVIDER);

        renderHeader(graphics, layout);
        renderFooter(graphics, layout);

        HoverData hoverData = renderActiveBody(graphics, layout, mouseX, mouseY);

        if (hoverData != null) {
            graphics.setComponentTooltipForNextFrame(this.font, hoverData.lines(), mouseX, mouseY);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!layout().containsBody((int) mouseX, (int) mouseY)) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        if (this.activeTab == Tab.SKILLS) {
            if (this.skillsMaxScroll <= 0.0D) {
                return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
            }

            this.skillsScrollOffset = Mth.clamp(this.skillsScrollOffset - (verticalAmount * SCROLL_STEP), 0.0D, this.skillsMaxScroll);
            return true;
        }

        if (this.statsMaxScroll <= 0.0D) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        this.statsScrollOffset = Mth.clamp(this.statsScrollOffset - (verticalAmount * SCROLL_STEP), 0.0D, this.statsMaxScroll);
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void switchTab(Tab tab) {
        this.activeTab = tab;
        refreshTabButtons();
    }

    private void refreshTabButtons() {
        if (this.skillsTabButton != null) {
            this.skillsTabButton.active = this.activeTab != Tab.SKILLS;
        }

        if (this.statsTabButton != null) {
            this.statsTabButton.active = this.activeTab != Tab.STATS;
        }
    }

    private void requestRefresh() {
        ClientPlayerSkills.requestRefresh();
        ClientPlayerStats.requestRefresh();
    }

    private void renderHeader(GuiGraphics graphics, Layout layout) {
        graphics.drawString(this.font, this.title, layout.contentX(), layout.panelY() + 10, PRIMARY_TEXT, false);
        graphics.drawString(
                this.font,
                Component.translatable(this.activeTab == Tab.SKILLS ? "screen.simple.b_menu.subtitle.skills" : "screen.simple.b_menu.subtitle.stats"),
                layout.contentX(),
                layout.panelY() + 22,
                MUTED_TEXT,
                false
        );

        Component status = currentLoading()
                ? Component.translatable("screen.simple.b_menu.status.refreshing")
                : Component.translatable("screen.simple.b_menu.status.synced");
        int statusWidth = this.font.width(status);
        int chipWidth = statusWidth + 12;
        int chipX = layout.panelRight() - 100 - chipWidth;
        int chipY = layout.panelY() + 17;
        int chipColor = currentLoading() ? 0x334B7184 : 0x33326B62;
        int chipOutline = currentLoading() ? 0x995C8CA6 : 0x9961B4A5;
        graphics.fill(chipX, chipY - 3, chipX + chipWidth, chipY + 10, chipColor);
        graphics.renderOutline(chipX, chipY - 3, chipWidth, 13, chipOutline);
        graphics.drawString(this.font, status, chipX + 6, chipY, 0xFFD9E8E5, false);
    }

    private void renderFooter(GuiGraphics graphics, Layout layout) {
        graphics.drawString(
                this.font,
                Component.translatable(this.activeTab == Tab.SKILLS ? "screen.simple.b_menu.footer.skills" : "screen.simple.b_menu.footer.stats"),
                layout.contentX(),
                layout.panelBottom() - 17,
                MUTED_TEXT,
                false
        );
    }

    private HoverData renderActiveBody(GuiGraphics graphics, Layout layout, int mouseX, int mouseY) {
        if (this.activeTab == Tab.SKILLS) {
            SkillMetrics metrics = skillMetrics(layout, false);
            this.skillsMaxScroll = Math.max(0.0D, metrics.contentHeight() - layout.bodyHeight());

            if (this.skillsMaxScroll > 0.0D) {
                metrics = skillMetrics(layout, true);
                this.skillsMaxScroll = Math.max(0.0D, metrics.contentHeight() - layout.bodyHeight());
            }

            this.skillsScrollOffset = Mth.clamp(this.skillsScrollOffset, 0.0D, this.skillsMaxScroll);
            graphics.enableScissor(layout.bodyX(), layout.bodyY(), layout.bodyX() + metrics.contentWidth(), layout.bodyBottom());
            HoverData hoverData = renderSkillsBody(graphics, layout, metrics, mouseX, mouseY, (int) Math.round(this.skillsScrollOffset));
            graphics.disableScissor();

            if (this.skillsMaxScroll > 0.0D) {
                renderScrollbar(graphics, layout, metrics.contentHeight(), this.skillsScrollOffset, this.skillsMaxScroll);
            }

            return hoverData;
        }

        List<ProfessionEntry> professions = orderedProfessions();
        GridMetrics metrics = professionGridMetrics(layout, professions.size(), false);
        this.statsMaxScroll = Math.max(0.0D, metrics.contentHeight() - layout.bodyHeight());

        if (this.statsMaxScroll > 0.0D) {
            metrics = professionGridMetrics(layout, professions.size(), true);
            this.statsMaxScroll = Math.max(0.0D, metrics.contentHeight() - layout.bodyHeight());
        }

        this.statsScrollOffset = Mth.clamp(this.statsScrollOffset, 0.0D, this.statsMaxScroll);
        graphics.enableScissor(layout.bodyX(), layout.bodyY(), layout.bodyX() + metrics.contentWidth(), layout.bodyBottom());
        HoverData hoverData = renderStatsBody(graphics, layout, metrics, professions, mouseX, mouseY, (int) Math.round(this.statsScrollOffset));
        graphics.disableScissor();

        if (this.statsMaxScroll > 0.0D) {
            renderScrollbar(graphics, layout, metrics.contentHeight(), this.statsScrollOffset, this.statsMaxScroll);
        }

        return hoverData;
    }

    private HoverData renderSkillsBody(
            GuiGraphics graphics,
            Layout layout,
            SkillMetrics metrics,
            int mouseX,
            int mouseY,
            int scroll
    ) {
        List<SkillSection> sections = skillSections();
        ClientPlayerSkills.Snapshot snapshot = ClientPlayerSkills.snapshot();
        int originX = layout.bodyX();
        int y = layout.bodyY() - scroll;

        List<SummaryCard> cards = List.of(
                new SummaryCard(Component.translatable("screen.simple.b_menu.summary.ep"), Integer.toString(snapshot.ep()), 0xFFCC9A52),
                new SummaryCard(Component.translatable("screen.simple.b_menu.summary.bxp"), Integer.toString(snapshot.bXp()), 0xFF5E9E8B),
                new SummaryCard(Component.translatable("screen.simple.b_menu.summary.unlocked"), Integer.toString(snapshot.unlockedCount()), 0xFF7AA2C2),
                new SummaryCard(Component.translatable("screen.simple.b_menu.summary.total"), Integer.toString(SkillRegistry.totalCount()), 0xFFC86A7A)
        );

        for (int index = 0; index < cards.size(); index++) {
            SummaryTile tile = summaryTile(originX, y, metrics, index, cards.get(index));
            renderSummaryTile(graphics, tile);
        }

        y += metrics.summaryHeight() + SECTION_GAP;
        HoverData hovered = null;

        for (SkillSection section : sections) {
            graphics.drawString(
                    this.font,
                    Component.translatable("screen.simple.b_menu.section.tier", Component.translatable(section.tier().translationKey())),
                    originX,
                    y,
                    PRIMARY_TEXT,
                    false
            );
            y += SECTION_HEADER_HEIGHT;

            for (int index = 0; index < section.entries().size(); index++) {
                SkillTile tile = skillTile(originX, y, metrics, index, section.entries().get(index));
                boolean hoveredNow = tile.contains(mouseX, mouseY);
                renderSkillTile(graphics, tile, hoveredNow);

                if (hoveredNow) {
                    hovered = new HoverData(skillTooltip(tile.entry().definition(), tile.entry().unlocked()));
                }
            }

            int rows = section.rows(metrics.skillColumns());
            y += rows * SKILL_TILE_HEIGHT;
            y += Math.max(0, rows - 1) * TILE_GAP;
            y += SECTION_GAP;
        }

        return hovered;
    }

    private HoverData renderStatsBody(
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

        for (int index = 0; index < professions.size(); index++) {
            ProfessionTile tile = professionTile(originX, originY, metrics, index, professions.get(index));
            boolean hovered = tile.contains(mouseX, mouseY);
            renderProfessionTile(graphics, tile, hovered);

            if (hovered) {
                ProfessionEntry entry = tile.entry();
                return new HoverData(ProfessionDisplayRegistry.tooltip(entry.id(), professionLabel(entry.id()), entry.progress()));
            }
        }

        return null;
    }

    private void renderSummaryTile(GuiGraphics graphics, SummaryTile tile) {
        graphics.fill(tile.x(), tile.y(), tile.right(), tile.bottom(), CARD_COLOR);
        graphics.renderOutline(tile.x(), tile.y(), tile.width(), tile.height(), CARD_OUTLINE);
        graphics.fill(tile.x(), tile.y(), tile.right(), tile.y() + 3, tile.card().accentColor());
        graphics.drawString(this.font, tile.card().label(), tile.x() + 8, tile.y() + 8, MUTED_TEXT, false);
        graphics.drawString(this.font, tile.card().value(), tile.x() + 8, tile.y() + 22, tile.card().accentColor(), false);
    }

    private void renderSkillTile(GuiGraphics graphics, SkillTile tile, boolean hovered) {
        SkillDefinition definition = tile.entry().definition();
        boolean unlocked = tile.entry().unlocked();
        int accent = definition.tier().accentColor();
        int outline = hovered ? accent : (unlocked ? 0xBB4E667A : CARD_OUTLINE);

        graphics.fill(tile.x(), tile.y(), tile.right(), tile.bottom(), hovered ? CARD_HOVER : CARD_COLOR);
        graphics.fill(tile.x(), tile.y(), tile.right(), tile.y() + 3, accent);
        graphics.renderOutline(tile.x(), tile.y(), tile.width(), tile.height(), outline);

        graphics.drawString(
                this.font,
                Component.literal(trimLabel(Component.translatable(definition.nameKey()).getString(), tile.width() - 16)),
                tile.x() + 8,
                tile.y() + 8,
                PRIMARY_TEXT,
                false
        );
        graphics.drawString(this.font, Component.translatable(definition.tier().translationKey()), tile.x() + 8, tile.y() + 20, MUTED_TEXT, false);

        Component badge = Component.translatable(unlocked
                ? "screen.simple.b_menu.skill.status.unlocked"
                : "screen.simple.b_menu.skill.status.locked");
        int badgeWidth = this.font.width(badge);
        int badgeX = tile.right() - badgeWidth - 12;
        int badgeY = tile.y() + 8;
        graphics.fill(badgeX - 4, badgeY - 2, badgeX + badgeWidth + 4, badgeY + 9, unlocked ? 0x334A7D6D : 0x334A5663);
        graphics.drawString(this.font, badge, badgeX, badgeY, unlocked ? 0xFFCFE9DC : MUTED_TEXT, false);

        Component value = unlocked
                ? Component.translatable("screen.simple.b_menu.skill.ep_bonus_short", definition.epValue())
                : Component.translatable("screen.simple.b_menu.skill.requirement_short", definition.requiredBxp());
        graphics.drawString(this.font, value, tile.x() + 8, tile.y() + 43, accent, false);
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

        graphics.drawCenteredString(this.font, Integer.toString(entry.progress().level()), tile.x() + (tile.width() / 2), tile.y() + 24, accent);
    }

    private boolean currentLoading() {
        return this.activeTab == Tab.SKILLS ? ClientPlayerSkills.isLoading() : ClientPlayerStats.isLoading();
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

    private GridMetrics professionGridMetrics(Layout layout, int itemCount, boolean showScrollbar) {
        int contentWidth = layout.bodyContentWidth(showScrollbar);
        int columns = Math.max(1, Math.min(MAX_PROFESSION_COLUMNS, (contentWidth + TILE_GAP) / (MIN_PROFESSION_TILE_WIDTH + TILE_GAP)));
        int tileWidth = Math.max(96, (contentWidth - ((columns - 1) * TILE_GAP)) / columns);
        int rows = Math.max(1, (Math.max(itemCount, 1) + columns - 1) / columns);
        int contentHeight = itemCount == 0
                ? EMPTY_STATE_HEIGHT
                : (rows * PROFESSION_TILE_HEIGHT) + (Math.max(0, rows - 1) * TILE_GAP);
        return new GridMetrics(contentWidth, columns, tileWidth, contentHeight);
    }

    private SkillMetrics skillMetrics(Layout layout, boolean showScrollbar) {
        int contentWidth = layout.bodyContentWidth(showScrollbar);
        int summaryColumns = Math.max(2, Math.min(4, (contentWidth + TILE_GAP) / (MIN_SUMMARY_TILE_WIDTH + TILE_GAP)));
        int summaryTileWidth = Math.max(96, (contentWidth - ((summaryColumns - 1) * TILE_GAP)) / summaryColumns);
        int summaryRows = (4 + summaryColumns - 1) / summaryColumns;
        int summaryHeight = (summaryRows * SUMMARY_TILE_HEIGHT) + (Math.max(0, summaryRows - 1) * TILE_GAP);

        int skillColumns = Math.max(1, Math.min(MAX_SKILL_COLUMNS, (contentWidth + TILE_GAP) / (MIN_SKILL_TILE_WIDTH + TILE_GAP)));
        int skillTileWidth = Math.max(128, (contentWidth - ((skillColumns - 1) * TILE_GAP)) / skillColumns);
        int contentHeight = summaryHeight + SECTION_GAP;

        for (SkillSection section : skillSections()) {
            int rows = section.rows(skillColumns);
            contentHeight += SECTION_HEADER_HEIGHT;
            contentHeight += rows * SKILL_TILE_HEIGHT;
            contentHeight += Math.max(0, rows - 1) * TILE_GAP;
            contentHeight += SECTION_GAP;
        }

        return new SkillMetrics(contentWidth, summaryColumns, summaryTileWidth, summaryHeight, skillColumns, skillTileWidth, contentHeight);
    }

    private void renderScrollbar(GuiGraphics graphics, Layout layout, int contentHeight, double scrollOffset, double maxScroll) {
        int trackX = layout.bodyRight() - SCROLLBAR_WIDTH;
        int trackY = layout.bodyY() + 2;
        int trackHeight = layout.bodyHeight() - 4;
        graphics.fill(trackX, trackY, trackX + SCROLLBAR_WIDTH, trackY + trackHeight, SCROLL_TRACK);

        int thumbHeight = Math.max(18, Math.round(trackHeight * (layout.bodyHeight() / (float) contentHeight)));
        int travel = Math.max(1, trackHeight - thumbHeight);
        int thumbY = trackY + (int) Math.round((scrollOffset / maxScroll) * travel);
        graphics.fill(trackX - 1, thumbY, trackX + SCROLLBAR_WIDTH + 1, thumbY + thumbHeight, SCROLL_THUMB);
    }

    private SummaryTile summaryTile(int originX, int originY, SkillMetrics metrics, int index, SummaryCard card) {
        int column = index % metrics.summaryColumns();
        int row = index / metrics.summaryColumns();
        int x = originX + column * (metrics.summaryTileWidth() + TILE_GAP);
        int y = originY + row * (SUMMARY_TILE_HEIGHT + TILE_GAP);
        return new SummaryTile(x, y, metrics.summaryTileWidth(), SUMMARY_TILE_HEIGHT, card);
    }

    private SkillTile skillTile(int originX, int originY, SkillMetrics metrics, int index, SkillEntry entry) {
        int column = index % metrics.skillColumns();
        int row = index / metrics.skillColumns();
        int x = originX + column * (metrics.skillTileWidth() + TILE_GAP);
        int y = originY + row * (SKILL_TILE_HEIGHT + TILE_GAP);
        return new SkillTile(x, y, metrics.skillTileWidth(), SKILL_TILE_HEIGHT, entry);
    }

    private ProfessionTile professionTile(int originX, int originY, GridMetrics metrics, int index, ProfessionEntry entry) {
        int column = index % metrics.columns();
        int row = index / metrics.columns();
        int x = originX + column * (metrics.tileWidth() + TILE_GAP);
        int y = originY + row * (PROFESSION_TILE_HEIGHT + TILE_GAP);
        return new ProfessionTile(x, y, metrics.tileWidth(), PROFESSION_TILE_HEIGHT, entry);
    }

    private List<SkillSection> skillSections() {
        EnumMap<SkillTier, List<SkillEntry>> byTier = new EnumMap<>(SkillTier.class);

        for (SkillTier tier : SkillTier.values()) {
            byTier.put(tier, new ArrayList<>());
        }

        ClientPlayerSkills.Snapshot snapshot = ClientPlayerSkills.snapshot();

        for (SkillDefinition definition : SkillRegistry.all()) {
            byTier.get(definition.tier()).add(new SkillEntry(definition, snapshot.isUnlocked(definition.id())));
        }

        ArrayList<SkillSection> ordered = new ArrayList<>();

        for (SkillTier tier : SkillTier.values()) {
            List<SkillEntry> entries = List.copyOf(byTier.get(tier));

            if (!entries.isEmpty()) {
                ordered.add(new SkillSection(tier, entries));
            }
        }

        return List.copyOf(ordered);
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

    private List<Component> skillTooltip(SkillDefinition definition, boolean unlocked) {
        ArrayList<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(definition.nameKey()));
        lines.add(Component.translatable("screen.simple.b_menu.tooltip.tier", Component.translatable(definition.tier().translationKey()))
                .withColor(definition.tier().accentColor()));
        lines.add(Component.translatable(
                unlocked ? "screen.simple.b_menu.skill.status.unlocked" : "screen.simple.b_menu.skill.status.locked"
        ).withColor(unlocked ? 0xFFB6E0CB : 0xFFC2CCD6));

        if (I18n.exists(definition.descriptionKey())) {
            lines.add(Component.translatable(definition.descriptionKey()).withColor(MUTED_TEXT));
        }

        lines.add(Component.translatable("screen.simple.b_menu.tooltip.ep_bonus", definition.epValue()).withColor(definition.tier().accentColor()));
        lines.add(Component.translatable("screen.simple.b_menu.tooltip.requirement", definition.requiredBxp()).withColor(MUTED_TEXT));

        if (definition.prerequisites().isEmpty()) {
            lines.add(Component.translatable("screen.simple.b_menu.tooltip.prerequisites.none").withColor(MUTED_TEXT));
        } else {
            String joined = definition.prerequisites().stream()
                    .map(this::skillLabel)
                    .map(Component::getString)
                    .reduce((left, right) -> left + ", " + right)
                    .orElse("");
            lines.add(Component.translatable("screen.simple.b_menu.tooltip.prerequisites", joined).withColor(MUTED_TEXT));
        }

        return List.copyOf(lines);
    }

    private Component professionLabel(Identifier id) {
        String key = "profession." + id.getNamespace() + "." + id.getPath();

        if (I18n.exists(key)) {
            return Component.translatable(key);
        }

        return Component.literal(titleCase(id.getPath()));
    }

    private Component skillLabel(Identifier id) {
        return SkillRegistry.byId(id)
                .map(skill -> Component.translatable(skill.nameKey()))
                .orElse(Component.literal(titleCase(id.getPath())));
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

    private enum Tab {
        SKILLS,
        STATS
    }

    private record ProfessionEntry(Identifier id, ProfessionProgress progress) {
    }

    private record SummaryCard(Component label, String value, int accentColor) {
    }

    private record SummaryTile(int x, int y, int width, int height, SummaryCard card) {
        private int right() {
            return x + width;
        }

        private int bottom() {
            return y + height;
        }
    }

    private record SkillEntry(SkillDefinition definition, boolean unlocked) {
    }

    private record SkillSection(SkillTier tier, List<SkillEntry> entries) {
        private int rows(int columns) {
            return Math.max(1, (entries.size() + columns - 1) / columns);
        }
    }

    private record SkillTile(int x, int y, int width, int height, SkillEntry entry) {
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

    private record GridMetrics(int contentWidth, int columns, int tileWidth, int contentHeight) {
    }

    private record SkillMetrics(
            int contentWidth,
            int summaryColumns,
            int summaryTileWidth,
            int summaryHeight,
            int skillColumns,
            int skillTileWidth,
            int contentHeight
    ) {
    }

    private record HoverData(List<Component> lines) {
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
