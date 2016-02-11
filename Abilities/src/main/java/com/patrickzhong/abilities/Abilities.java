package com.patrickzhong.abilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import net.minecraft.server.v1_8_R3.PlayerConnection;

import com.patrickzhong.kingdomlifeapi.KingdomLifeAPI;

public class Abilities extends JavaPlugin implements Listener{
	HashMap chickens = new HashMap();
	//HashMap particleTimers = new HashMap();
	HashMap<Player, String> clicks = new HashMap<Player, String>();
	List<BukkitTask> timers = new ArrayList<BukkitTask>();
	List<Silverfish> friendlyFish = new ArrayList<Silverfish>();
	List<Egg> eggs = new ArrayList<Egg>();
	Plugin plugin;
	KingdomLifeAPI kLifeAPI;
	
	public void onEnable(){
		plugin = this;
		this.getServer().getPluginManager().registerEvents(this, this);
		kLifeAPI = (KingdomLifeAPI) this.getServer().getPluginManager().getPlugin("KingdomLifeAPI");
		getLogger().info("Abilities enabled successfully.");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equals("totem")){
			int amount = 1;
			if(args.length > 0)
				amount = Integer.parseInt(args[0]);
			
			if(!((Player)sender).getName().equals("ShowbizLocket61") && amount > 10){
				((Player)sender).sendMessage(ChatColor.GRAY+"Spawned the maximum number of 10 totems.");
				amount = 10;
			}
			
			for(int i = 0; i < amount; i++){
				spawnChickens((Player)sender);
			}
			
			return true;
		}
		return false;
	}
	
	@EventHandler
	public void onClick(PlayerInteractEvent ev){
		final Player player = ev.getPlayer();
		
		ItemStack item = player.getInventory().getItemInHand();
		//if(item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().contains("Conjure Chicken Totem")){
		if(item != null && item.getType().equals(Material.STICK) && item.hasItemMeta() && item.getItemMeta().hasDisplayName()){
			ItemStack hand = player.getInventory().getItemInHand();
			String type = kLifeAPI.type(player.getUniqueId().toString());
			if(hand == null || !type.split("-")[1].equals("mage") || !hand.getType().equals(Material.STICK) || !hand.hasItemMeta() || !hand.getItemMeta().hasLore())
				return;
			List<String> lores = hand.getItemMeta().getLore();
			
			for(int a = 0; a < lores.size(); a++){
				String loreLine = ChatColor.stripColor(lores.get(a));
				if(loreLine.contains("Min. Level")){
					int minLevel = Integer.parseInt(loreLine.substring(loreLine.indexOf(":")+2));
					if(kLifeAPI.level(player.getUniqueId().toString(), type) >= minLevel){
						ev.setCancelled(true);
						
						if(ev.getAction().equals(Action.LEFT_CLICK_AIR) || ev.getAction().equals(Action.LEFT_CLICK_BLOCK)){
							final int index;
							if(clicks.containsKey(player)){
								String str = clicks.get(player);
								clicks.put(player, str+"0");
								index = str.length();
							}
							else {
								clicks.put(player, "  0");
								index = 2;
							}
							
							timers.add(new BukkitRunnable(){
								public void run(){
									String str = clicks.get(player);
									str = str.substring(0, index)+" "+str.substring(index+1);
									clicks.put(player, str);
								}
							}.runTaskLater(plugin, 40));
						}
						else if(ev.getAction().equals(Action.RIGHT_CLICK_AIR) || ev.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
							final int index;
							
							if(clicks.containsKey(player)){
								String str = clicks.get(player);
								clicks.put(player, str+"1");
								index = str.length();
							} else {
								clicks.put(player, "  1");
								index = 2;
							}
							
							timers.add(new BukkitRunnable(){
								public void run(){
									String str = clicks.get(player);
									str = str.substring(0, index)+" "+str.substring(index+1);
									clicks.put(player, str);
								}
							}.runTaskLater(plugin, 40));
						}
						String comboStr = clicks.get(player);
						if(comboStr.substring(comboStr.length()-3).equals("010")){
							for(BukkitTask t : timers){
								t.cancel();
							}
							timers = new ArrayList<BukkitTask>();
							clicks.remove(player);
							
							spawnChickens(player);
							
							ev.setCancelled(true);
						}
						
						return;
					}
				}
			}	
			
		}
	}
	
	private void spawnChickens(final Player player){
		final Chicken c1 = (Chicken) player.getWorld().spawnEntity(player.getLocation(), EntityType.CHICKEN);
		final Chicken c2 = (Chicken) player.getWorld().spawnEntity(player.getLocation(), EntityType.CHICKEN);
		final Chicken c3 = (Chicken) player.getWorld().spawnEntity(player.getLocation(), EntityType.CHICKEN);
		
		//c1.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 601, 255));
		//c2.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 601, 255));
		//c3.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 601, 255));
		
		final Silverfish s1 = (Silverfish) player.getWorld().spawnEntity(player.getLocation(), EntityType.SILVERFISH);
		final Silverfish s2 = (Silverfish) player.getWorld().spawnEntity(player.getLocation(), EntityType.SILVERFISH);
		final Silverfish s3 = (Silverfish) player.getWorld().spawnEntity(player.getLocation(), EntityType.SILVERFISH);
		
		s1.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 601, 1));
		s1.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 601, 4));
		s2.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 601, 1));
		s3.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 601, 1));
		
		s3.setPassenger(c3);
		c2.setPassenger(s3);
		s2.setPassenger(c2);
		c1.setPassenger(s2);
		s1.setPassenger(c1);
		
		c1.setMaxHealth(20);
		c2.setMaxHealth(20);
		c3.setMaxHealth(20);
		
		c1.setHealth(20);
		c2.setHealth(20);
		c3.setHealth(20);
		
		friendlyFish.add(s1);
		friendlyFish.add(s2);
		friendlyFish.add(s3);
		
		/*BukkitTask particleTimer = new BukkitRunnable(){
			public void run(){
				spawnHelix(s1.getLocation());
			}
		}.runTaskTimer(plugin, 0, 40);
		
		particleTimers.put(s1, particleTimer);
		*/
		//s1.setTarget(player);
		
		int d1 = (int)Math.floor((Math.random()*3+2) * 20);
		BukkitTask t1 = new BukkitRunnable(){
			public void run(){
				attack(c1);
			}
		}.runTaskTimer(plugin, d1, d1);
		
		int d2 = (int)Math.floor((Math.random()*3+2) * 20);
		BukkitTask t2 = new BukkitRunnable(){
			public void run(){
				attack(c2);
			}
		}.runTaskTimer(plugin, d2, d2);
		
		int d3 = (int)Math.floor((Math.random()*3+2) * 20);
		BukkitTask t3 = new BukkitRunnable(){
			public void run(){
				attack(c3);
			}
		}.runTaskTimer(plugin, d3, d3);
		
		chickens.put(c1, t1);
		chickens.put(c2, t2);
		chickens.put(c3, t3);
		
		final BukkitTask follow = new BukkitRunnable(){
			public void run(){
				if(s1.getLocation().distance(player.getLocation()) > 2){
					s1.setTarget(player);
					s1.removePotionEffect(PotionEffectType.SLOW);
				}else {
					s1.setTarget(s1);
					s1.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10, 255));
				}
			}
		}.runTaskTimer(plugin, 0, 5);
		
		new BukkitRunnable(){
			public void run(){
				if(!c1.isDead())
					c1.remove();
				if(!c2.isDead())
					c2.remove();
				if(!c3.isDead())
					c3.remove();
				
				if(!s1.isDead())
					s1.remove();
				if(!s2.isDead())
					s2.remove();
				if(!s3.isDead())
					s3.remove();
				
				if(chickens.containsKey(c1))
					((BukkitTask)chickens.remove(c1)).cancel();
				if(chickens.containsKey(c2))
					((BukkitTask)chickens.remove(c2)).cancel();
				if(chickens.containsKey(c3))
					((BukkitTask)chickens.remove(c3)).cancel();
				
				if(friendlyFish.contains(s1))
					friendlyFish.remove(s1);
				if(friendlyFish.contains(s2))
					friendlyFish.remove(s2);
				if(friendlyFish.contains(s3))
					friendlyFish.remove(s3);
				
				//((BukkitTask)particleTimers.remove(s1)).cancel();
				follow.cancel();
			}
		}.runTaskLater(plugin, 600);
	}
	
	
	private void spawnHelix(final Location loc){
		final Double[] time = {0.0};
		final double radius = 1;
		final double range = 4;
		
		new BukkitRunnable(){
			public void run(){
				double x1 = loc.getX() + xPos(time[0], radius);
				double y = loc.getY() + time[0];
				double z1 = loc.getZ() + zPos(time[0], radius);
				
				double x2 = loc.getX() + xPos(time[0]+Math.PI, radius);
				double z2 = loc.getZ() + zPos(time[0]+Math.PI, radius);
				
				PacketPlayOutWorldParticles packet1 = new PacketPlayOutWorldParticles(EnumParticle.FIREWORKS_SPARK, true, (float)x1, (float)y, (float)z1, 0f, 0f, 0f, 0f, 1);
				PacketPlayOutWorldParticles packet2 = new PacketPlayOutWorldParticles(EnumParticle.FIREWORKS_SPARK, true, (float)x2, (float)y, (float)z2, 0f, 0f, 0f, 0f, 1);
				
				for(Player player : Bukkit.getServer().getOnlinePlayers()){
					PlayerConnection c = ((CraftPlayer)player).getHandle().playerConnection;
					c.sendPacket(packet1);
					c.sendPacket(packet2);
				}
				
				time[0] += 0.2;
				if(time[0] > range)
					this.cancel();
			}
		}.runTaskTimer(plugin, 0, 1);
	}
	
	private double xPos(double time, double radius){
		return Math.sin(time) * radius;
	}
	
	private double zPos(double time, double radius){
		return Math.cos(time) * radius;
	}
	
	private void attack(Chicken chicken){
		Entity closest = null;
		double dist = Double.MAX_VALUE;
		Location loc = chicken.getLocation();
		
		for(Entity ent : loc.getWorld().getEntities()){
			if((ent instanceof Monster || ent instanceof Slime) && (!(ent instanceof Silverfish) || !friendlyFish.contains(ent))){
				double entDist = ent.getLocation().distance(loc);
				if(entDist <= 20){
					if(entDist < dist){
						closest = ent;
						dist = entDist;
					}
				}
			}
		}
		
		if(closest != null){
			Location entLoc = closest.getLocation();
			Vector vector = (new Vector(entLoc.getX()-loc.getX(), entLoc.getY()-loc.getY(), entLoc.getZ()-loc.getZ())).multiply(0.5);
			double magnitude = vector.length();
			double x = loc.getX() + vector.getX() / magnitude;
			double y = loc.getY() + vector.getY() / magnitude;
			double z = loc.getZ() + vector.getZ() / magnitude;
			
			Egg egg = chicken.getWorld().spawn(new Location(loc.getWorld(), x, y, z), Egg.class);
			egg.setShooter(chicken);
			egg.setVelocity(vector);
			eggs.add(egg);
			
			((Monster)closest).setTarget(chicken);
		}
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent ev){
		Entity proj = ev.getEntity();
		if(proj instanceof Egg && eggs.contains(proj)){
			Location loc = proj.getLocation();
			loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 0f, true, false);
			for(Entity ent : loc.getWorld().getEntities()){
				Location entLoc = ent.getLocation();
				if((ent instanceof Monster || ent instanceof Slime) && (!(ent instanceof Silverfish) || !friendlyFish.contains(ent))){
					if(entLoc.distance(loc) <= 5 && ent instanceof Damageable){
						((Damageable)ent).damage(5);
						ent.setVelocity(new Vector((5-Math.abs(entLoc.getX()-loc.getX()))/7, (5-Math.abs(entLoc.getY()-loc.getY()))/7, (5-Math.abs(entLoc.getZ()-loc.getZ()))/7));
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent ev){
		Entity damager = ev.getDamager();
		Entity victim = ev.getEntity();
		
		if(damager instanceof Egg && eggs.contains(damager)){
			ev.setCancelled(true);
			eggs.remove(damager);
			
			if((victim instanceof Monster || victim instanceof Slime) && (!(victim instanceof Silverfish) || !friendlyFish.contains(victim)))
				((Damageable)victim).damage(5);
			
			Location loc = damager.getLocation();
			loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 0f, true, false);
			
			for(Entity ent : loc.getWorld().getEntities()){
				Location entLoc = ent.getLocation();
				if((ent instanceof Monster || ent instanceof Slime) && (!(ent instanceof Silverfish) || !friendlyFish.contains(ent))){
					if(entLoc.distance(loc) <= 5 && ent instanceof Damageable){
						((Damageable)ent).damage(5);
						ent.setVelocity(new Vector((5-Math.abs(entLoc.getX()-loc.getX()))/7, (5-Math.abs(entLoc.getY()-loc.getY()))/7, (5-Math.abs(entLoc.getZ()-loc.getZ()))/7));
					}
				}
			}
		}
		else if(victim instanceof Chicken && /*!(damager instanceof Monster || damager instanceof Slime || (damager instanceof Projectile && ((Projectile)damager).getShooter() instanceof Monster)) &&*/
				chickens.containsKey(victim)){
			ev.setCancelled(true);
		}
		else if(victim instanceof Player && damager instanceof Silverfish && friendlyFish.contains(damager)){
			ev.setCancelled(true);
		}
		else if(damager instanceof Egg && eggs.contains((Egg)damager)){
			ev.setCancelled(true);
			eggs.remove(damager);
			
			//getLogger().info("------------------");
			//getLogger().info(victim.toString());
			//getLogger().info((victim instanceof Monster)+"");
			//getLogger().info((victim instanceof Silverfish)+"");
			//getLogger().info(friendlyFish.contains(victim)+"");
			
			if(victim instanceof Monster && (!(victim instanceof Silverfish) || !friendlyFish.contains(victim))){
				((Damageable)victim).damage(10);
			}
		}
	}
	
	
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent ev){
		Entity hurt = ev.getEntity();
		if(hurt instanceof Silverfish && friendlyFish.contains(hurt)){
			ev.setCancelled(true);
		}
	}
	
	@EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event)
    {
        if (event.getSpawnReason() == SpawnReason.EGG)
        {
            event.setCancelled(true);
        }
    }
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent ev){
		Entity dead = ev.getEntity();
		if(dead instanceof Chicken){
			if(chickens.containsKey((Chicken) dead)){
				((BukkitTask) chickens.remove((Chicken) dead)).cancel();
			}
		}
	}
}
