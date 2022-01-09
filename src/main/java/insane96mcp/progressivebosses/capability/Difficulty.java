package insane96mcp.progressivebosses.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class Difficulty {

	public static final Capability<IDifficulty> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {});

	public Difficulty() { }
}
