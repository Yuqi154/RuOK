package team.teampotato.ruok.gui.modern;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import team.teampotato.ruok.RuOKMod;
import team.teampotato.ruok.config.RuOK;
import team.teampotato.ruok.gui.modern.option.ModernOption;
import team.teampotato.ruok.gui.modern.screens.ConfigHUDScreen;
import team.teampotato.ruok.gui.modern.screens.DeBugScreen;
import team.teampotato.ruok.gui.modern.screens.EntityListScreen;
import team.teampotato.ruok.gui.modern.screens.ParticleListScreen;
import team.teampotato.ruok.gui.modern.storage.ModernOptionsStorage;
import team.teampotato.ruok.gui.modern.widget.ButtonWidget;
import team.teampotato.ruok.gui.modern.widget.MessageWidget;
import team.teampotato.ruok.gui.modern.widget.list.OptionListWidget;
import team.teampotato.ruok.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;


public class MainScreen extends Screen {
    private final Minecraft mc = Minecraft.getInstance();
    private ButtonWidget currentButton;
    private final Component NORMAL_SETTING_TEXT = Component.translatable("ruok.setting.normal");
    private final Component OTHER_SETTING_TEXT = Component.translatable("ruok.setting.other");
    private final Component HUD_SETTING_TEXT = Component.translatable("ruok.setting.hud");
    private final Component LIST_SETTING_TEXT = Component.translatable("ruok.setting.list");
    private final Component HUD_CONFIG_TEXT = Component.translatable("ruok.config.hud");
    private final Component DEBUG_HUD_TEXT = Component.translatable("ruok.setting.debug");
    private int ButtEndX = 0;
    public List<ButtonWidget> buttonWidgets = Lists.newArrayList();
    @Nullable
    public List<ModernOption<?>> currentOptionList = this.getDefaultList();
    private int oopsButtonCount = 0;
    private Screen parent;
    private boolean onOptionList = false;

    public MainScreen(Component title,Screen parent) {
        super(title);
        this.parent = parent;
    }
    public List<AbstractWidget> getSettingList() {
        int baseX = ButtEndX + 3 + 1; // 计算基础 X 坐标
        int listWidth = this.width - baseX; // 计算 OptionList 宽度
        List<AbstractWidget> clickableWidgets = Lists.newArrayList();
        int baseWidth = this.width - ButtEndX;
        int buttonWidth = baseWidth - 20;
        AbstractWidget messageWidget = new MessageWidget(baseX,baseX+listWidth,buttonWidth,20,Component.translatable("ruok.options.gui.view.type.info"));
        AbstractWidget setModeWidget = ButtonWidget.builder(Component.translatable("ruok.options.gui.view.type.ordinary"),(e) -> {
            RuOK.get().GuiViewMode = GuiViewType.ORDINARY;
            RuOK.save();
            if(RuOK.get().DeBug) {
                ToastUtil.send(Component.translatable("ruok.options.gui.view.type.oops.title"),Component.translatable("ruok.options.gui.view.type.oops.info"));
            } else {
                ToastUtil.send(Component.translatable("ruok.options.gui.view.type.selection.done"),Component.translatable("ruok.options.gui.view.type.toast.ordinary"));
            }
            this.initLeftButton();
            this.rebuildWidgets();
        }).build();
        AbstractWidget setModeWidgets = ButtonWidget.builder(Component.translatable("ruok.options.gui.view.type.technical"),(e) -> {
            RuOK.get().GuiViewMode = GuiViewType.TECHNICAL;
            RuOK.get().DeBug = true;
            RuOK.save();
            ToastUtil.send(Component.translatable("ruok.options.gui.view.type.selection.done"),Component.translatable("ruok.options.gui.view.type.toast.technical"));
            this.initLeftButton();
            this.rebuildWidgets();
        }).build();
        messageWidget.setWidth(baseWidth);
        setModeWidget.setWidth(baseWidth);
        setModeWidget.setTooltip(Tooltip.create(Component.translatable("ruok.options.gui.view.type.ordinary.tooltip")));
        setModeWidgets.setWidth(baseWidth);
        setModeWidgets.setTooltip(Tooltip.create(Component.translatable("ruok.options.gui.view.type.technical.tooltip")));
        clickableWidgets.add(messageWidget);//baseX,baseX+listWidth+25,baseWidth,20
        clickableWidgets.add(setModeWidget);
        clickableWidgets.add(setModeWidgets);
        return clickableWidgets;
    }

