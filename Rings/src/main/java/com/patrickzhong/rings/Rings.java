package com.patrickzhong.rings;

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

public class Rings extends JavaPlugin implements Listener{
	Plugin plugin;
	List<LivingEntity> unholyFlame = new ArrayList<LivingEntity>();
	
	public void onEnable(){
		this.getServer().getPluginManager().registerEvents(this, this);
		plugin = this;
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent ev){
		if(ev.getAction() == Action.PHYSICAL || ev.getAction() == Action.LEFT_CLICK_AIR || ev.getAction() == Action.LEFT_CLICK_BLOCK){
			final Player player = ev.getPlayer();
		
			ItemStack item = player.getInventory().getItemInHand();
			if(item != null && item.getType().equals(Material.STICK) && item.hasItemMeta() && item.getItemMeta().hasDisplayName()){
				if(item.getItemMeta().getDisplayName().contains("Holy Flame")){
					ev.setCancelled(true);
					double time = 20.0;
					spawnRing(3.0, time, player, 50);
					PotionEffectType[] effects = {PotionEffectType.FIRE_RESISTANCE,
							PotionEffectType.REGENERATION};
					primeEffects(3.0, time, player, effects, 1);
				}
				else if(item.getItemMeta().getDisplayName().contains("Unholy Flame")){
					ev.setCancelled(true);
					double t = 15.0;
					final double[] time = {t};
					spawnRing(1.0, t, player, 20);
					
					new BukkitRunnable(){
						public void run(){
							Location loc = player.getLocation();
							for(final Entity ent : player.getWorld().getEntities()){
								if(ent instanceof LivingEntity && (ent instanceof Monster || ent instanceof Slime || ent instanceof Player) && ent.getLocation().distance(loc) <= 2){
									if(!ent.equals(player) && !unholyFlame.contains(((LivingEntity)ent))){
										((LivingEntity)ent).setFireTicks(200);
										unholyFlame.add(((LivingEntity)ent));
										
										new BukkitRunnable(){
											public void run(){
												unholyFlame.remove(((LivingEntity)ent));
												((Damageable)ent).damage(6);
											}
										}.runTaskLater(plugin, 200);
									}
								}
							}
							
							time[0] -= 0.5;
							if(time[0] <= 0)
								this.cancel();
						}
					}.runTaskTimer(plugin, 0, 10);
				}
				else if(item.getItemMeta().getDisplayName().contains("Infernal Frenzy")){
					ev.setCancelled(true);
					double time = 20.0;
					spawnRing(4.5, time, player, 120);
					PotionEffectType[] effects = {PotionEffectType.INCREASE_DAMAGE,
							PotionEffectType.FAST_DIGGING};
					primeEffects(4.5, time, player, effects, 0);
				}
			}
		}
		else if(ev.getAction() == Action.RIGHT_CLICK_AIR || ev.getAction() == Action.RIGHT_CLICK_BLOCK){
			if(ev.getPlayer().getItemInHand().getType() == Material.POTION){
				ev.setCancelled(true);
				ItemStack item = ev.getPlayer().getItemInHand();
				ev.getPlayer().getInventory().remove(item);
				ev.getPlayer().getInventory().addItem(item);
				ev.getPlayer().updateInventory();
			}
		}
		
	}
	
	private void spawnRing(final double radius, final double t, final Player player, final double numParts){
		final double[] time = {t};
		new BukkitRunnable(){
			public void run(){
				Location loc = player.getLocation();
				for(double i = 0; i < Math.PI*2; i+=Math.PI*2/numParts){
					float newX = (float)(xLoc(i, radius) + loc.getX());
					float newY = (float)(loc.getY());
					float newZ = (float)(zLoc(i, radius) + loc.getZ());
					
					PacketPlayOutWorldParticles packet= new PacketPlayOutWorldParticles(EnumParticle.FLAME, true, newX, newY, newZ, 0f, 0f, 0f, 0f, 1);
					PacketPlayOutWorldParticles packet2= new PacketPlayOutWorldParticles(EnumParticle.FLAME, true, newX, newY+0.5f, newZ, 0f, 0f, 0f, 0f, 1);
					PacketPlayOutWorldParticles packet3= new PacketPlayOutWorldParticles(EnumParticle.SMOKE_NORMAL, true, newX, newY+1, newZ, 0f, 0f, 0f, 0f, 1);
					for(Player p : loc.getWorld().getPlayers()){
						((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
						((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet2);
						((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet3);
					}
				}
				
				time[0] -= 0.5;
				if(time[0] <= 0)
					this.cancel();
			}
		}.runTaskTimer(plugin, 0, 10);
	}
	
	private void primeEffects(final double range, final double t, final Player player, final PotionEffectType[] effects, final int amplifier){
		final double[] time = {t};
		new BukkitRunnable(){
			public void run(){
				Location loc = player.getLocation();
				for(Entity ent : player.getWorld().getEntities()){
					if(ent instanceof LivingEntity && !(ent instanceof Monster) && !(ent instanceof Slime) && ent.getLocation().distance(loc) <= range){
						for(PotionEffectType effect : effects){
							if(!((LivingEntity)ent).hasPotionEffect(effect)){
								((LivingEntity)ent).addPotionEffect(new PotionEffect(effect, (int)Math.ceil(time[0])*20, amplifier));
							}
						}
					}
				}
				
				time[0] -= 0.1;
				if(time[0] <= 0)
					this.cancel();
			}
		}.runTaskTimer(plugin, 0, 2);
	}
			
	private double xLoc(double time, double radius){
		return Math.sin(time) * radius;
	}
	
	private double zLoc(double time, double radius){
		return Math.cos(time) * radius;
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent ev){
		Entity ent = ev.getEntity();
		if(ent instanceof LivingEntity && unholyFlame.contains(ent)){
			ev.setDamage(ev.getDamage()*2.0);
		}
	}
}
