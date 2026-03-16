package cn.bg7qvu.mtrwebctc.fabric;

import cn.bg7qvu.mtrwebctc.MTRWebCTCMod;
import dev.architectury.event.events.common.LifecycleEvent;
import net.fabricmc.api.ModInitializer;

public class MTRWebCTCFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        MTRWebCTCMod mod = new MTRWebCTCMod();
        mod.initialize();

        LifecycleEvent.SERVER_STARTING.register(mod::onServerStarting);
        LifecycleEvent.SERVER_STARTED.register(mod::onServerStarted);
        LifecycleEvent.SERVER_STOPPING.register(mod::onServerStopping);
    }
}