    public List<ModernOption<?>> getDefaultList() {
        return ModernOptionsStorage.getMainOptions();
    }
    @Override
    protected void init() {
        this.initLeftButton();
        super.init();
        this.addLeftButton(this.buttonWidgets);
        this.addRightButton(currentOptionList);
        this.addRenderableWidget(this.getRightDoneButton());
    }
    public AbstractWidget getRightDoneButton() {
        ButtonWidget.Builder clickableWidget = ButtonWidget.builder(CommonComponents.GUI_DONE,(e) -> mc.setScreen(this.parent));
        clickableWidget.dimensions(this.width-110,this.height-23,100,15);
        return clickableWidget.build();
    }

    public void renderLeftDoneButton(AbstractWidget widget, GuiGraphics context) {
        int color = -804253680;  // 容器颜色
        int width = widget.getWidth();
        int height = widget.getHeight();
        int bX = widget.getX();
        int bY = widget.getY();
        context.fill(bX,bY,bX+width,bY+height,color);
    }
    private void initLeftButton() {
        if(this.buttonWidgets.isEmpty()){
            this.buttonWidgets = this.getLeftButton();
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
    }
    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.renderLeftBackground(context, Component.translatable("ruok.options.gui.ruok"));
        this.renderRightBackground(context);
        this.renderCurrentButton(context);
        this.renderLeftDoneButton(this.getRightDoneButton(),context);
        this.renderLeftDownText(context);
        super.render(context, mouseX, mouseY, delta);
    }
    public void renderLeftDownText(GuiGraphics context) {
        int startX = 0;
        int endX = ButtEndX;
        int startY = this.height - (50);
        int endY = this.height;

        context.hLine(RenderType.gui(), startX, endX, startY, -1);

        var pn = Minecraft.getInstance().getUser().getName();
        // 处理玩家名称，防止 NPE
        Component playerName = (this.mc.player != null) ? this.mc.player.getName() : Component.literal(pn);

        Component text = Component.translatable("ruok.options.gui.left.down.text.player", playerName);
        context.drawString(this.font, text, startX + 5, startY+5, 0xFFFFFF, false);

        // TODO
        context.drawString(this.font, Component.translatable("ruok.options.gui.left.down.text.ruok",RuOKMod.VERSION), startX + 5, endY - 15, 11184810, false);
    }


    private void renderCurrentButton(GuiGraphics context) {
        if(RuOK.get().GuiViewMode.equals(GuiViewType.NULL)) return;//如果未选择,则不执行逻辑
        if (this.getCurrentButton()!=null) {
            int startY = this.getCurrentButton().getY();
            int EndY = this.getCurrentButton().getY()+this.getCurrentButton().getHeight();
            int startX = this.getCurrentButton().getX();
            context.vLine(
                    RenderType.gui(),
                    startX,
                    startY,
                    EndY,
                       -1
            );
        }
    }

