package cn.bg7qvu.mtrwebctc.forge;

import cn.bg7qvu.mtrwebctc.MTRWebCTCMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Forge 入口点
 */
@Mod("mtrwebctc")
public class MTRWebCTCForge {
    private final MTRWebCTCMod mod;
    
    public MTRWebCTCForge() {
        mod = new MTRWebCTCMod();
        
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            mod.initialize();
        });
    }
}
