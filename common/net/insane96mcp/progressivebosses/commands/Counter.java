package net.insane96mcp.progressivebosses.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.Sys;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.client.config.GuiConfigEntries.IntegerEntry;

public class Counter extends CommandBase {

	@Override
	public String getName() {
		return "pb:counter";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "commands.counter.usage";
		//"Sets or gets wither and dragon counter for player.\n/counter <player> get\n/counter <player> set <wither|dragon> <set_count>";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 2)
        {
            throw new WrongUsageException("commands.counter.usage", new Object[0]);
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
            			throw new WrongUsageException("commands.counter.usage", new Object[0]);
                	if (args[2].equals("wither")) {
                		tags.setInteger("progressivebosses:spawnedwithers", Integer.parseInt(args[3]));
                	}
                	else if (args[2].equals("dragon")) {
                		tags.setInteger("progressivebosses:killeddragons", Integer.parseInt(args[3]));
                	}
                	else {
                        throw new WrongUsageException("commands.counter.usage", new Object[0]);
                	}
            	}
            	else {
                    throw new WrongUsageException("commands.counter.usage", new Object[0]);	
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
