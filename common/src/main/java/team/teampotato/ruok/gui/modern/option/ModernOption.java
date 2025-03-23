package team.teampotato.ruok.gui.modern.option;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.OptionEnum;
import org.jetbrains.annotations.Nullable;
import team.teampotato.ruok.RuOKMod;
import team.teampotato.ruok.gui.modern.widget.CyclingButtonWidget;
import team.teampotato.ruok.gui.modern.widget.option.OptionSliderWidget;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.IntStream;

public final class ModernOption<T> {
    public static final PotentialValuesBasedCallbacks<Boolean> BOOLEAN = new PotentialValuesBasedCallbacks<>(ImmutableList.of(Boolean.TRUE, Boolean.FALSE));;
    public static final ValueTextGetter<Boolean> BOOLEAN_TEXT_GETTER = (optionText, value) -> value ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF;;
    private final TooltipFactory<T> tooltipFactory;
    final Function<T, Component> textGetter;
    private final Callbacks<T> callbacks;
    private final T defaultValue;
    private final Consumer<T> changeCallback;
    final Component text;
    T value;

    public static ModernOption<Boolean> ofBoolean(String key, boolean defaultValue, Consumer<Boolean> changeCallback) {
        return ofBoolean(key, emptyTooltip(), defaultValue, changeCallback);
    }

    public static ModernOption<Boolean> ofBoolean(String key, boolean defaultValue) {
        return ofBoolean(key, emptyTooltip(), defaultValue, (value) -> {
        });
    }

    public static ModernOption<Boolean> ofBoolean(String key, TooltipFactory<Boolean> tooltipFactory, boolean defaultValue) {
        return ofBoolean(key, tooltipFactory, defaultValue, (value) -> {
        });
    }

    public static ModernOption<Boolean> ofBoolean(String key, TooltipFactory<Boolean> tooltipFactory, boolean defaultValue, Consumer<Boolean> changeCallback) {
        return ofBoolean(key, tooltipFactory, BOOLEAN_TEXT_GETTER, defaultValue, changeCallback);
    }

    public static ModernOption<Boolean> ofBoolean(String key, TooltipFactory<Boolean> tooltipFactory, ValueTextGetter<Boolean> valueTextGetter, boolean defaultValue, Consumer<Boolean> changeCallback) {
        return new ModernOption<>(key, tooltipFactory, valueTextGetter, BOOLEAN, defaultValue, changeCallback);
    }

    public ModernOption(String key, TooltipFactory<T> tooltipFactory, ValueTextGetter<T> valueTextGetter, Callbacks<T> callbacks,T defaultValue, Consumer<T> changeCallback) {
        this.text = Component.translatable(key);
        this.tooltipFactory = tooltipFactory;
        this.textGetter = (value) -> valueTextGetter.toString(this.text, value);
        this.callbacks = callbacks;
        this.defaultValue = defaultValue;
        this.changeCallback = changeCallback;
        this.value = this.defaultValue;
    }

    public static <T> TooltipFactory<T> emptyTooltip() {
        return (value) -> null;
    }

    public static <T> TooltipFactory<T> constantTooltip(Component text) {
        return (value) -> Tooltip.create(text);
    }

    public static <T extends OptionEnum> ValueTextGetter<T> enumValueText() {
        return (optionText, value) -> value.getCaption();
    }

    public AbstractWidget createWidget(int x, int y, int width) {
        return this.createWidget(x, y, width, (value) -> {
        });
    }

    public AbstractWidget createWidget(int x, int y, int width, Consumer<T> changeCallback) {
        return this.callbacks.getWidgetCreator(this.tooltipFactory, x, y, width, changeCallback).apply(this);
    }

    public T getValue() {
        return this.value;
    }



    public String toString() {
        return this.text.getString();
    }

    public void setValue(T value) {
        T object = this.callbacks.validate(value).orElseGet(() -> {
            RuOKMod.LOGGER.error("Illegal option value {} for {}", value, this.text);
            return this.defaultValue;
        });
        if (!Minecraft.getInstance().isRunning()) {
            this.value = object;
        } else {
            if (!Objects.equals(this.value, object)) {
                this.value = object;
                this.changeCallback.accept(this.value);
            }

        }
    }

    public Callbacks<T> getCallbacks() {
        return this.callbacks;
    }


    @FunctionalInterface
    public interface TooltipFactory<T> {
        @Nullable
        Tooltip apply(T value);
    }

    public interface ValueTextGetter<T> {
        Component toString(Component optionText, T value);
    }

