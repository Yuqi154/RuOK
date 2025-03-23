package team.teampotato.ruok.gui.modern.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ButtonWidget extends AbstractButton {
    protected static final NarrationSupplier DEFAULT_NARRATION_SUPPLIER = Supplier::get;
    protected final PressAction onPress;
    protected final NarrationSupplier narrationSupplier;

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull Builder builder(Component message, PressAction onPress) {
        return new Builder(message, onPress);
    }

    public ButtonWidget(int x, int y, int width, int height, Component message, PressAction onPress, NarrationSupplier narrationSupplier) {
        super(x, y, width, height, message);
        this.onPress = onPress;
        this.narrationSupplier = narrationSupplier;
    }

    public void onPress() {
        this.onPress.onPress(this);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    public static class Builder {
        private final Component message;
        private final PressAction onPress;
        @Nullable
        private Tooltip tooltip;
        private int x;
        private int y;
        private int width = 150;
        private int height = 15;
        private NarrationSupplier narrationSupplier;

        public Builder(Component message, PressAction onPress) {
            this.narrationSupplier = DEFAULT_NARRATION_SUPPLIER;
            this.message = message;
            this.onPress = onPress;
        }

        public Builder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder dimensions(int x, int y, int width, int height) {
            return this.position(x, y).size(width, height);
        }

        public Builder tooltip(@Nullable Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public Builder narrationSupplier(NarrationSupplier narrationSupplier) {
            this.narrationSupplier = narrationSupplier;
            return this;
        }

        public ButtonWidget build() {
            ButtonWidget buttonWidget = new ButtonWidget(this.x, this.y, this.width, this.height, this.message, this.onPress, this.narrationSupplier);
            buttonWidget.setTooltip(this.tooltip);
            return buttonWidget;
        }
    }

    public interface PressAction {
        void onPress(ButtonWidget button);
    }
    public interface NarrationSupplier {
        MutableComponent createNarrationMessage(Supplier<MutableComponent> textSupplier);
    }
    private int lineEndX = 0;  // 记录横线的结束位置，初始为 0，类型改为 int

    @Override
    protected void renderWidget(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        Minecraft minecraftClient = Minecraft.getInstance();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        int alphaValue = Mth.ceil(this.alpha * 255.0F);  // 获取动态透明度

        // 绘制填充矩形按钮背景
        if(this.isHovered()) {
            context.fill(
                    this.getX(),
                    this.getY(),
                    this.getX() + this.getWidth(),
                    this.getY() + this.getHeight(),
                    (alphaValue << 24) | 0xBBBBBB
            );


        }

        // 动态控制横线的宽度
        int speed = 30;
        if (this.isHovered()) {
            // 如果按钮被选中，横线从左到右逐渐绘制
            lineEndX = Math.min(this.getX() + this.getWidth(), lineEndX + (int)(delta * speed)); // delta 控制速度
        } else {
            // 如果按钮未被选中，横线逐渐从右到左消失
            lineEndX = Math.max(this.getX(), lineEndX - (int)(delta * speed));  // delta 控制速度，强制转换为 int
        }

        if(this.isHovered()) {
            // 绘制横线
            context.hLine(
                    RenderType.gui(),
                    this.getX(),
                    lineEndX - 1, // 横线的结束位置（动态更新）
                    this.getY() + this.getHeight(), // 绘制横线的垂直位置
                    -1 // 横线的颜色
            );
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F); // 恢复默认颜色
        int textColor = this.active ? 0xFFFFFF : 0x808080;  // 根据按钮状态设置文字颜色，选择时白色，未选择时灰色
        //TODO DrawMessage
        this.renderString(context, minecraftClient.font, textColor); // 绘制文字
    }



}
