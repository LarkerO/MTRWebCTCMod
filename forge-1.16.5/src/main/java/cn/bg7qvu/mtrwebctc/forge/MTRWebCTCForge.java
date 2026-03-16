package cn.bg7qvu.mtrwebctc.forge;

import cn.bg7qvu.mtrwebctc.MTRWebCTCMod;
import dev.architectury.event.events.common.LifecycleEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("mtrwebctc")
public class MTRWebCTCForge {
    public MTRWebCTCForge() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        MTRWebCTCMod mod = new MTRWebCTCMod();
        mod.initialize();

        LifecycleEvent.SERVER_STARTING.register(mod::onServerStarting);
        LifecycleEvent.SERVER_STARTED.register(mod::onServerStarted);
        LifecycleEvent.SERVER_STOPPING.register(mod::onServerStopping);
    }
}