    public record PotentialValuesBasedCallbacks<T>(List<T> values) implements CyclingCallbacks<T> {

        public Optional<T> validate(T value) {
            return this.values.contains(value) ? Optional.of(value) : Optional.empty();
        }

        public CyclingButtonWidget.Values<T> getValues() {
            return CyclingButtonWidget.Values.of(this.values);
        }

        public List<T> values() {
            return this.values;
        }


    }

    public interface Callbacks<T> {
        Function<ModernOption<T>, AbstractWidget> getWidgetCreator(TooltipFactory<T> tooltipFactory, int x, int y, int width, Consumer<T> changeCallback);

        Optional<T> validate(T value);

    }

    public enum DoubleSliderCallbacks implements SliderCallbacks<Double> {
        INSTANCE;

        public Optional<Double> validate(Double double_) {
            return double_ >= 0.0 && double_ <= 1.0 ? Optional.of(double_) : Optional.empty();
        }

        public double toSliderProgress(Double double_) {
            return double_;
        }

        public Double toValue(double d) {
            return d;
        }
        public <R> SliderCallbacks<R> withModifier(final DoubleFunction<? extends R> sliderProgressValueToValue, final ToDoubleFunction<? super R> valueToSliderProgressValue) {
            return new SliderCallbacks<>() {
                public Optional<R> validate(R value) {
                    Optional<Double> var10000 = DoubleSliderCallbacks.this.validate(valueToSliderProgressValue.applyAsDouble(value));
                    Objects.requireNonNull(sliderProgressValueToValue);
                    return var10000.map(sliderProgressValueToValue::apply);
                }

                public double toSliderProgress(R value) {
                    return DoubleSliderCallbacks.this.toSliderProgress(valueToSliderProgressValue.applyAsDouble(value));
                }

                public R toValue(double sliderProgress) {
                    return sliderProgressValueToValue.apply(DoubleSliderCallbacks.this.toValue(sliderProgress));
                }
            };
        }
    }

    public record MaxSuppliableIntCallbacks(int minInclusive, IntSupplier maxSupplier, int encodableMaxInclusive) implements IntSliderCallbacks, TypeChangeableCallbacks<Integer> {

        public Optional<Integer> validate(Integer integer) {
            return Optional.of(Mth.clamp(integer, this.minInclusive(), this.maxInclusive()));
        }

        public int maxInclusive() {
            return this.maxSupplier.getAsInt();
        }


        public boolean isCycling() {
            return true;
        }

        public CyclingButtonWidget.Values<Integer> getValues() {
            return CyclingButtonWidget.Values.of(IntStream.range(this.minInclusive, this.maxInclusive() + 1).boxed().toList());
        }

        public int minInclusive() {
            return this.minInclusive;
        }

        public IntSupplier maxSupplier() {
            return this.maxSupplier;
        }

        public int encodableMaxInclusive() {
            return this.encodableMaxInclusive;
        }
    }

    public record ValidatingIntSliderCallbacks(int minInclusive, int maxInclusive) implements IntSliderCallbacks {


        public Optional<Integer> validate(Integer integer) {
            return integer.compareTo(this.minInclusive()) >= 0 && integer.compareTo(this.maxInclusive()) <= 0 ? Optional.of(integer) : Optional.empty();
        }

        public int minInclusive() {
            return this.minInclusive;
        }

        public int maxInclusive() {
            return this.maxInclusive;
        }
    }

    interface IntSliderCallbacks extends SliderCallbacks<Integer> {
        int minInclusive();

        int maxInclusive();

        default double toSliderProgress(Integer integer) {
            return Mth.map((float)integer, (float)this.minInclusive(), (float)this.maxInclusive(), 0.0F, 1.0F);
        }

        default Integer toValue(double d) {
            return Mth.floor(Mth.map(d, 0.0, 1.0, this.minInclusive(), this.maxInclusive()));
        }

        default <R> SliderCallbacks<R> withModifier(final IntFunction<? extends R> sliderProgressValueToValue, final ToIntFunction<? super R> valueToSliderProgressValue) {
            return new SliderCallbacks<>() {
                public Optional<R> validate(R value) {
                    Optional<Integer> var10000 = IntSliderCallbacks.this.validate(valueToSliderProgressValue.applyAsInt(value));
                    Objects.requireNonNull(sliderProgressValueToValue);
                    return var10000.map(sliderProgressValueToValue::apply);
                }

                public double toSliderProgress(R value) {
                    return IntSliderCallbacks.this.toSliderProgress(valueToSliderProgressValue.applyAsInt(value));
                }

                public R toValue(double sliderProgress) {
                    return sliderProgressValueToValue.apply(IntSliderCallbacks.this.toValue(sliderProgress));
                }

            };
        }
    }

