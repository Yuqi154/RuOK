package team.teampotato.ruok.gui.modern.mode;


import net.minecraft.util.OptionEnum;

public enum WeatherType implements OptionEnum {
    CLOSE(0, "ruok.quality.close"),
    LOW(1, "ruok.quality.low"),
    NORMAL(2,"ruok.quality.normal");//Button 2 Name

    private final int id;
    private final String translationKey;

    WeatherType(int id, String translationKey) {
        this.id = id;
        this.translationKey = translationKey;
    }


    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getKey() {
        return this.translationKey;
    }
}
