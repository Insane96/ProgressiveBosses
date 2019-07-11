package net.insane96mcp.progressivebosses.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Throwables;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class Counter extends CommandBase {

	@Override
	public String getName() {
		return "progressivebosses";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "commands.counter.usage.help";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 2)
        {
            throw new WrongUsageException("commands.counter.usage.wrong", new Object[0]);
        }
        else
        {
            EntityLivingBase living = getEntity(server, sender, args[0], EntityLivingBase.class);
            sender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, 0);
            if (living instanceof EntityPlayer) {
        		NBTTagCompound tags = living.getEntityData();
        		int witherCount, dragonCount;

            	if (args[1].equals("get")) {
            		String output = String.format("Spawned Withers: %d\nKilled Dragons: %d", tags.getInteger("progressivebosses:spawnedwithers"), tags.getInteger("progressivebosses:killeddragons"));
            		living.sendMessage(new TextComponentString(output));
            	}
            	else if (args[1].equals("set")) {
            		if (args.length < 4)
            			throw new WrongUsageException("commands.counter.usage.set", new Object[0]);
            		try {
            			int count = Integer.parseInt(args[3]);
            			if (args[2].equals("wither")) {
                    		tags.setInteger("progressivebosses:spawnedwithers", count);
                    		living.sendMessage(new TextComponentString("Set withers spawned for " + living.getName() + " to " + count));
                    	}
                    	else if (args[2].equals("dragon")) {
                    		tags.setInteger("progressivebosses:killeddragons", count);
                    		living.sendMessage(new TextComponentString("Set dragons killed for " + living.getName() + " to " + count));
                    	}
                    	else {
                            throw new WrongUsageException("commands.counter.usage.wrong", new Object[0]);
                    	}
            		}
            		catch (NumberFormatException e) {
            			throw new WrongUsageException("commands.counter.usage.number", new Object[0]);
            		}
                	
            	}
            	else {
                    throw new WrongUsageException("commands.counter.usage.wrong", new Object[0]);	
            	}
            }
        }
	}

	@Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }
        else if (args.length == 2)
        {
            return getListOfStringsMatchingLastWord(args, new String[] {"get", "set"});
        }
        else if (args.length == 3 && args[1].equals("set"))
        {
            return getListOfStringsMatchingLastWord(args, new String[] {"dragon", "wither"});
        }
        else {
			return Collections.emptyList();
		}
    }
}
