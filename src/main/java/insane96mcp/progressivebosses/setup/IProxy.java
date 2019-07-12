package insane96mcp.progressivebosses.setup;

import net.minecraft.world.World;

public interface IProxy {

    void init();

    World getClientWorld();
}
