package team.teampotato.ruok.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import team.teampotato.ruok.RuOKMod;
import team.teampotato.ruok.gui.modern.GuiViewType;
import team.teampotato.ruok.gui.modern.mode.BlockBreakParticleType;
import team.teampotato.ruok.gui.modern.mode.QualityType;
import team.teampotato.ruok.gui.modern.mode.WeatherType;

import java.util.ArrayList;
import java.util.List;

@Config(name = RuOKMod.MOD_NAME)
public class RuOKConfig implements ConfigData {
    public boolean onCull = true;
    public int MaxEntityEntities = 128;
    public QualityType qualityModes = QualityType.NORMAL;
    public WeatherType RenderWeather = WeatherType.NORMAL;
    public boolean FastItemRender = false;
    public boolean RenderDisplayItem = false;
    public List<String> blackListedEntities = new ArrayList<>();
    public List<String> whiteListedEntities = new ArrayList<>();
    public boolean isAlwaysShowItemCount = true;
    public double startTime = 0;
    public boolean chatFix = false;
    public boolean onGui = false;
    public boolean GuiRXTX = false;
    public boolean GuiFPS = false;
    public boolean GuiCPU = false;
    public boolean GuiGPU = false;
    public boolean GuiRAM = false;
    public boolean GuiEntityCount = false;
    public boolean GuiPlayerPos = false;
    public boolean GuiServer = false;
    public boolean GuiCameraTarget = false;
    public boolean Mood = false;
    public boolean Particle = true;
    public BlockBreakParticleType BlockBreakParticleMode = BlockBreakParticleType.HIGH;
    public int MaxParticleDistance = 128;
    public List<String> WhiteListedParticle = new ArrayList<>();
    public List<String> BlackListedParticle = new ArrayList<>();
    public boolean FPSMonitor = true;
    public boolean GuiEasyRamMode = true;
    public boolean GuiDisplayRamUsage = false;
    public int GuiX = 0;
    public int GuiY = 0;
    public boolean EntityRender = true;
    public boolean UseAui = false;
    public boolean DeBug = false;
    public boolean TextBackground = false;
    public boolean TextureAnimatedSprites = true;
    public boolean SoundDevicesMonitor = false;
    public boolean SetParticleMaxAge = false;
    public int ParticleMaxAge = 1;
    public GuiViewType GuiViewMode = GuiViewType.NULL;
    public int EntityCullFov = 120;
    public int MinDistance = 5;
    public int EntityDistance = 64;
    public boolean TickPerformance = false;
}
