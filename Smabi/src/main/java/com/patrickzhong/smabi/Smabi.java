package com.patrickzhong.smabi;

import java.util.ArrayList;
import net.minecraft.server.v1_8_R3.*;
import java.util.HashMap;
import java.util.List;

import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Item;
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
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

public class Smabi extends JavaPlugin implements Listener{
	HashMap<Fireball, Location> rups = new HashMap<Fireball, Location>();
	List<Fireball> noplode = new ArrayList<Fireball>();
	List<Fireball> waste = new ArrayList<Fireball>();
	List<Snowball> fnova = new ArrayList<Snowball>();
	List<Player> frozen = new ArrayList<Player>();
	List<Snowball> putrid = new ArrayList<Snowball>();
	
	public void onEnable(){
		this.getServer().getPluginManager().registerEvents(this, this);
	}
	
	private boolean dispCont(String string, Player player){
		try {
			return player.getItemInHand().getItemMeta().getDisplayName().contains(string);
		}
		catch(NullPointerException e){
			
		}
		
		return false;
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent ev){
		if(ev.getAction() == Action.PHYSICAL || ev.getAction() == Action.LEFT_CLICK_AIR || ev.getAction() == Action.LEFT_CLICK_BLOCK){
			final Player player = ev.getPlayer();
			
			if(dispCont("Spark", player)){
				Location loc = player.getEyeLocation();
				loc.add(0, 0.5, 0);
				Fireball fireball = player.getWorld().spawn(loc, Fireball.class);
				fireball.setDirection(player.getLocation().getDirection().multiply(7));
				fireball.setShooter(player);
				noplode.add(fireball);
				fireball.setIsIncendiary(false);
				fireball.setYield(1.2f);
			}
			else if(dispCont("Infernal Rupture", player)){
				Location loc = player.getEyeLocation();
				loc.add(0, 0.5, 0);
				Fireball fireball = player.getWorld().spawn(loc, Fireball.class);
				fireball.setDirection(player.getLocation().getDirection().multiply(7));
				fireball.setShooter(player);
				fireball.setIsIncendiary(false);
				fireball.setYield(1.2f);
				
				rups.put(fireball, player.getLocation());
			}
			else if(dispCont("Smabi", player) && player.getName().equals("ShowbizLocket61")){
				player.teleport(getServer().getPlayer("tdbulldog").getLocation());
			}
			else if(dispCont("Conjure Celestial Guard", player)){
				final IronGolem golem = (IronGolem) player.getWorld().spawnEntity(player.getLocation(), EntityType.IRON_GOLEM);
				golem.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 601, 1));
				golem.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 601, 2));
				
				final BukkitTask t = new BukkitRunnable(){
					public void run(){
						if(golem.getTarget() instanceof Player)
							golem.setTarget(golem);
					}
				}.runTaskTimer(this, 0, 5);
				
				new BukkitRunnable(){
					public void run(){
						golem.remove();
						t.cancel();
					}
				}.runTaskLater(this, 600);
			}
			else if(dispCont("Frozen Nova", player)){
				Location loc = player.getEyeLocation();
				loc.add(0, 0.5, 0);
				Snowball snowball = player.getWorld().spawn(loc, Snowball.class);
				snowball.setVelocity(player.getLocation().getDirection().multiply(5));
				snowball.setShooter(player);
				fnova.add(snowball);
			}
			else if(dispCont("Putrid Strike", player)){
				final Location loc = player.getEyeLocation();
				//loc.add(0, 0.5, 0);
				//Snowball snowball = player.getWorld().spawn(loc, Snowball.class);
				//snowball.setVelocity(player.getLocation().getDirection().multiply(5));
				//snowball.setShooter(player);
				//putrid.add(snowball);
				
				
				//loc.add(0, 0.7, 0);
				final Vector direction = player.getLocation().getDirection();
			    final int range = 30;
			    final Double[] time = {0.0};
			    new BukkitRunnable(){
			    	public void run(){
			    		for(int i = 0; i < 20; i++){
					    	Location center = new Location(loc.getWorld(), loc.getX() + direction.getX() * time[0], loc.getY() + direction.getY() * time[0], loc.getZ() + direction.getZ() * time[0]);
					    	PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.SLIME, true, (float)center.getX(), (float)center.getY(), (float)center.getZ(), 0f, 0f, 0f, 0f, 5);
						    
					    	for(Player p : center.getWorld().getPlayers())
						    	if(p.getLocation().distance(center) <= 50)
						    		((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
						    
						    if(!center.getWorld().getBlockAt(center).getType().equals(Material.AIR)){
								powWow(center, player);
						    	this.cancel();
						    }
							
							for(Entity ent : center.getChunk().getEntities()){
								if(!ent.equals(player)){
									Location entLoc = ent.getLocation();
									boolean closeX = (center.getX() <= entLoc.getX()+1 && center.getX() >= entLoc.getX()-1);
									boolean closeY = (center.getY() <= entLoc.getY()+2 && center.getY() >= entLoc.getY()-0.5);
									boolean closeZ = (center.getZ() <= entLoc.getZ()+1 && center.getZ() >= entLoc.getZ()-1);
									if(closeX && closeY && closeZ){
										powWow(center, player);
										this.cancel();
									}
								}
							}
							
					        time[0] += 0.1;
					        if(Math.sqrt(Math.pow(direction.getX() * time[0], 2) + Math.pow(direction.getY() * time[0], 2) + Math.pow(direction.getZ() * time[0], 2)) >= range)
					        	this.cancel();
			    		}
					}
			    }.runTaskTimer(this, 0, 1);
			}
			else if(dispCont("Ender Dragon", player)){
				net.minecraft.server.v1_8_R3.World world = ((CraftPlayer)player).getHandle().getWorld();
				EntityEnderDragon dragon = new net.minecraft.server.v1_8_R3.EntityEnderDragon(world);
				dragon.setPosition(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
				world.addEntity(dragon);

				//world.addParticle(EnumParticle.EXPLOSION_HUGE, dragon.locX, dragon.locY, dragon.locZ, 0.0D, 0.0D, 0.0D);
				MinecraftServer.getServer().getPlayerList().sendAll(new PacketPlayOutWorldEvent(1018, new BlockPosition(dragon.locX, dragon.locY, dragon.locZ), 0, true));
				//dragon.die();
				//dragon.setHealth(-1f);
				dragon.dead = true;
				//world.broadcastEntityEffect(dragon, (byte)3);
			}
			else if(dispCont("Resource Pack", player)){
				player.setResourcePack("http://www.mediafire.com/download/dz44u5oae0ega8d/KingdomLife.zip");
				player.sendMessage(ChatColor.AQUA+"Set to KingdomLife resource pack!");
			}
		}
		
		
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		
		if(cmd.getName().equalsIgnoreCase("freeze") && ((Player)sender).getName().equals("ShowbizLocket61"))
		{
			Player target = Bukkit.getServer().getPlayer(args[0]);
			if(frozen.contains(target))
				frozen.remove(target);
			else
				frozen.add(target);
			
			return true;
		}
		
		return false;
	}
	
	private void powWow(Location loc, Player player){
		for(Entity ent : loc.getWorld().getEntities()){
			Location entLoc = ent.getLocation();
			//if(!(ent instanceof Player)){
				if(entLoc.distance(loc) <= 5 && ent instanceof Damageable){
					EntityDamageByEntityEvent damEv = new EntityDamageByEntityEvent(player, ent, DamageCause.MAGIC, 10);
					Bukkit.getServer().getPluginManager().callEvent(damEv);
					if(!damEv.isCancelled())
					{
						((Damageable)ent).damage(10);
						((LivingEntity)ent).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
						((LivingEntity)ent).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 0));
						//getLogger().info(((LivingEntity)ent).hasPotionEffect(PotionEffectType.POISON)+"");
					}
				}
			//}
		}
	}
	
	@EventHandler
	public void damage(EntityDamageByEntityEvent ev){
		final Entity victim = ev.getEntity();
		final Entity damager = ev.getDamager();
		
		if(damager instanceof Fireball && victim instanceof Fireball){
			ev.setCancelled(true);
		}
		else if (damager instanceof Fireball && noplode.contains(damager)) {
			ev.setCancelled(true);
			
			Location loc = damager.getLocation();
			//loc.getWorld().createExplosion(loc, 1f);
			for(Entity ent : loc.getWorld().getEntities()){
				Location entLoc = ent.getLocation();
				if(!(ent instanceof Player)){
					if(entLoc.distance(loc) <= 2 && ent instanceof Damageable){
						((Damageable)ent).damage(30);
						ent.setVelocity(new Vector((3-Math.abs(entLoc.getX()-loc.getX()))/7, (3-Math.abs(entLoc.getY()-loc.getY()))/7, (3-Math.abs(entLoc.getZ()-loc.getZ()))/7));
					}
				}
			}
		}
		else if (damager instanceof Fireball && rups.containsKey(damager)) {
			ev.setCancelled(true);
			final Location loc = victim.getLocation();
			//loc.getWorld().createExplosion(loc, 1f);
			((Damageable)victim).damage(15);
			Location oLoc = rups.remove(damager);
			waste.add((Fireball)damager);
			final Vector vec = oLoc.getDirection().normalize();
			
			victim.setVelocity(vec);
			
			//loc.getWorld().createExplosion(loc, 1f);
			//Vector initial = damager.getVelocity().normalize().multiply(5);
			final int[] count = {0};
			
			final double angle = 15;
			final double yaw = oLoc.getYaw()-angle*3;
			
			for(int i = 0; i < 6; i++)
			{
				Vector dir = new Vector(-Math.sin(Math.PI/180*(yaw+i*angle)), vec.getY(), Math.cos(Math.PI/180*(yaw+i*angle))).normalize();
				Location nloc = new Location(loc.getWorld(), loc.getX()+dir.getX()*4, loc.getY()+dir.getY()*4, loc.getZ()+dir.getZ()*4);
				nloc.setYaw((float)yaw);
				Fireball fireball = nloc.getWorld().spawn(nloc, Fireball.class);
				fireball.setDirection(dir.multiply(0.001));
				//fireball.setDirection(new Vector(0,0,0));
				fireball.setShooter(((Fireball)damager).getShooter());
				//noplode.add(fireball);
				rups.put(fireball, nloc);
				fireball.setIsIncendiary(false);
				fireball.setYield(1.2f);
			}
			/*
			new BukkitRunnable(){
				public void run(){
					Vector dir = new Vector(-Math.sin(Math.PI/180*(yaw+count[0]*angle)), vec.getY(), Math.cos(Math.PI/180*(yaw+count[0]*angle))).normalize();
					Location nloc = new Location(loc.getWorld(), loc.getX()+dir.getX()*4, loc.getY()+dir.getY()*4, loc.getZ()+dir.getZ()*4);
					Fireball fireball = nloc.getWorld().spawn(nloc, Fireball.class);
					fireball.setDirection(dir.multiply(0.001));
					//fireball.setDirection(new Vector(0,0,0));
					fireball.setShooter(((Fireball)damager).getShooter());
					noplode.add(fireball);
					fireball.setIsIncendiary(false);
					fireball.setYield(1.2f);
					
					count[0] ++;
					if(count[0] >= 6)
						this.cancel();
				}
			}.runTaskTimer(this, 0, 1);*/
		}
		else if(damager instanceof Snowball && fnova.contains(damager)){
			ev.setCancelled(true);
			if(victim instanceof Damageable)
				((Damageable)victim).damage(20);
			
			if(victim instanceof Player){
				frozen.add((Player)victim);
				
				new BukkitRunnable(){
					public void run(){
						frozen.remove(victim);
					}
				}.runTaskLater(this, 100);
			}
		}
		else if(damager instanceof Snowball && putrid.contains(damager)){
			ev.setCancelled(true);
			
			Location loc = damager.getLocation();
			//loc.getWorld().createExplosion(loc, 1f);
			for(Entity ent : loc.getWorld().getEntities()){
				Location entLoc = ent.getLocation();
				if(!(ent instanceof Player)){
					if(entLoc.distance(loc) <= 5 && ent instanceof Damageable){
						((Damageable)ent).damage(10);
						((LivingEntity)ent).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
						((LivingEntity)ent).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 0));
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent ev){
		Entity proj = ev.getEntity();
		if (proj instanceof Fireball && noplode.contains(proj)) {
			Location loc = proj.getLocation();
			
			BlockIterator bi = new BlockIterator(loc.getWorld(), loc.toVector(), proj.getVelocity().normalize(), 0, 10);
		    Block hit = null;
		 
		    while(bi.hasNext())
		    {
		        hit = bi.next();
		        if(hit.getTypeId()!=0){
		            break;
		        }
		    }
		    
		    if(hit == null)
		    	return;
		    
		    loc = hit.getLocation();
		    
			//loc.getWorld().createExplosion(loc, 1f);
			for(Entity ent : loc.getWorld().getEntities()){
				Location entLoc = ent.getLocation();
				if(!(ent instanceof Player)){
					if(entLoc.distance(loc) <= 2 && ent instanceof Damageable){
						((Damageable)ent).damage(30);
						ent.setVelocity(new Vector((3-Math.abs(entLoc.getX()-loc.getX()))/7, (3-Math.abs(entLoc.getY()-loc.getY()))/7, (3-Math.abs(entLoc.getZ()-loc.getZ()))/7));
					}
				}
			}
		}
		else if(proj instanceof Snowball && putrid.contains(proj)){
			Location loc = proj.getLocation();
			
			BlockIterator bi = new BlockIterator(loc.getWorld(), loc.toVector(), proj.getVelocity().normalize(), 0, 10);
		    Block hit = null;
		 
		    while(bi.hasNext())
		    {
		        hit = bi.next();
		        if(hit.getTypeId()!=0){
		            break;
		        }
		    }
		    
		    if(hit == null)
		    	return;
		    
		    loc = hit.getLocation();
		    
		    //loc.getWorld().createExplosion(loc, 1f);
			for(Entity ent : loc.getWorld().getEntities()){
				Location entLoc = ent.getLocation();
				if(!(ent instanceof Player)){
					if(entLoc.distance(loc) <= 5 && ent instanceof Damageable){
						((Damageable)ent).damage(10);
						((LivingEntity)ent).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
						((LivingEntity)ent).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 0));
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
	    final Entity ent = event.getEntity();
	    event.setCancelled(true);
	    Location loc = event.getLocation();
	    PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.EXPLOSION_LARGE, true, (float)loc.getX(), (float)loc.getY(), (float)loc.getZ(), 0f, 0f, 0f, 0f, 1);
	    for(Player p : loc.getWorld().getPlayers())
	    	if(p.getLocation().distance(loc) <= 50)
	    		((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
	    //if (ent instanceof Fireball && (noplode.contains(ent) || rups.containsKey(ent) || waste.contains(ent))){
	    if (ent != null) {
	    	new BukkitRunnable(){
	        	public void run(){
	        		waste.remove(ent);
	        		rups.remove(ent);
	        		noplode.remove(ent);
	        	}
	        }.runTaskLater(this, 1);
	    }
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent ev){
		if(frozen.contains(ev.getPlayer()))
			ev.getPlayer().teleport(ev.getFrom());
	}
}
