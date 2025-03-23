package team.teampotato.ruok.gui.modern.storage;

import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import team.teampotato.ruok.gui.base.Base;
import team.teampotato.ruok.gui.base.BaseManager;
import team.teampotato.ruok.gui.base.BaseUtil;
import team.teampotato.ruok.gui.modern.option.ModernOption;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ModernOptionsStorage {
    private static final List<ModernOption<?>> main = new ArrayList<>();
    private static final List<ModernOption<?>> other = new ArrayList<>();
    private static final List<ModernOption<?>> hud = new ArrayList<>();

    public static List<ModernOption<?>> getMainOptions() {
        if (main.isEmpty()) {
            populateOptions(main, BaseUtil.Group.MAIN);
            main.add(ModernEnumOptions.getWeatherModeSimpleOption());
            main.add(ModernEnumOptions.getQualityModeSimpleOption());
        }
        return main;
    }

    public static List<ModernOption<?>> getOtherOptions() {
        if (other.isEmpty()) {
            populateOptions(other, BaseUtil.Group.OTHER);
            other.add(ModernEnumOptions.getBlockBreakParticleTypeOption());
        }
        return other;
    }

    public static List<ModernOption<?>> getHudOptions() {
        if (hud.isEmpty()) {
            populateOptions(hud, BaseUtil.Group.HUD);
        }
        return hud;
    }
    private static void populateOptions(List<ModernOption<?>> targetList, BaseUtil.Group group) {
        // 筛选 BaseManager 中属于该分组的 Base
        List<Base<String, ?>> filteredBases = BaseManager.getBinds().stream()
                .filter(base -> base.getGroup() == group)
                .sorted(Comparator.comparingInt(Base::getRank)) // 按 Rank 排序
                .toList();

        // 遍历筛选结果，生成并添加对应的 SimpleOption
        for (Base<String, ?> base : filteredBases) {
            if (base.getType() == BaseUtil.Type.BOOLEAN) {
                Base<String, Boolean> boolBase = (Base<String, Boolean>) base;
                targetList.add(genButtonOption(boolBase.getKey(), boolBase.getFunction(), boolBase.getBiConsumer()));
            } else if (base.getType() == BaseUtil.Type.INT) {
                Base<String, Integer> intBase = (Base<String, Integer>) base;
                targetList.add(genIntegerOption(
                        intBase.getKey(),
                        intBase.getMin(),
                        intBase.getMax(),
                        intBase.getFunction(),
                        intBase.getBiConsumer(),
                        intBase.getFormat()
                ));
            }
        }
    }

    private static ModernOption<Integer> genIntegerOption(String key, int min, int max, Function<String, Integer> defaultValue, BiConsumer<String, Integer> changeCallback, String format) {
        return new ModernOption<>(
                key + ".info",
                value -> Tooltip.create(Component.translatable(key + ".tooltip")),
                (optionText, value) -> Options.genericValueLabel(optionText, Component.translatable(format, value)),
                new ModernOption.ValidatingIntSliderCallbacks(min, max),
                defaultValue.apply(String.valueOf(false)),
                intValue -> changeCallback.accept(String.valueOf(intValue), intValue)
        );
    }

    private static ModernOption<Boolean> genButtonOption(String key, Function<String, Boolean> defaultValue, BiConsumer<String, Boolean> changeCallback) {
        return ModernOption.ofBoolean(
                key + ".info",
                value -> Tooltip.create(Component.translatable(key + ".tooltip")),
                defaultValue.apply(String.valueOf(false)),
                bool -> changeCallback.accept(String.valueOf(bool), bool)
        );
    }



}