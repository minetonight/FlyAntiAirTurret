package com.github.minetonight.flyantiairturret;

import java.util.Collection;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class FlyAntiAirTurret extends JavaPlugin implements Listener {

	private static final boolean isDebugging = false;

	@Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){

		int minHeight = 3;
		if(cmd.getName().equalsIgnoreCase("turret")){ // If the player typed /turret then do the following...
			if (args.length < 1) {
				minHeight = 3;
			}
			else {
				try {
					minHeight = new Integer(args[0]);
				} catch (NumberFormatException e) {
					getLogger().info(sender + " called us with " + args[0]);
				}
			}
			
			sender.sendMessage("[FlyAntiAirTurret] Activating turrets above level " + minHeight + "!");

			checkFliers(FlyAntiAirTurret.this, sender, minHeight);
			getLogger().info(sender.getName() + " performed command " + cmd.getName());
			
			return true;
		} 

		return false; 
	}

	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (isDebugging) {
			getLogger().info(event.getPlayer().getName() + " joined the server!");
		}
		
		final Player player = event.getPlayer();
		checkFliers(FlyAntiAirTurret.this, player, 3);
	}
	
	
	
	private void checkFliers(JavaPlugin plugin, final CommandSender sender, final int minHeight) {
		
		boolean isFlying = false;
		
		Collection<? extends Player> onlinePlayers = Bukkit.getServer().getOnlinePlayers();
		for(Player target : onlinePlayers) {
			isFlying = isFlying(target, minHeight);
			if (isFlying && 
					! isNextToBlock(target)) { // fix for builders of high platforms
				performFire(plugin, target, sender);
			}
			isFlying = false;
		}//for online players
	}//eof checkStuck
	
	
	private boolean isFlying(Player target, int minHeight) {
		World world = target.getWorld();
		Location location = target.getLocation();
		
		int playerY = location.getBlockY();
		
		int playerHeight = -2;
		while (world.getBlockAt(location).isEmpty()) {
			location.setY(playerY--);
			playerHeight++;
		}
		
		getLogger().info(target.getName() + " is at " + playerHeight + " blocks hight.");
		
		return playerHeight >= minHeight;
	}//eof isFlying
	
	final int directionsEight[][] = {
			{ 1, 0,  0}, // 12 o'clock
			{-1, 0,  0}, // 6
			{ 1, 0,  1}, // 1
			{-1, 0,  1}, // 4
			{ 1, 0, -1}, // 11
			{-1, 0, -1}, // 7
			{ 0, 0,  1}, // 3
			{ 0, 0, -1}, // 9
	};

	private boolean isNextToBlock(Player player) {
		
		Location location = player.getLocation();
		location.setY(location.getY() - 1);
		
		Block block = location.getBlock();
		Material type;

		boolean flag = false;
		
		//check block at location
		type = block.getType();
		if(isDebugging){
			getLogger().info("The location is in "+type.toString());
		}
		
		//check blocks around
		for (int[] dir : directionsEight) {
			Block relative = block.getRelative(dir[0], dir[1], dir[2]);

			if(isDebugging){
				System.out.println("location=" + location);
				getLogger().info("At " + relative.getLocation() + " there is "+ relative.getType());
			}

			type = relative.getType();
			if (type != Material.AIR) {
				flag = true;
			}
		}
		
		if(isDebugging){
			if (flag) {
				getLogger().info("It looks like you are next to a block.");
			}
		}
		return flag;
	}//eof isNextToPortal
	
	
	
	
	private void performFire(JavaPlugin plugin, final Player player, final CommandSender sender) {

		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {

				boolean success = true;
				
				
				if (success) {
					try {
						player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDERDRAGON_FIREBALL_EXPLODE, 1.5f, 1);
						Thread.sleep(500);
						player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDERDRAGON_FIREBALL_EXPLODE, 1.5f, 1);
						Thread.sleep(500);
						player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDERDRAGON_FIREBALL_EXPLODE, 1.5f, 1);
					} catch (InterruptedException e) {
						getLogger().log(Level.SEVERE, e.getMessage());
					}
					
					
					player.sendMessage("§4[FlyAntiAirTurret] has detected you were flying. Flying is *no more* without consequences in this server!");
					
					player.setHealth(1);
					player.setFlySpeed(0.0f);
					player.setAllowFlight(false);
					player.setFoodLevel(1);
					player.setVelocity(new Vector(0, -100, 0));
					player.setFallDistance(100);
					
					
					if (sender != null) {
						sender.sendMessage("§8[FlyAntiAirTurret] Thanks, you stopped "+player.getName());
						// player.setExp(arg0);
					}
					getLogger().info("Shot down player " + player.getName() + "!");
				} 
			}
		}, 20L);
	}//eof performUnstuck


}
