package insane96mcp.progressivebosses.proxy;

import net.minecraft.world.World;

public interface IProxy {

    void init();

    World getClientWorld();
}
