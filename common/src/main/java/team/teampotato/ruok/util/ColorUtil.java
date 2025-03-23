package team.teampotato.ruok.util;

public class ColorUtil {
    public static int getAUIColor(int color,double tColor) {

        // 提取 ARGB 分量
        int alpha = (color >> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        // 增加透明度的 30%（即减少透明度）
        int newAlpha = (int)(alpha * tColor);  // 增加透明度的 30%

        // 重新组合成新的颜色
        return (newAlpha << 24) | (red << 16) | (green << 8) | blue;
    }
}
