package net.insane96mcp.progressivebosses.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class SetCounter extends CommandBase {

	@Override
	public String getName() {
		return "counter";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "commands.counter.usage";
		//"Sets or gets wither and dragon counter for player.\n/counter <wither|dragon> <player> <get|set> [set_count]";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 3)
        {
            throw new WrongUsageException("commands.counter.usage", new Object[0]);
        }
        else
        {
            EntityLivingBase living = getEntity(server, sender, args[1], EntityLivingBase.class);
            sender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, 0);
            if (!(living instanceof EntityPlayer)) {
            	
            }
        }
	}

}
