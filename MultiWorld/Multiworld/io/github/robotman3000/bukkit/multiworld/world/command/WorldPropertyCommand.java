package io.github.robotman3000.bukkit.multiworld.world.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import io.github.robotman3000.bukkit.multiworld.world.WorldManagerHelper;
import io.github.robotman3000.bukkit.multiworld.world.WorldPropertyList;

public class WorldPropertyCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        WorldPropertyList prop;
        World world = null;
        String newValue = "imp0sibl3$tring";
        int inc = 0;

        if (args.length < 2) {
            // sender.sendMessage(ChatColor.RED + "You must provide a world property");
            sender.sendMessage(WorldManagerHelper.printEnum(WorldPropertyList.values()));
            return false;
        }

        world = Bukkit.getWorld(args[0]);
        if (world != null) {
            inc++;
        }

        if (WorldManagerHelper.isWorldProperty(args[0 + inc])) {
            prop = WorldPropertyList.valueOf(args[0 + inc]);
            if (args.length == 2 + inc) {
                // Now we know we are setting the property
                newValue = args[1 + inc]; // Zero index means that 3rd arg is index 2
            }

            if (world == null) {
                if (sender instanceof Player) {
                    world = ((Player) sender).getWorld();
                } else {
                    // sender.sendMessage(ChatColor.RED + "You must provide a world name");
                    return false;
                }
            }

            if (newValue.equals("imp0sibl3$tring")) {
                sender.sendMessage("Property " + prop + " has value "
                        + prop.getPropertyValue(world));
                return true;
            } else {
                boolean bool = prop.setPropertyValue(world, newValue);
                if (bool) {
                    sender.sendMessage("Property updated successfully");
                }
                return bool;
            }
        }
        sender.sendMessage(WorldManagerHelper.printEnum(WorldPropertyList.values()));
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias,
            String[] args) {
        ArrayList<String> list = new ArrayList<String>();
        switch(args.length){
        case 0:
            for (World world : Bukkit.getWorlds()) {
                list.add(world.getName());
            }
            break;
        case 1:
        	for(WorldPropertyList item : WorldPropertyList.values()){
        		list.add(item.name());
        	}
        	break;
        default:
            for (Player player : Bukkit.getOnlinePlayers()) {
                list.add(player.getName());
            }
        }
        return list;
    }

}