    private void oopsButton() {
        //第一次点击彩蛋,只提醒一次
        if(oopsButtonCount == 1){
            ToastUtil.send(Component.translatable("ruok.options.gui.view.type.oops.button.title"),Component.translatable( "ruok.options.gui.view.type.oops.button.info.1"));
        }
        if(oopsButtonCount == 10)ToastUtil.send(Component.translatable("ruok.options.gui.view.type.oops.button.title"),Component.translatable( "ruok.options.gui.view.type.oops.button.info.2"));
        if(oopsButtonCount == 100)ToastUtil.send(Component.translatable("ruok.options.gui.view.type.oops.button.title"),Component.translatable( "ruok.options.gui.view.type.oops.button.info.3"));
        if(oopsButtonCount == 500)ToastUtil.send(Component.translatable("ruok.options.gui.view.type.oops.button.title"),Component.translatable( "ruok.options.gui.view.type.oops.button.info.4"));
        if(oopsButtonCount == 1000)ToastUtil.send(Component.translatable("ruok.options.gui.view.type.oops.button.title"),Component.translatable( "ruok.options.gui.view.type.oops.button.info.5"));
        if(oopsButtonCount == 2000)ToastUtil.send(Component.translatable("ruok.options.gui.view.type.oops.button.title"),Component.translatable( "ruok.options.gui.view.type.oops.button.info.6"));
        if(oopsButtonCount == 2005){
            ToastUtil.send(Component.translatable("ruok.options.gui.view.type.oops.button.title"),Component.translatable("ruok.options.gui.view.type.oops.button.info.7"));
            for (ButtonWidget buttonWidget : buttonWidgets) {
                this.removeWidget(buttonWidget);
            }

        }
        this.oopsButtonCount ++;
    }

    @Override
    public void tick() {
        super.tick();
    }

    public void setCurrentButton(ButtonWidget currentButton) {
        this.currentButton = currentButton;
    }

    public void setCurrentOptionList(@Nullable List<ModernOption<?>> currentOptionList) {
        this.currentOptionList = currentOptionList;
    }
    public void setSelectableOption(List<ModernOption<?>> options,ButtonWidget buttonWidget) {
        this.onOptionList = false;
        if(RuOK.get().GuiViewMode.equals(GuiViewType.NULL)) {
            this.oopsButton();
        } else {
            if(oopsButtonCount > 10) {
                ToastUtil.send(Component.translatable("ruok.options.gui.view.type.oops.button.done.title"),Component.translatable("ruok.options.gui.view.type.oops.button.done.info"));
                this.oopsButtonCount = 0;
            }
            this.setCurrentOptionList(options);
            this.rebuildWidgets();
            this.setCurrentButton(buttonWidget);
        }

    }

    public ButtonWidget getCurrentButton() {
        return currentButton;
    }
    public List<ButtonWidget> getLeftButton() {
        List<ButtonWidget> buttonWidgets = new ArrayList<>();
        buttonWidgets.add(ButtonWidget.builder(NORMAL_SETTING_TEXT,(e) -> {
            this.setSelectableOption(ModernOptionsStorage.getMainOptions(),e);
        }).build());
        buttonWidgets.add(ButtonWidget.builder(OTHER_SETTING_TEXT,(e) -> {
            this.setSelectableOption(ModernOptionsStorage.getOtherOptions(),e);
        }).build());
        buttonWidgets.add(ButtonWidget.builder(HUD_SETTING_TEXT,(e) -> {
            this.setSelectableOption(ModernOptionsStorage.getHudOptions(),e);
        }).build());
        buttonWidgets.add(ButtonWidget.builder(LIST_SETTING_TEXT,(e) -> {
            this.onOptionList = true;
            this.rebuildWidgets();
            this.setCurrentButton(e);
        }).build());
        buttonWidgets.add(ButtonWidget.builder(HUD_CONFIG_TEXT,(e) -> {
            mc.setScreen(new ConfigHUDScreen(HUD_CONFIG_TEXT,this));
            this.rebuildWidgets();
            this.onOptionList = false;
        }).build());
        if(RuOK.get().DeBug){
            buttonWidgets.add(ButtonWidget.builder(DEBUG_HUD_TEXT,(e) -> {
                mc.setScreen(new DeBugScreen(this,DEBUG_HUD_TEXT));
                this.rebuildWidgets();
                this.setCurrentButton(e);
                this.onOptionList = false;
            }).build());
        }
        return buttonWidgets;
    }
    public int EndHeadY;
    public void renderLeftBackground(GuiGraphics context, Component title) {
        int color = -804253680;  // 容器颜色
        int height = context.guiHeight();
        if(mc.level == null) {
            this.renderMenuBackground(context);
        }
        int containerX = 20;  // 容器的起始X坐标
        int containerY = 40;  // 容器的起始Y坐标
        int containerWidth = 100;  // 容器的宽度
        // 绘制容器背景
        context.fill(0, 0, containerX + containerWidth, height, color);
        // 绘制标题，居中显示
        this.drawCenteredText(context, title, containerX-15, 5, 1.5F);
        this.ButtEndX = containerX + containerWidth;

        // 设置项开始绘制位置
        int yOffset = (containerY/2) +20;  // 标题下方开始放置设置项
        this.EndHeadY = yOffset;
        //40
        context.hLine(//绘制按钮间隔中间的横线间隔线
                RenderType.gui(),
                0,
                ButtEndX,
                yOffset-(8+5),
                -1
        );
        context.hLine(//绘制按钮间隔中间的横线间隔线
                RenderType.gui(),
                ButtEndX,
                context.guiWidth(),
                yOffset-(8+5),
                -1
        );
    }

