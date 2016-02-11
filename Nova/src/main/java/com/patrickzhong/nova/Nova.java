package com.patrickzhong.nova;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class Nova extends JavaPlugin implements Listener{
	Plugin plugin;
	
	public void onEnable(){
		this.getServer().getPluginManager().registerEvents(this, this);
		plugin = this;
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent ev){
		if(ev.getAction() == Action.PHYSICAL || ev.getAction() == Action.LEFT_CLICK_AIR || ev.getAction() == Action.LEFT_CLICK_BLOCK){
			final Player player = ev.getPlayer();
		
			ItemStack item = player.getInventory().getItemInHand();
			if(item != null && item.getType().equals(Material.STICK) && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().contains("Nova Of The Dragon")){
				ev.setCancelled(true);
					
				final int[] count = {25};
				
				new BukkitRunnable(){
					public void run(){
						Location loc = player.getEyeLocation();
						loc.add(0, 0.5, 0);
						Fireball fireball = player.getWorld().spawn(loc, Fireball.class);
						fireball.setVelocity(player.getLocation().getDirection().multiply(5));
						fireball.setShooter(player);
						fireball.setMetadata("noplode", new FixedMetadataValue(plugin, true));
						fireball.setIsIncendiary(false);
						fireball.setYield(1.2f);
						
						count[0] --;
						if(count[0] <= 0)
							this.cancel();
					}
				}.runTaskTimer(plugin, 0, 4);
			}
			else if(item != null && item.getType().equals(Material.STICK) && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().contains("Seismic Blast")){
				ev.setCancelled(true);
					
				final int[] count = {3};
				final double range = 20;
				
				new BukkitRunnable(){
					public void run(){
						final double[] time = {0.0};
						final Location loc = player.getEyeLocation();
						final double yaw = loc.getYaw();
						final Vector dir = loc.getDirection().normalize();
						loc.add(0, -0.5, 0);
						
						new BukkitRunnable(){
							public void run(){
								final Location center = new Location(loc.getWorld(), loc.getX()+dir.getX()*time[0], loc.getY()+dir.getY()*time[0], loc.getZ()+dir.getZ()*time[0]);
								
								generateLine(center, yaw, time[0], player, dir);
								
								time[0] += 1;
								if(time[0] >= range)
									this.cancel();
							}
						}.runTaskTimer(plugin, 0, 1);
						
						count[0] --;
						if(count[0] <= 0)
							this.cancel();
					}
				}.runTaskTimer(plugin, 0, 30);
			}
		}
	}
	
	private void generateLine(Location center, double yaw, double time, Player player, Vector dir){
		int numParticles = 40;
		double length = time * 2;
		double dx = 2 * Math.sin(Math.PI/180 * (90 - yaw)) * time;
		double dz = 2 * Math.cos(Math.PI/180 * (90 - yaw)) * time;
		for(double i = 0; i < numParticles; i++){
			double x = center.getX() - dx/2 + dx*i/(numParticles-1);
			double z = center.getZ() - dz/2 + dz*i/(numParticles-1);
			
			PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.SLIME, true, (float) x, (float) (center.getY()), (float) z, 0f, 0f, 0f, 0f, 1);
	        for(Player online : center.getWorld().getPlayers()) {
	            ((CraftPlayer)online).getHandle().playerConnection.sendPacket(packet);
	        }
	        
	        Location at = new Location(center.getWorld(), x, center.getY(), z);
	        
	        for(Entity ent : at.getChunk().getEntities()){
				if(!ent.equals(player) && ent instanceof LivingEntity){
					if(isColliding(at, ent)){
						ent.setVelocity(new Vector(dir.getX()*2, dir.getY()*3/2, dir.getZ()*2));
						((Damageable)ent).damage(4);
					}
				}
			}
		}
	}
	
	private boolean isColliding(Location loc, Entity ent){
		Location entLoc = ent.getLocation();
		boolean x = Math.abs(loc.getX()-entLoc.getX()) <= ((CraftLivingEntity)ent).getHandle().width/2;
		boolean y = loc.getY() >= entLoc.getY() && loc.getY()-entLoc.getY() <= ((LivingEntity)ent).getEyeHeight();
		boolean z = Math.abs(loc.getZ()-entLoc.getZ()) <= ((CraftLivingEntity)ent).getHandle().width/2;
		
		return x && y && z;
	}
	
	@EventHandler
	public void damage(EntityDamageByEntityEvent ev){
		Entity victim = ev.getEntity();
		Entity damager = ev.getDamager();
		
		if (damager instanceof Fireball && damager.hasMetadata("noplode")) {
			ev.setCancelled(true);
			
			Location loc = damager.getLocation();
			
			for(Entity ent : loc.getWorld().getEntities()){
				Location entLoc = ent.getLocation();
				if(!(ent instanceof Player)){
					if(entLoc.distance(loc) <= 4 && ent instanceof Damageable){
						((Damageable)ent).damage(15);
						ent.setVelocity(new Vector((4-Math.abs(entLoc.getX()-loc.getX()))/7, (4-Math.abs(entLoc.getY()-loc.getY()))/7, (4-Math.abs(entLoc.getZ()-loc.getZ()))/7));
					}
				}
			}
		}
		
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent ev){
		Entity proj = ev.getEntity();
		if (proj instanceof Fireball && proj.hasMetadata("noplode")) {
			Location loc = proj.getLocation();
			
			for(Entity ent : loc.getWorld().getEntities()){
				Location entLoc = ent.getLocation();
				if(!(ent instanceof Player)){
					if(entLoc.distance(loc) <= 4 && ent instanceof Damageable){
						((Damageable)ent).damage(15);
						ent.setVelocity(new Vector((4-Math.abs(entLoc.getX()-loc.getX()))/7, (4-Math.abs(entLoc.getY()-loc.getY()))/7, (4-Math.abs(entLoc.getZ()-loc.getZ()))/7));
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
	    Entity ent = event.getEntity();
	   
	    if (ent instanceof Fireball && ent.hasMetadata("noplode")) {
	        event.setCancelled(true); //Removes block damage
	    }
	}
}
