package team.teampotato.ruok;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.teampotato.ruok.config.RuOKConfig;
import team.teampotato.ruok.gui.sodium.Options;
import team.teampotato.ruok.util.ModLoadState;

public class RuOKMod {
    public static final String MOD_ID = "ruokmod";
    public static final String MOD_NAME = "RuOK";
    public static final String MOD_IDE = "ruok";
    public static final String VERSION = "1.7.1";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static void init() {
        AutoConfig.register(RuOKConfig.class, Toml4jConfigSerializer::new);
        if(ModLoadState.isSodium()) {
            Options.initGroup();
        }

    }
}
