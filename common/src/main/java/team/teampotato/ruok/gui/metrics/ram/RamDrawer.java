package team.teampotato.ruok.gui.metrics.ram;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import team.teampotato.ruok.gui.metrics.MetricsRender;
import team.teampotato.ruok.util.OSysInfo;

public class RamDrawer {

    // 绘制指标数据的方法
    public static void drawMetricsData(GuiGraphics context, DeltaTracker metricsData, int x, int width) {
        // 获取数据起始索引、当前索引和采样数据

//        long[] samples = metricsData.get();
        long[] samples = new long[0];

        // 计算显示窗口的偏移和宽度
        int displayOffset = Math.max(0, samples.length - width);
        int displayWidth = samples.length - displayOffset;

        // 获取窗口高度
        int windowHeight = context.guiHeight();

        // 绘制背景框
        drawBackground(context, x, windowHeight, displayWidth);

        // 绘制指标线
        drawMetricsLines(context, x, windowHeight);

        // 绘制指标相关信息
        drawMetricInfo(context, x, windowHeight, displayWidth);

        // 绘制边框
        drawBorder(context, x, windowHeight, displayWidth);
    }

    private static void drawText(GuiGraphics context, int x, int windowHeight) {
        Font textRenderer = OSysInfo.getMinecraft.c().font;
        context.fill(RenderType.gui(), x + 1, windowHeight - 30 + 1, x + 14, windowHeight - 30 + 10, -1873784752);
        context.drawString(textRenderer, OSysInfo.getSystem.getMemoryUsagePercentage() + "% Usage Ram", x + 2, windowHeight - 30 + 2, 14737632, false);
    }

    // 绘制背景框
    private static void drawBackground(GuiGraphics context, int x, int windowHeight, int displayWidth) {
        // 只绘制下方的背景区域
        context.fill(RenderType.gui(), x, windowHeight - 30, x + displayWidth, windowHeight, -1873784752);
    }

    private static final int MAX_SAMPLES = 240; // 最大采样点数
    private static final int[] memoryUsageSamples = new int[MAX_SAMPLES]; // 用来保存历史采样数据
    private static int currentIndex = 0; // 当前采样索引

    // 绘制指标线
    private static void drawMetricsLines(GuiGraphics context, int x, int windowHeight) {
        int currentX = x; // x 起始位置

        // 获取当前内存使用百分比
        int currentMemoryUsage = OSysInfo.getSystem.getMemoryUsagePercentage();

        // 保存到采样数组中
        memoryUsageSamples[currentIndex] = currentMemoryUsage;

        // 计算当前样本的 scaledSample
        int scaledSample = scaleSample(currentMemoryUsage, 30);  // 转为合适的大小
        // 根据当前值来确定线条颜色
        int lineColor = MetricsRender.getMetricsLineColor(scaledSample, 30, 60);

        // 绘制当前指标线
        context.fill(RenderType.gui(), currentX, windowHeight - scaledSample, currentX + 1, windowHeight, lineColor);

        // 更新采样索引
        currentIndex = (currentIndex + 1) % MAX_SAMPLES; // 使索引循环

        // 绘制历史的采样点
        currentX++;
        for (int i = 1; i < MAX_SAMPLES; i++) {
            // 绘制TEXT
            drawText(context, x, windowHeight);

            int sampleIndex = (currentIndex + i) % MAX_SAMPLES; // 获取历史采样点
            int sampleValue = memoryUsageSamples[sampleIndex];
            scaledSample = scaleSample(sampleValue, 30);  // 转为合适的大小
            lineColor = MetricsRender.getMetricsLineColor(scaledSample, 30, 60);

            // 绘制历史采样指标线
            context.fill(RenderType.gui(), currentX, windowHeight - scaledSample, currentX + 1, windowHeight, lineColor);
            currentX++; // 更新到下一个采样点
        }
    }

    // scaleSample 方法
    public static int scaleSample(int sample, int destScale) {
        // 假设 sample 是百分比，直接根据比例缩放
        return (int) ((double) sample / 100 * destScale);
    }

    // 绘制不同指标的相关信息
    private static void drawMetricInfo(GuiGraphics context, int x, int windowHeight, int displayWidth) {
        context.hLine(RenderType.gui(), x, x + displayWidth - 1, windowHeight - 30, -1);
        // 不再绘制上方的线条
    }

    // 绘制边框
    private static void drawBorder(GuiGraphics context, int x, int windowHeight, int displayWidth) {
        context.hLine(RenderType.gui(), x, x + displayWidth - 1, windowHeight - 1, -1);
        context.vLine(RenderType.gui(), x, windowHeight - 30, windowHeight, -1);
        context.vLine(RenderType.gui(), x + displayWidth - 1, windowHeight - 30, windowHeight, -1);
    }
}
