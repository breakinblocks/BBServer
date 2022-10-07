package com.breakinblocks.bbserver.coremods;

import net.minecraft.server.network.ServerConnectionListener;

/**
 * Remove once <a href="https://github.com/MinecraftForge/MinecraftForge/pull/9072">MinecraftForge#9072</a>
 * is merged and then back-ported to 1.18.2.
 * <br/>
 * login_timeout.js
 */
@SuppressWarnings("unused")
public class LoginTimeoutCoremod {
    /**
     * @see ServerConnectionListener
     */
    private static final int READ_TIMEOUT = Integer.parseInt(System.getProperty("forge.readTimeout", "30"));
    private static final int READ_TIMEOUT_TICKS = READ_TIMEOUT * 20;

    public static int modifyLoginTimeout(int ticks) {
        return Math.max(READ_TIMEOUT_TICKS, ticks);
    }
}