    public void addLeftButton(List<ButtonWidget> list) {
        int containerX = 20;  // 容器的起始X坐标
        int containerY = 40;  // 容器的起始Y坐标
        int containerWidth = 100;  // 容器的宽度
        this.ButtEndX = containerX + containerWidth;
        // 设置项开始绘制位置
        int yOffset = (containerY/2) +20;  // 标题下方开始放置设置项
        this.EndHeadY = yOffset;
        int wid = containerX + containerWidth;
        // 绘制设置项
        for (ButtonWidget widget : list) {
            widget.setWidth(wid);
            widget.setX(0);
            widget.setY(yOffset-8);
            this.addRenderableWidget(widget);
            yOffset += 18;  // 每个设置项之间的垂直间距
        }
    }

    public void renderRightBackground(GuiGraphics context) {
        int color = -804253680;  // 原容器颜色
        // 提取 ARGB 分量
        int alpha = (color >> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        // 增加透明度的 30%（即减少透明度）
        int newAlpha = (int)(alpha * 0.7);  // 增加透明度的 30%
        // 重新组合成新的颜色
        int newColor = (newAlpha << 24) | (red << 16) | (green << 8) | blue;
        context.vLine(
                RenderType.gui(),
                ButtEndX,
                0,
                context.guiWidth(),
                -1
        );
        // 绘制容器背景
        context.fill(ButtEndX, 0, context.guiWidth(), height, newColor);
    }
    public void addRightButton(List<ModernOption<?>> list) {
        int baseX = ButtEndX + 3 + 1; // 计算基础 X 坐标
        int baseY = EndHeadY - (12);  // 计算基础 Y 坐标
        int listWidth = this.width - baseX; // 计算 OptionList 宽度
        int listHeight = this.height - baseY; // 计算 OptionList 高度
        int rq = this.width - ButtEndX;

        // **entryX 调整**
        int rowLeft = baseX+20; // 让列表项从 baseX 位置开始，而不是 baseX + ButtEndX

        OptionListWidget lists = new OptionListWidget(
                mc,
                listWidth,   // ✅ 修正宽度，避免过大
                listHeight,
                baseY,
                this.height-20,
                20,
                rowLeft
        );
        lists.setRowLeft(ButtEndX+1);
        lists.setRowWidth(rq);
        lists.setX1(ButtEndX+1); // ✅ 用新的命名方法
        lists.setX2(baseX + listWidth);
        lists.setY1(baseY);
        lists.setY2(this.height-30);
        if(RuOK.get().GuiViewMode.equals(GuiViewType.NULL)){
            lists.addAll(this.getSettingList());
        } else if (this.onOptionList) {
            lists.addAll(this.getOptionList());
        } else {
            lists.addAll(list, rq-20);
        }

        // 添加到 UI
        this.addRenderableWidget(lists);
    }
    public final Component WHITE_ENTITY_TEXT = Component.translatable("ruok.options.gui.white.entity");
    public final Component BLACK_ENTITY_TEXT = Component.translatable("ruok.options.gui.black.entity");
    public final Component WHITE_PARTICLE_TEXT = Component.translatable("ruok.options.gui.white.particle");
    public final Component BLACK_PARTICLE_TEXT = Component.translatable("ruok.options.gui.black.particle");

    public List<AbstractWidget> getOptionList() {
        int baseX = ButtEndX + 3 + 1; // 计算基础 X 坐标
        int listWidth = this.width - baseX; // 计算 OptionList 宽度
        int baseWidth = this.width - ButtEndX;
        int buttonWidth = baseWidth - 20;
        List<AbstractWidget> widgets = Lists.newArrayList();
        AbstractWidget messageWidget = new MessageWidget(baseX,baseX+listWidth,buttonWidth,20,Component.translatable("ruok.options.gui.list.info"));
        AbstractWidget whiteParticle = ButtonWidget.builder(WHITE_PARTICLE_TEXT,(e) -> mc.setScreen(new ParticleListScreen(WHITE_PARTICLE_TEXT,this, RuOK.get().WhiteListedParticle))).size(baseWidth,20).build();
        AbstractWidget blackParticle = ButtonWidget.builder(BLACK_PARTICLE_TEXT,(e) -> mc.setScreen(new ParticleListScreen(BLACK_PARTICLE_TEXT,this,RuOK.get().BlackListedParticle))).size(baseWidth,20).build();
        AbstractWidget whiteEntity = ButtonWidget.builder(WHITE_ENTITY_TEXT,(e) -> mc.setScreen(new EntityListScreen(WHITE_ENTITY_TEXT,this,RuOK.get().whiteListedEntities))).size(baseWidth,20).build();
        AbstractWidget blackEntity = ButtonWidget.builder(BLACK_ENTITY_TEXT,(e) -> mc.setScreen(new EntityListScreen(BLACK_ENTITY_TEXT,this, RuOK.get().blackListedEntities))).size(baseWidth,20).build();
        messageWidget.setWidth(baseWidth);

        whiteParticle.setWidth(baseWidth);
        whiteParticle.setTooltip(Tooltip.create(Component.translatable("ruok.options.gui.list.particle.white")));

        blackParticle.setWidth(baseWidth);
        blackParticle.setTooltip(Tooltip.create(Component.translatable("ruok.options.gui.list.particle.black")));

        whiteEntity.setWidth(baseWidth);
        whiteEntity.setTooltip(Tooltip.create(Component.translatable("ruok.options.gui.list.entity.white")));
        blackEntity.setWidth(baseWidth);
        blackEntity.setTooltip(Tooltip.create(Component.translatable("ruok.options.gui.list.entity.black")));

        widgets.add(messageWidget);
        widgets.add(whiteParticle);
        widgets.add(blackParticle);
        widgets.add(whiteEntity);
        widgets.add(blackEntity);
        return widgets;
    }

    @Override
    protected void rebuildWidgets() {
        this.clearWidgets();
        this.blur();
        this.init();
    }

    @Override
    protected void repositionElements() {
        super.repositionElements();
    }

    private void blur() {
        ComponentPath guiNavigationPath = this.getCurrentFocusPath();
        if (guiNavigationPath != null) {
            guiNavigationPath.applyFocus(false);
        }

    }

    // 绘制居中的文字
    private void drawCenteredText(GuiGraphics context, Component text, int x, int y, float scale) {
        // 保存当前的矩阵状态
        PoseStack matrixStack = context.pose();
        matrixStack.pushPose();
        // 应用缩放
        matrixStack.scale(scale, scale, 1.0F);  // 这里的scale是缩放比例

        // 绘制文字
        context.drawString(mc.font, text, x, y, 0xFFFFFF, false);

        // 恢复矩阵状态
        matrixStack.popPose();
    }


}
