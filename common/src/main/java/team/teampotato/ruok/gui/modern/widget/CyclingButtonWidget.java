package team.teampotato.ruok.gui.modern.widget;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.teampotato.ruok.gui.modern.option.ModernOption;
import team.teampotato.ruok.util.ColorUtil;

import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class CyclingButtonWidget<T> extends AbstractButton {
    public static final BooleanSupplier HAS_ALT_DOWN = Screen::hasAltDown;
    private static final List<Boolean> BOOLEAN_VALUES;
    private int index;
    private T value;
    private final Values<T> values;
    private final Function<T, Component> valueToText;
    private final Function<CyclingButtonWidget<T>, MutableComponent> narrationMessageFactory;
    private final UpdateCallback<T> callback;
    private final boolean optionTextOmitted;
    private final ModernOption.TooltipFactory<T> tooltipFactory;

    CyclingButtonWidget(int x, int y, int width, int height, Component message, int index, T value, Values<T> values, Function<T, Component> valueToText, Function<CyclingButtonWidget<T>, MutableComponent> narrationMessageFactory, UpdateCallback<T> callback, ModernOption.TooltipFactory<T> tooltipFactory, boolean optionTextOmitted) {
        super(x, y, width, height, message);
        //this.optionText = optionText;
        this.index = index;
        this.value = value;
        this.values = values;
        this.valueToText = valueToText;
        this.narrationMessageFactory = narrationMessageFactory;
        this.callback = callback;
        this.optionTextOmitted = optionTextOmitted;
        this.tooltipFactory = tooltipFactory;
        this.refreshTooltip();
    }

    private void refreshTooltip() {
        this.setTooltip(this.tooltipFactory.apply(this.value));
    }

    public void onPress() {
        if (Screen.hasShiftDown()) {
            this.cycle(-1);
        } else {
            this.cycle(1);
        }


    }
    public int getValueInt() {
        T object = this.getValue(1);
        if (object instanceof Boolean e) {
            return e ? 1 : 0;
        }
        if (object instanceof Enum<?> e) {
            return e.ordinal();
        }
        return -1; // 其他未知类型返回 -1，表示无效
    }
    public int getValueMaxInt() {
        T object = this.getValue(1);
        if (object instanceof Enum<?> e) {
            Enum<?>[] values = e.getDeclaringClass().getEnumConstants();
            if (values != null && values.length > 0) {
                return values[values.length - 1].ordinal(); // 获取最大 ordinal 值
            }
        }
        if (object instanceof Boolean) {
            return 1;
        }
        return -1; // 非 Enum 类型或无法确定最大值时返回 -1
    }

    public MutableComponent getValueToText() {
        T object = this.getValue(1);
        Component text = this.valueToText.apply(object);
        return Component.empty().append(text);
    }
    public boolean isEnum() {
        return this.value instanceof Enum<?>;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        Minecraft minecraftClient = Minecraft.getInstance();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();

        int alphaValue = Mth.ceil(this.alpha * 255.0F);  // 获取动态透明度

        int color = this.isHoveredOrFocused() ? ColorUtil.getAUIColor(-804253680,0.8) : ColorUtil.getAUIColor(-804253680,0.5);
        // 绘制填充矩形按钮背景
        context.fill(
                this.getX(),
                this.getY(),
                this.getX() + this.getWidth(),
                this.getY() + this.getHeight(),
                color
        );
        if(!this.isEnum()) renderBooleanButton(context);
        else renderButtonSetting(context,mouseX,mouseY,delta);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = this.active ? 16777215 : 10526880;
        //TODO
        this.renderString(context, minecraftClient.font, i | Mth.ceil(this.alpha * 255.0F) << 24);
    }
    @Override
    public void renderString(GuiGraphics context, Font font, int color) {
        this.renderScrollingString(context, font, 2, color);
    }
    @Override
    public void renderScrollingString(GuiGraphics context, Font font, int xMargin, int color) {
        int left = this.getX() + xMargin + 5; // ✅ 让文本向右偏移 5px
        int right = this.getX() + this.getWidth() - xMargin;
        renderScrollingString(context, font, this.getMessage(), left, this.getY(), right, this.getY() + this.getHeight(), color);
    }

    public static void renderScrollingString(GuiGraphics context, Font font, Component text, int left, int top, int right, int bottom, int color) {
        int textWidth = font.width(text);
        int textHeight = 9; // 默认字体高度
        int centerY = (top + bottom - textHeight) / 2 + 1;

        int maxWidth = right - left;
        if (textWidth > maxWidth) {
            int overflowWidth = textWidth - maxWidth;
            double time = (double) Util.getMillis() / 1000.0;
            double scrollSpeed = Math.max((double) overflowWidth * 0.5, 3.0);
            double scrollFactor = Math.sin(1.5707963267948966 * Math.cos(6.283185307179586 * time / scrollSpeed)) / 2.0 + 0.5;
            double scrollX = Mth.lerp(scrollFactor, 0.0,  overflowWidth);

            context.enableScissor(left, top, right, bottom);
            context.drawString(font, text, left - (int) scrollX, centerY, color); // ✅ 让文本从 `left` 开始
            context.disableScissor();
        } else {
            context.drawString(font, text, left, centerY, color); // ✅ 让文本始终左对齐，并向右偏移 5px
        }
    }


    public void renderButtonSetting(GuiGraphics context, int mouseX, int mouseY, float delta) {
        Minecraft mc = Minecraft.getInstance();
        int endX = this.width + this.getX();

        // ✅ 计算文本位置（水平居中）
        int textX = endX - 33;
        int y = this.getY();

        // ✅ 计算线条总宽度，使其整体居中
        int maxLevel = this.getValueMaxInt()+1; // 例如：3个值（0,1,2）
        int spacing = 8; // 线条间距
        int totalWidth = (maxLevel - 1) * spacing; // 计算总宽度
        int lineXStart = endX - (totalWidth / 2) - 30; // 居中起始点

        int textColor = this.active ? 0xFFFFFF : 0xA0A0A0; // 活跃时白色，否则灰色

        // ✅ 1. 绘制文本（调整 Y 轴，使其略高于线条）
        MutableComponent text = this.getValueToText();
        context.drawString(mc.font, text, textX, y + 3, textColor, false);

        // ✅ 2. 渲染横线（= - -）表示值
        int level = this.getValueInt(); // 获取当前选中的值

        for (int i = 0; i < maxLevel; i++) {
            // ✅ 计算颜色
            int color = (i == level) ? 0xFF88FF88 : 0xFF888888; // 选中的为黄绿色，其余为灰色
            int lineX = lineXStart + (i * spacing);

            // ✅ 计算 Y 轴，使横线居中
            int lineY = y + this.height / 2 + 2 +3;

            // ✅ 绘制线条
            context.fill(lineX, lineY, lineX + 6, lineY + 2, color);
        }
    }


    public void setHeight(int height) {
        this.height = height;
    }


    @Override
    public int getHeight() {
        return super.getHeight();
    }

    private double buttonX=this.width+this.getX();  // 使用 double 类型存储按钮位置，支持平滑动画
    private final double animationSpeed = 0.15; // 控制动画速度，值越小动画越慢

    protected void renderBooleanButton(@NotNull GuiGraphics context) {
        int endX = this.width + this.getX();
        int fillXStart = endX - 15; // 背景按钮起始X位置
        int fillXEnd = endX - 5; // 背景按钮结束X位置
        int z = 7;  // 上下边距

        boolean buttonOn = this.getValueInt() == 0;

        // ✅ 计算目标位置（静态位置）
        int targetButtonX = buttonOn ? fillXStart + 3 : fillXEnd - 17;

        // ✅ 平滑插值更新按钮位置
        buttonX = lerp(buttonX, targetButtonX, animationSpeed);

        // ✅ 背景颜色渐变
        int startColor = 0xFF888888; // 灰色
        int endColor = 0xFF88FF88; // 淡绿色
        int bgColor = lerpColor(startColor, endColor, buttonOn ? 1.0f : 0.0f);

        // ✅ 绘制背景
        context.fill(fillXStart - 8, this.getY() + (this.height / 2) + 1, fillXEnd, this.getY() + (this.height / 2) - 1, bgColor);

        // ✅ 让按钮左右各加 1 格（从 7px -> 9px）
        context.fill((int)buttonX - 1, this.getY() + z, (int)buttonX + 8, this.getY() + this.height - z, 0xFFFFFFFF);
    }

    // 线性插值函数（平滑过渡）
    private double lerp(double from, double to, double progress) {
        return from + (to - from) * progress;
    }

    // 颜色线性插值函数
    private int lerpColor(int startColor, int endColor, float progress) {
        int startA = (startColor >> 24) & 0xFF;
        int startR = (startColor >> 16) & 0xFF;
        int startG = (startColor >> 8) & 0xFF;
        int startB = startColor & 0xFF;

        int endA = (endColor >> 24) & 0xFF;
        int endR = (endColor >> 16) & 0xFF;
        int endG = (endColor >> 8) & 0xFF;
        int endB = endColor & 0xFF;

        int newA = (int) lerp(startA, endA, progress);
        int newR = (int) lerp(startR, endR, progress);
        int newG = (int) lerp(startG, endG, progress);
        int newB = (int) lerp(startB, endB, progress);

        return (newA << 24) | (newR << 16) | (newG << 8) | newB;
    }

    private void cycle(int amount) {
        List<T> list = this.values.getCurrent();
        this.index = Mth.positiveModulo(this.index + amount, list.size());
        T object = list.get(this.index);
        this.internalSetValue(object);
        this.callback.onValueChange(this, object);
    }

    private T getValue(int offset) {
        List<T> list = this.values.getCurrent();
        return list.get(Mth.positiveModulo(this.index + offset, list.size()));
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (amount > 0.0) {
            this.cycle(-1);
        } else if (amount < 0.0) {
            this.cycle(1);
        }

        return true;
    }

    public void setValue(T value) {
        List<T> list = this.values.getCurrent();
        int i = list.indexOf(value);
        if (i != -1) {
            this.index = i;
        }

        this.internalSetValue(value);
    }

    private void internalSetValue(T value) {
        //Text text = this.composeText(value);
        //this.setMessage(text);
        this.value = value;
        this.refreshTooltip();
    }
//
//    private Component composeText(T value) {
//        return this.optionTextOmitted ? this.valueToText.apply(value) : this.composeGenericOptionText(value);
//    }

//    private MutableText composeGenericOptionText(T value) {
//        return CommonComponents.composeGenericOptionText(this.optionText, this.valueToText.apply(value));
//    }

    public T getValue() {
        return this.value;
    }

    protected MutableComponent getNarrationMessage() {
        return this.narrationMessageFactory.apply(this);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.getNarrationMessage());
        if (this.active) {
            T object = this.getValue(1);
            Component text = this.valueToText.apply(object);
            if (this.isFocused()) {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.cycle_button.usage.focused", new Object[]{text}));
            } else {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.cycle_button.usage.hovered", new Object[]{text}));
            }
        }
    }


    public static <T> Builder<T> builder(Function<T, Component> valueToText) {
        return new Builder<>(valueToText);
    }

    public static Builder<Boolean> onOffBuilder(Component on, Component off) {
        return new Builder<Boolean>((value) -> {
            return value ? on : off;
        }).values(BOOLEAN_VALUES);
    }

    public static Builder<Boolean> onOffBuilder() {
        return new Builder<Boolean>((value) -> {
            return value ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF;
        }).values(BOOLEAN_VALUES);
    }

    public static Builder<Boolean> onOffBuilder(boolean initialValue) {
        return onOffBuilder().initially(initialValue);
    }

    static {
        BOOLEAN_VALUES = ImmutableList.of(Boolean.TRUE, Boolean.FALSE);
    }

    @Environment(EnvType.CLIENT)
    public interface Values<T> {
        List<T> getCurrent();

        List<T> getDefaults();

        static <T> Values<T> of(Collection<T> values) {
            final List<T> list = ImmutableList.copyOf(values);
            return new Values<T>() {
                public List<T> getCurrent() {
                    return list;
                }

                public List<T> getDefaults() {
                    return list;
                }
            };
        }

        static <T> Values<T> of(final BooleanSupplier alternativeToggle, List<T> defaults, List<T> alternatives) {
            final List<T> list = ImmutableList.copyOf(defaults);
            final List<T> list2 = ImmutableList.copyOf(alternatives);
            return new Values<T>() {
                public List<T> getCurrent() {
                    return alternativeToggle.getAsBoolean() ? list2 : list;
                }

                public List<T> getDefaults() {
                    return list;
                }
            };
        }
    }

    @Environment(EnvType.CLIENT)
    public interface UpdateCallback<T> {
        void onValueChange(CyclingButtonWidget<T> button, T value);
    }

    @Environment(EnvType.CLIENT)
    public static class Builder<T> {
        private int initialIndex;
        @Nullable
        private T value;
        private final Function<T, Component> valueToText;
        private ModernOption.TooltipFactory<T> tooltipFactory = (value) -> {
            return null;
        };
        private Function<CyclingButtonWidget<T>, MutableComponent> narrationMessageFactory = CyclingButtonWidget::getMessageMutable;
        private Values<T> values = Values.of(ImmutableList.of());
        private boolean optionTextOmitted;

        public Builder(Function<T, Component> valueToText) {
            this.valueToText = valueToText;
        }

        public Builder<T> values(Collection<T> values) {
            return this.values(Values.of(values));
        }

        @SafeVarargs
        public final Builder<T> values(T... values) {
            return this.values(ImmutableList.copyOf(values));
        }

        public Builder<T> values(List<T> defaults, List<T> alternatives) {
            return this.values(Values.of(CyclingButtonWidget. HAS_ALT_DOWN, defaults, alternatives));
        }

        public Builder<T> values(BooleanSupplier alternativeToggle, List<T> defaults, List<T> alternatives) {
            return this.values(Values.of(alternativeToggle, defaults, alternatives));
        }

        public Builder<T> values(Values<T> values) {
            this.values = values;
            return this;
        }

        public Builder<T> tooltip(ModernOption.TooltipFactory<T> tooltipFactory) {
            this.tooltipFactory = tooltipFactory;
            return this;
        }

        public Builder<T> initially(T value) {
            this.value = value;
            int i = this.values.getDefaults().indexOf(value);
            if (i != -1) {
                this.initialIndex = i;
            }

            return this;
        }

        public Builder<T> narration(Function<CyclingButtonWidget<T>, MutableComponent> narrationMessageFactory) {
            this.narrationMessageFactory = narrationMessageFactory;
            return this;
        }

        public Builder<T> omitKeyText() {
            this.optionTextOmitted = true;
            return this;
        }

        public CyclingButtonWidget<T> build(int x, int y, int width, int height, Component optionText) {
            return this.build(x, y, width, height, optionText, (button, value) -> {
            });
        }

        public CyclingButtonWidget<T> build(int x, int y, int width, int height, Component optionText, UpdateCallback<T> callback) {
            List<T> list = this.values.getDefaults();
            if (list.isEmpty()) {
                throw new IllegalStateException("No values for cycle button");
            } else {
                T object = this.value != null ? this.value : list.get(this.initialIndex);
                Component text = this.valueToText.apply(object);
                Component text2 = this.optionTextOmitted ? text : CommonComponents.optionNameValue(optionText, text);
                return new CyclingButtonWidget<>(x, y, width, height, optionText, this.initialIndex, object, this.values, this.valueToText, this.narrationMessageFactory, callback, this.tooltipFactory, this.optionTextOmitted);
            }
        }
    }

    private MutableComponent getMessageMutable() {
        return Component.empty().append(this.getMessage());
    }
}
