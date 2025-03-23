package team.teampotato.ruok.gui.modern.widget.option;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.Util;
import net.minecraft.client.InputType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import team.teampotato.ruok.util.ColorUtil;

public abstract class OptionSliderWidget extends AbstractSliderButton {
    public boolean sliderFocused;
    protected OptionSliderWidget(int x, int y, int width, int height, double value) {
        super(x, y, width, height, CommonComponents.EMPTY, value);
    }

    public boolean isSliderFocused() {
        return sliderFocused;
    }

    @Override
    protected MutableComponent createNarrationMessage() {
        return super.createNarrationMessage();
    }

    public void onRelease(double mouseX, double mouseY) {
        if (isMouseOverSlider(mouseX, mouseY)) { // ✅ 只允许在滑块上释放
            super.playDownSound(Minecraft.getInstance().getSoundManager());
        }
    }

    private boolean isMouseOverSlider(double mouseX, double mouseY) {
        int baseY = this.getY() + this.getHeight() / 2;
        int sliderMinX = this.getX() + (this.getWidth() * 3 / 4) - 8 + 2; // ✅ 滑块左边界
        int sliderMaxX = sliderMinX + (this.getWidth() / 4 - 10); // ✅ 滑块右边界

        return mouseX >= sliderMinX && mouseX <= sliderMaxX && mouseY >= baseY - 3 && mouseY <= baseY + 3;
    }


    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) {
            this.sliderFocused = false;
        } else {
            InputType guiNavigationType = Minecraft.getInstance().getLastInputType();
            if (guiNavigationType == InputType.MOUSE || guiNavigationType == InputType.KEYBOARD_TAB) {
                this.sliderFocused = true;
            }

        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.setValueFromMouse(mouseX);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (CommonInputs.selected(keyCode)) {
            this.sliderFocused = !this.sliderFocused;
            return true;
        } else if (this.sliderFocused) {
            int left = InputConstants.KEY_LEFT;//263
            int right = InputConstants.KEY_RIGHT;//262
            boolean bl = keyCode == left; // 左箭头
            boolean br = keyCode == right; // 右箭头
            if (bl || br) {
                float f = bl ? -1.0F : 1.0F;

                // ✅ 计算滑块的真实可用宽度
                int sliderMinX = this.getX() + (this.getWidth() * 3 / 4) - 8 + 2;
                int sliderMaxX = sliderMinX + (this.getWidth() / 4 - 10);
                int sliderWidth = sliderMaxX - sliderMinX;

                // ✅ 改为按照滑块区域计算增量
                this.setValue(this.value + (f / (float) sliderWidth));
                return true;
            }
        }

        return false;
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (isMouseOverSlider(mouseX, mouseY)) { // ✅ 只允许在滑块上拖动
            this.setValueFromMouse(mouseX);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOverSlider(mouseX, mouseY)) { // ✅ 只允许在滑块上点击
            this.setValueFromMouse(mouseX);
            return true;
        }
        return false;
    }

    public void setValue(double value) {
        double d = this.value;
        this.value = Mth.clamp(value, 0.0, 1.0);
        if (d != this.value) {
            this.applyValue();
        }

        this.updateMessage();
    }
    public void setValueFromMouse(double mouseX) {
        int sliderMinX = this.getX() + (this.getWidth() * 3 / 4) - 8 + 2; // ✅ 滑块左边界
        int sliderMaxX = sliderMinX + (this.getWidth() / 4 - 10); // ✅ 滑块右边界
        int sliderWidth = sliderMaxX - sliderMinX; // ✅ 滑块实际可用宽度

        this.setValue((mouseX - sliderMinX) / (double) sliderWidth); // ✅ 计算相对滑块的值
    }
    @Override
    public void renderWidget(GuiGraphics context, int i, int j, float f) {
        int alphaValue = Mth.ceil(this.alpha * 255.0F);
        Minecraft mc = Minecraft.getInstance();

        int baseY = 3; // 方框边界厚度
        int endY = this.getY() + this.getHeight();
        int endX = this.getX() + this.getWidth();

        int le = 4;
        int boxWidth = (endX - this.getX()) / 4 - le; // ✅ 更窄，并且整体左移 8 格
        int bY = this.getY() + this.getHeight() / 2; // ✅ Y 轴中线

        // ✅ 1. 先绘制背景（确保不会遮挡滑块）
        this.renderBackground(context);

        // ✅ 2. **边框整体左移 3px**
        int bX = this.getX() + (this.getWidth() * 3 / 4) - le - 3; // ✅ **左移 3px**
        int boxRight = bX + boxWidth; // ✅ 计算方框右边界

        // ✅ 3. 渲染边框（修正超出问题）
        context.fill(bX + 1 - 3, bY + baseY, boxRight - 1 -3, bY + baseY + 1, 0xFF55AA55); // 下边线
        context.fill(bX + 1 - 3, bY - baseY, boxRight - 1 - 3, bY - baseY - 1, 0xFF55AA55); // 上边线
        context.fill(bX - 3, bY - baseY, bX + 1 - 3, bY + baseY, 0xFF55AA55); // **左边框**
        context.fill(boxRight - 3, bY - baseY, boxRight - 1 - 3, bY + baseY, 0xFF55AA55); // 右竖线


        // ✅ 4. 计算滑块位置（修正偏移问题）
        int sliderWidth = boxWidth - 10; // ✅ 限制滑块范围（左右各少 1 格）
        int sliderMinX = bX + 2 - 3; // ✅ **左移 3px**
        int sliderMaxX = boxRight - 8 - 3; // ✅ **左移 3px**
        int sliderX = sliderMinX + (int) (this.value * sliderWidth); // ✅ 计算滑块位置
        sliderX = Mth.clamp(sliderX, sliderMinX, sliderMaxX); // ✅ 限制滑块范围

        int sliderColor = this.isHovered() ? 0xFFAAFFAA : 0xFF88FF88; // 滑块颜色

        // ✅ 5. **最后绘制滑块**（不会被背景遮挡）
        context.fill(sliderX, bY - baseY + 1, sliderX + 6, bY + baseY - 1, sliderColor);

        // ✅ 6. 绘制文本（居中）
        int textColor = this.active ? 0xFFFFFF : 0xA0A0A0;
        this.renderScrollingString(context, mc.font, 2, textColor | (alphaValue << 24));
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


    public void renderBackground(GuiGraphics context) {
        int color = this.isHoveredOrFocused() ? ColorUtil.getAUIColor(-804253680, 0.8) : ColorUtil.getAUIColor(-804253680, 0.5);
        // ✅ 绘制填充矩形按钮背景（不会覆盖滑块）
        context.fill(
                this.getX(),
                this.getY(),
                this.getX() + this.getWidth(),
                this.getY() + this.getHeight(),
                color
        );
    }

}
