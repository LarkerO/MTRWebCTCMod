package cn.bg7qvu.mtrwebctc.fabric;

import cn.bg7qvu.mtrwebctc.MTRWebCTCMod;
import net.fabricmc.api.ModInitializer;

/**
 * Fabric 入口点
 */
public class MTRWebCTCFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        MTRWebCTCMod mod = new MTRWebCTCMod();
        mod.initialize();
    }
}
