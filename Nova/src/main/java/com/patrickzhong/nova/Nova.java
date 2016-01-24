package com.patrickzhong.nova;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
				}.runTaskTimer(plugin, 0, 2);
			}
		}
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
