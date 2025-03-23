package team.teampotato.ruok.util.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import team.teampotato.ruok.config.RuOK;

public class RenderUtil {
    private static boolean TickRun = true;

    public static boolean isTickRun() {
        return TickRun;
    }

    public static void setTickRun(boolean tickRun) {
        TickRun = tickRun;
    }

    public static void setMaxFps(int maxFps, Operation<Void> original) {
        if(RuOK.get().TickPerformance) {
            if(TickRun) {
                original.call(maxFps);
                TickRun = false;
            }
        } else original.call(maxFps);
    }
}
