package team.teampotato.ruok.gui.modern.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class MessageWidget extends AbstractWidget {

    public MessageWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Override
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        Minecraft minecraftClient = Minecraft.getInstance();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        // 获取文本宽度
        int textWidth = minecraftClient.font.width(this.getMessage());
        int centerX = this.getX() + this.getWidth() / 2;
        int centerY = this.getY() + this.getHeight() / 2;

        // 计算文字位置
        int textX = this.getX() + (this.getWidth() - textWidth) / 2;
        int textY = centerY - minecraftClient.font.lineHeight / 2;

        // 设置文字颜色
        int textColor = this.active ? 0xFFFFFF : 0x808080;

        // 计算横线位置
        int linePadding = 5; // 文字与横线的间距
        int lineStartX = this.getX() + 5; // 左侧横线起点（留一定空隙）
        int lineEndX = this.getX() + this.getWidth() - 5; // 右侧横线终点
        int lineY = textY + minecraftClient.font.lineHeight / 2; // 横线Y坐标

        // 绘制横线
        int lineColor = 0xFFFFFFFF;
        context.fill(lineStartX, lineY, textX - linePadding, lineY + 1, lineColor); // 左横线
        context.fill(textX + textWidth + linePadding, lineY, lineEndX, lineY + 1, lineColor); // 右横线

        // 绘制居中文字
        drawCenteredText(context, this.getMessage(), textX, textY, 1.0F, minecraftClient, textColor);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false; // 禁止点击反馈，仅作为文字显示
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    private void drawCenteredText(GuiGraphics context, Component text, int x, int y, float scale, Minecraft mc, int color) {
        PoseStack matrixStack = context.pose();
        matrixStack.pushPose();
        matrixStack.scale(scale, scale, 1.0F);
        context.drawString(mc.font, text, x, y, color, false);
        matrixStack.popPose();
    }
}