    private static final class OptionSliderWidgetImpl<N> extends OptionSliderWidget {
        private final ModernOption<N> option;
        private final SliderCallbacks<N> callbacks;
        private final TooltipFactory<N> tooltipFactory;
        private final Consumer<N> changeCallback;

        OptionSliderWidgetImpl(int x, int y, int width, int height, ModernOption<N> option, SliderCallbacks<N> callbacks, TooltipFactory<N> tooltipFactory, Consumer<N> changeCallback) {
            super(x, y, width, height, callbacks.toSliderProgress(option.getValue()));
            this.option = option;
            this.callbacks = callbacks;
            this.tooltipFactory = tooltipFactory;
            this.changeCallback = changeCallback;
            this.updateMessage();
        }

        protected void updateMessage() {
            this.setMessage(this.option.textGetter.apply(this.option.getValue()));
            this.setTooltip(this.tooltipFactory.apply(this.callbacks.toValue(this.value)));
        }

        protected void applyValue() {
            this.option.setValue(this.callbacks.toValue(this.value));
            this.changeCallback.accept(this.option.getValue());
        }
    }

    public record LazyCyclingCallbacks<T>(Supplier<List<T>> values, Function<T, Optional<T>> validateValue) implements CyclingCallbacks<T> {

        public Optional<T> validate(T value) {
            return this.validateValue.apply(value);
        }

        public CyclingButtonWidget.Values<T> getValues() {
            return CyclingButtonWidget.Values.of(this.values.get());
        }

        public Supplier<List<T>> values() {
            return this.values;
        }

        public Function<T, Optional<T>> validateValue() {
            return this.validateValue;
        }

    }

    public record AlternateValuesSupportingCyclingCallbacks<T>(List<T> values, List<T> altValues, BooleanSupplier altCondition, ValueSetter<T> valueSetter) implements CyclingCallbacks<T> {

        public CyclingButtonWidget.Values<T> getValues() {
            return CyclingButtonWidget.Values.of(this.altCondition, this.values, this.altValues);
        }

        public Optional<T> validate(T value) {
            return (this.altCondition.getAsBoolean() ? this.altValues : this.values).contains(value) ? Optional.of(value) : Optional.empty();
        }

        public List<T> values() {
            return this.values;
        }

        public List<T> altValues() {
            return this.altValues;
        }

        public BooleanSupplier altCondition() {
            return this.altCondition;
        }

        public ValueSetter<T> valueSetter() {
            return this.valueSetter;
        }

    }

    interface TypeChangeableCallbacks<T> extends CyclingCallbacks<T>, SliderCallbacks<T> {
        boolean isCycling();

        default Function<ModernOption<T>, AbstractWidget> getWidgetCreator(TooltipFactory<T> tooltipFactory, int x, int y, int width, Consumer<T> changeCallback) {
            return this.isCycling() ? CyclingCallbacks.super.getWidgetCreator(tooltipFactory, x, y, width, changeCallback) : SliderCallbacks.super.getWidgetCreator(tooltipFactory, x, y, width, changeCallback);
        }
    }

    interface CyclingCallbacks<T> extends Callbacks<T> {
        CyclingButtonWidget.Values<T> getValues();

        default ValueSetter<T> valueSetter() {
            return ModernOption::setValue;
        }

        default Function<ModernOption<T>, AbstractWidget> getWidgetCreator(TooltipFactory<T> tooltipFactory, int x, int y, int width, Consumer<T> changeCallback) {
            return (option) -> CyclingButtonWidget.builder(option.textGetter).values(this.getValues()).tooltip(tooltipFactory).initially(option.value).build(x, y, width, 20, option.text, (button, value) -> {
                this.valueSetter().set(option, value);
                //gameOptions.write();
                changeCallback.accept(value);
            });
        }

        interface ValueSetter<T> {
            void set(ModernOption<T> option, T value);
        }
    }

    public interface SliderCallbacks<T> extends Callbacks<T> {
        double toSliderProgress(T value);

        T toValue(double sliderProgress);

        default Function<ModernOption<T>, AbstractWidget> getWidgetCreator(TooltipFactory<T> tooltipFactory, int x, int y, int width, Consumer<T> changeCallback) {
            return (option) -> new OptionSliderWidgetImpl<>(x, y, width, 20, option, this, tooltipFactory, changeCallback);
        }
    }
}
