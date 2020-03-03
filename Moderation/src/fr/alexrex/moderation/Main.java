package fr.alexrex.moderation;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import fr.alexrex.moderation.commands.Commands;
import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin{
	
	@Override
	public void onEnable() {
		getCommand("mod").setExecutor(new Commands());
		getCommand("alert").setExecutor(new Commands());
		getCommand("menu").setExecutor(new Commands());
		getCommand("freeze").setExecutor(new Commands());
		Bukkit.getPluginManager().registerEvents(new Commands(), this);
	}
	
	@Override
	public void onDisable() {
		System.out.println(ChatColor.RED + "Le plugin vient de s'�teindre");
	}

}
