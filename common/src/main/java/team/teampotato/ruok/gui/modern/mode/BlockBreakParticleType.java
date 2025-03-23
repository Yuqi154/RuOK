package team.teampotato.ruok.gui.modern.mode;

import net.minecraft.util.OptionEnum;

public enum BlockBreakParticleType implements OptionEnum {
    VERY_HIGH(5,5,5,0,"ruok.quality.ultra"),
    HIGH(4, 4, 4,1,"ruok.quality.high"),
    NORMAL(3, 3, 3,2,"ruok.quality.normal"),
    LOW(2, 2, 2,3,"ruok.quality.low"),
    VERY_LOW(2, 1, 2,4,"ruok.quality.critical");

    private final int id;
    private final String translationKey;
    private final int x;
    private final int y;
    private final int z;
    BlockBreakParticleType(int x, int y, int z,int id, String translationKey) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.id = id;
        this.translationKey = translationKey;
    }


    public int getX() {
        return x;
    }


    public int getY() {
        return y;
    }


    public int getZ() {
        return z;
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
