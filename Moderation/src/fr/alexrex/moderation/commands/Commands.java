package fr.alexrex.moderation.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


public class Commands implements CommandExecutor,Listener {

	private static HashMap<String, Boolean> playerMod = new HashMap<String, Boolean>();
	private static HashMap<UUID, Location> playersFreeze = new HashMap<UUID, Location>();
	public HashMap<UUID, ItemStack[]> playersInv = new HashMap<UUID, ItemStack[]>();
	private List<UUID> vanishedPlayer = new ArrayList<UUID>();
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			if(cmd.getName().equalsIgnoreCase("mod") && player.hasPermission("mod.use")) {
				if(!hasMod(player)) {
					setMod(player, true);
					saveInv(player);
					player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 255,false,false));
					player.setInvulnerable(true);
					player.sendMessage("§aVous venez d'entrer en mode Modération");
					player.getInventory().clear();
					player.getInventory().setItem(0, getItem(Material.CHEST, "§3Check d'inventaire"));
					player.getInventory().setItem(1, getItem(Material.LIME_DYE, "§aVisible"));
					player.getInventory().setItem(2, getItem(Material.ENDER_EYE, "§aTéleportaion aléatoire"));
					player.getInventory().setItem(3, getItem(Material.BOOK, "§6Informations du joueurs"));
					player.getInventory().setItem(4, getItem(Material.PACKED_ICE, "§9Freeze"));
					player.getInventory().setItem(8, getItem(Material.BARRIER, "§cQuitter"));
					player.setCanPickupItems(false);
				} else {
					Commands.playerMod.remove(player.getName());
					for(Player p : Bukkit.getOnlinePlayers()){
						if(player != p && !p.hasPermission("mod.use")){
							p.showPlayer(player);
						}
					}
					loadInv(player);
					player.sendMessage("§cVous venez de sortir du mode Modération");
					player.setCanPickupItems(true);
					player.setInvulnerable(false);
					player.removePotionEffect(PotionEffectType.NIGHT_VISION);
				}
				return true;
			}
			if(cmd.getName().equalsIgnoreCase("menu")) {
				if(args.length == 0) {
					player.sendMessage("§cErreur : la Command est : /menu <joueur>");
				}
				else if(Bukkit.getPlayer(args[0]) == null) {
					player.sendMessage("§cLe joueur " + args[0] + " n'est pas en ligne");
				}				
				else if(args.length == 1 && !args[0].isEmpty()) {
					Player target = Bukkit.getPlayer(args[0]);
					Inventory inv = Bukkit.createInventory(null, 54, target.getName());
					setInv(inv);
					
					ItemStack head = getHead(target);
					ItemMeta headM = head.getItemMeta();
					headM.setLore(Arrays.asList("§b" + (int) target.getHealth() + "§c❤"));
					head.setItemMeta(headM);
					inv.setItem(49, head);
					
					inv.setItem(20, getItem(Material.SKELETON_SKULL, "§4Tuer " + target.getName()));
					
					ItemStack loc = getItem(Material.SUNFLOWER, "§eLocalisation");
					ItemMeta locM = loc.getItemMeta();
					locM.setLore(Arrays.asList("§bX:" + (int) target.getLocation().getX() + " §bY:" + (int) target.getLocation().getY(),"§bZ:" + (int) target.getLocation().getZ() + " §bWorld:" + target.getLocation().getWorld().getName()));
					loc.setItemMeta(locM);
					
					inv.setItem(22, loc);
					inv.setItem(24, getItem(Material.BLAZE_ROD, "§cKick " + target.getName()));
					inv.setItem(30, getItem(Material.RED_DYE, "§dHeal"));
					inv.setItem(32, getItem(Material.PACKED_ICE, "§9Freeze"));
					player.openInventory(inv);
				}
				return true;
			}
			if(cmd.getName().equalsIgnoreCase("freeze")) {
				if(args.length == 0) {
					player.sendMessage("§cErreur : la Command est : /freeze <joueur>");
				}
				else if(Bukkit.getPlayer(args[0]) == null) {
					player.sendMessage("§cLe joueur " + args[0] + " n'est pas en ligne");
				}				
				else if(args.length == 1 && !args[0].isEmpty()) {
					Player target = Bukkit.getPlayer(args[0]);
					if(!playersFreeze.containsKey(target.getUniqueId())) {
						playersFreeze.put(target.getUniqueId(),target.getLocation());
						player.sendMessage("§3Le Joueur " + target.getName() + " a été Freeze");
						target.setInvulnerable(true);
					} else {
						playersFreeze.remove(target.getUniqueId());
						player.sendMessage("§3Le Joueur " + target.getName() + " n'est plus Freeze");
						target.setInvulnerable(false);
					}
				}
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInteract(PlayerInteractEvent event){
		Player player = event.getPlayer();
		Action action = event.getAction();
		ItemStack it = event.getItem();

		if(it == null) return;
		
		if (hasMod(player)) {		
			if(it.getType() == Material.LIME_DYE && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
				vanishedPlayer.add(player.getUniqueId());
				for(Player p : Bukkit.getOnlinePlayers()){
					if(player != p && !p.hasPermission("mod.use")){
						p.hidePlayer(player);
					}
				}
				player.getInventory().setItem(1, getItem(Material.GRAY_DYE, "§8Invisible"));
				player.sendMessage("§cVous êtes désormais invisible");
			}
			if(it.getType() == Material.GRAY_DYE && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
				vanishedPlayer.remove(player.getUniqueId());
				for(Player p : Bukkit.getOnlinePlayers()){
					if(player != p && !p.hasPermission("mod.use")){
						p.showPlayer(player);
					}
				}
				player.getInventory().setItem(1, getItem(Material.LIME_DYE, "§aVisible"));
				player.sendMessage("§aVous êtes désormais visible");
			}
			if(it.getType() == Material.ENDER_EYE && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
				event.setCancelled(true);
				Player p = getRandomPlayer(player);
				player.teleport(p);
				player.sendMessage("§bVous venez d'être téléporté sur " + p.getName());
			}
			if(it.getType() == Material.BARRIER && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
				event.setCancelled(true);
				player.performCommand("mod");
			}
			if(it.getType() == Material.PACKED_ICE || it.getType() == Material.CHEST && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
				event.setCancelled(true);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void clickEntity(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		if (event.getRightClicked() instanceof Player && hasMod(player) && event.getHand().toString() == "HAND") {
			Player pclicked = (Player) event.getRightClicked();
			if(player.getItemInHand().getType() == Material.CHEST && !pclicked.hasPermission("mod.use")) {
				player.openInventory(pclicked.getInventory());
			}
			if(player.getItemInHand().getType() == Material.BOOK && !pclicked.hasPermission("menu.use")) {
				player.performCommand("menu " + pclicked.getName());
			}
			if(player.getItemInHand().getType() == Material.PACKED_ICE) {
				player.performCommand("freeze " + pclicked.getName());
			}
		}
	}
	
	@EventHandler
	public void clickInv(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		ItemStack item = event.getCurrentItem();		
		if (Bukkit.getPlayer(event.getView().getTitle()) != null && !(item == null)) {
			Player target = Bukkit.getPlayer(event.getView().getTitle());
			event.setCancelled(true);
			if(item.getType() == Material.RED_DYE) {
				target.setHealth(20);
				target.setFoodLevel(20);
				target.setFireTicks(0);
				player.sendMessage("§aLe joueur " +target.getName() + " a bien été Healé");
			}
			if(item.getType() == Material.SKELETON_SKULL) {
				target.setHealth(0.0D);
				player.sendMessage("§4Le joueur " + target.getName() + " a bien été tué");
			}
			if(item.getType() == Material.BLAZE_ROD) {
				target.kickPlayer("§c" + player.getName() + " vous a expulsé!");
				player.sendMessage("§4Le joueur " + target.getName() + " a bien été expulsé");
			}
			if(item.getType() == Material.PACKED_ICE) {
				player.performCommand("freeze " + target.getName());
			}
			if(item.getType() == Material.SUNFLOWER) {
				player.teleport(target);
				player.closeInventory();
			}
			
		}
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		Player player = (Player) event.getPlayer();
		if(playersFreeze.containsKey(player.getUniqueId())){
			event.setTo(playersFreeze.get(player.getUniqueId()));
			player.sendMessage("§4Vous avez été freeze ne bougez pas !!");
		}
	}
	
	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		if (hasMod(player)) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEntityTarget(EntityTargetEvent event) {
		if (event.getTarget() instanceof Player) {
			Player p = (Player) event.getTarget();
			if (hasMod(p)) event.setCancelled(true);
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if(!vanishedPlayer.isEmpty()) {
			for(UUID uuid : vanishedPlayer) {
				p.hidePlayer(Bukkit.getPlayer(uuid));
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		if(!vanishedPlayer.isEmpty()) {
			for(UUID uuid : vanishedPlayer) {
				p.showPlayer(Bukkit.getPlayer(uuid));
			}
		}
	}
	
	public ItemStack getItem(Material material, String customName){
		ItemStack it = new ItemStack(material,1);
		ItemMeta itM = it.getItemMeta();
		itM.setDisplayName(customName);
		it.setItemMeta(itM);
		return it;
	}
	
	public ItemStack getHead(Player player) {
		ItemStack skull = new ItemStack(Material.PLAYER_HEAD,1);
		SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
		skullMeta.setDisplayName("" + player.getName());
        skullMeta.setOwningPlayer(player);
        skull.setItemMeta(skullMeta);
        return skull;
	}
	
	public void setMod(Player player,Boolean mod) {
		Commands.playerMod.put(player.getName(),mod);
	}
	
	public Boolean hasMod(Player player) {
		if (playerMod.containsKey(player.getName())) {
			return true;
		} else {
			return false;
		}
	}
	
	public Player getRandomPlayer(Player player){
		double nbj = 0;
		int random = new Random().nextInt(Bukkit.getServer().getOnlinePlayers().size());
		Player p = (Player) Bukkit.getServer().getOnlinePlayers().toArray()[random];
		for(Player pl : Bukkit.getOnlinePlayers()) {
			if(!pl.hasPermission("mod.use")){
				nbj++;
			}
		}
		if(nbj>0) {		
			while(p == player || Commands.playerMod.containsKey(p.getName()) || p.hasPermission("mod.use")) {
				random = new Random().nextInt(Bukkit.getServer().getOnlinePlayers().size());
				p = (Player) Bukkit.getServer().getOnlinePlayers().toArray()[random];
			}
			return p;
		} else {
			return player;
		}
	}
	
	public void saveInv(Player player) {
		UUID id = player.getUniqueId();
		playersInv.put(id,player.getInventory().getContents());
	}
	
	public void loadInv(Player player) {
		UUID id = player.getUniqueId();
		player.getInventory().setContents(playersInv.get(id));
		playersInv.remove(id);
		player.updateInventory();
	}
	
	public void setInv(Inventory inv) {
		for (int i = 0 ; i < 9 ; i++) {
			inv.setItem(i, getItem(Material.WHITE_STAINED_GLASS_PANE, " "));
		}
		for (int i = 46 ; i < 54 ; i++) {
			inv.setItem(i, getItem(Material.WHITE_STAINED_GLASS_PANE, " "));
		}
		for (int i = 9 ; i <46 ; i = i + 9) {
			inv.setItem(i, getItem(Material.WHITE_STAINED_GLASS_PANE, " "));
		}
		for (int i = 17 ; i <54 ; i = i + 9) {
			inv.setItem(i, getItem(Material.WHITE_STAINED_GLASS_PANE, " "));
		}
	}
}
