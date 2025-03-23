package team.teampotato.ruok.gui.metrics;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import team.teampotato.ruok.gui.metrics.ram.RamDrawer;

public class MetricsRender {
    private static final Minecraft mc = Minecraft.getInstance();
    public static void render(GuiGraphics context) {
        int scaledWidth = context.guiWidth();
        renderMem(context,scaledWidth);
    }
    private static void renderMem(GuiGraphics context,int scaledWidth) {
        RamDrawer.drawMetricsData(
                context,
                mc.getTimer(),
                scaledWidth - Math.min(scaledWidth / 2, 240),
                scaledWidth/2
        );
    }
    // 获取指标线颜色
    public static int getMetricsLineColor(int value, int yellowValue, int redValue) {
        return value < yellowValue ? interpolateColor(-16711936, -256, (float) value / (float) yellowValue) : interpolateColor(-256, -65536, (float) (value - yellowValue) / (float) (redValue - yellowValue));
    }
    // 颜色插值方法
    private static int interpolateColor(int color1, int color2, float dt) {
        int alpha1 = color1 >> 24 & 255;
        int red1 = color1 >> 16 & 255;
        int green1 = color1 >> 8 & 255;
        int blue1 = color1 & 255;
        int alpha2 = color2 >> 24 & 255;
        int red2 = color2 >> 16 & 255;
        int green2 = color2 >> 8 & 255;
        int blue2 = color2 & 255;
        int interpolatedAlpha = (int)Mth.clamp(Mth.lerp(dt, alpha1, alpha2), 0, 255);
        int interpolatedRed = (int)Mth.clamp(Mth.lerp(dt, red1, red2), 0, 255);
        int interpolatedGreen = (int) Mth.clamp(Mth.lerp(dt, green1, green2), 0, 255);
        int interpolatedBlue = (int)Mth.clamp(Mth.lerp(dt, blue1, blue2), 0, 255);
        return interpolatedAlpha << 24 | interpolatedRed << 16 | interpolatedGreen << 8 | interpolatedBlue;
    }
}
