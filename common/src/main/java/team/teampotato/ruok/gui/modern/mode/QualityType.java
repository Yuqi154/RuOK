package team.teampotato.ruok.gui.modern.mode;


import net.minecraft.util.OptionEnum;

public enum QualityType implements OptionEnum {
   // ULTRA(),HIGH,NORMAL,LOW,CRITICAL
    CRITICAL(0, "ruok.quality.close"),
    LOW(1, "ruok.quality.low"),
    NORMAL(2,"ruok.quality.normal"),//Button 2 Name
    HIGH(3,"ruok.quality.high"),
    ULTRA(4,"ruok.quality.ultra");

    private final int id;
    private final String translationKey;

    QualityType(int id, String translationKey) {
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
