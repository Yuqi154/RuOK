package team.teampotato.ruok.gui.modern.storage;


import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import team.teampotato.ruok.config.RuOK;
import team.teampotato.ruok.gui.modern.mode.BlockBreakParticleType;
import team.teampotato.ruok.gui.modern.mode.QualityType;
import team.teampotato.ruok.gui.modern.mode.WeatherType;
import team.teampotato.ruok.gui.modern.option.ModernOption;
import team.teampotato.ruok.util.Quality;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static team.teampotato.ruok.gui.modern.mode.WeatherType.values;

public class ModernEnumOptions {
    @Contract(" -> new")
    public static @NotNull ModernOption<WeatherType> getWeatherModeSimpleOption() {
        return new ModernOption<>(
                "ruok.quality.weather.info",
                (value) -> Tooltip.create(Component.translatable("ruok.quality.weather.tooltip")),
                (optionText, value) -> Component.translatable(value.getKey()),
                new ModernOption.AlternateValuesSupportingCyclingCallbacks<>(
                        Arrays.asList(values()),
                        Stream.of(values()).collect(Collectors.toList()),
                        () -> true,
                        (option, mode) -> {
                            option.setValue(mode);
                            RuOK.get().RenderWeather = mode;
                            RuOK.save();
                        }
                ),
                RuOK.get().RenderWeather,
                (value) -> {

                }
        );
    }
    @Contract(" -> new")
    public static @NotNull ModernOption<QualityType> getQualityModeSimpleOption() {
        return new ModernOption<>(
                "ruok.quality.global.info",
                (value) -> Tooltip.create(Component.translatable("ruok.quality.global.tooltip")),
                (optionText, value) -> Component.translatable(value.getKey()),
                new ModernOption.AlternateValuesSupportingCyclingCallbacks<>(
                        Arrays.asList(QualityType.values()),
                        Stream.of(QualityType.values()).collect(Collectors.toList()),
                        () -> true,
                        (option, mode) -> {
                            Quality.set(mode);
                            RuOK.get().qualityModes = mode;
                            RuOK.save();
                        }
                ),
                RuOK.get().qualityModes,
                (value) -> {

                }
        );
    }
    @Contract(" -> new")
    public static @NotNull ModernOption<BlockBreakParticleType> getBlockBreakParticleTypeOption() {
        return new ModernOption<>(
                "ruok.quality.particle.info",
                (value) -> Tooltip.create(Component.translatable("ruok.quality.particle.tooltip")),
                (optionText, value) -> Component.translatable(value.getKey()),
                new ModernOption.AlternateValuesSupportingCyclingCallbacks<>(
                        Arrays.asList(BlockBreakParticleType.values()),
                        Stream.of(BlockBreakParticleType.values()).collect(Collectors.toList()),
                        () -> true,
                        (option, mode) -> {
                            RuOK.get().BlockBreakParticleMode = mode;
                            RuOK.save();
                        }
                ),
                RuOK.get().BlockBreakParticleMode,
                (value) -> {

                }
        );

    }

}
