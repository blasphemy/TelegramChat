package org.leining.TelegramChat;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.leining.TelegramComponents.Chat;

import java.io.*;


public class Main extends JavaPlugin implements Listener {
    public static File dataDir = new File("plugins/TelegramChat/data.json");
    public static FileConfiguration cfg;

    public static Data data = new Data();
    static Plugin pl;

    public static void save() {
        Gson gson = new Gson();

        try {
            FileOutputStream fileOut = new FileOutputStream(dataDir);
            ObjectOutputStream oos = new ObjectOutputStream(fileOut);

            oos.writeObject(gson.toJson(data));
            fileOut.close();
            oos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void sendToMC(String msg, String tgUsername) {
        String msgF = Main.cfg.getString("chat-format").replace('&', '§').replace("%player%", tgUsername).replace("%message%", msg);
        Bukkit.broadcastMessage(msgF);
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        cfg = this.getConfig();
        this.pl = this;
        Bukkit.getPluginCommand("telegram").setExecutor(new TelegramCmd());
        Bukkit.getPluginManager().registerEvents(this, this);
        File dir = new File("plugins/TelegramChat/");
        dir.mkdir();
        data = new Data();
        if (dataDir.exists()) {
            try {
                FileInputStream fin = new FileInputStream(dataDir);
                ObjectInputStream ois = new ObjectInputStream(fin);
                Gson gson = new Gson();
                data = (Data) gson.fromJson((String) ois.readObject(), Data.class);
                ois.close();
                fin.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        Telegram.auth();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if (Telegram.connected) {
                Telegram.getUpdate();
            }
        }, 20L, 20L);
    }

    @Override
    public void onDisable() {
        save();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!this.getConfig().getBoolean("enable-joinquitmessages")) return;
        if (Telegram.connected) {
            Chat chat = new Chat();
            chat.parse_mode = "Markdown";
            chat.text = "`" + e.getPlayer().getName() + " joined the game.`";
            Telegram.sendAll(chat);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (!this.getConfig().getBoolean("enable-deathmessages")) return;
        if (Telegram.connected) {
            Chat chat = new Chat();
            chat.parse_mode = "Markdown";
            chat.text = "`" + e.getDeathMessage() + "`";
            Telegram.sendAll(chat);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (!this.getConfig().getBoolean("enable-joinquitmessages")) return;
        if (Telegram.connected) {
            Chat chat = new Chat();
            chat.parse_mode = "Markdown";
            chat.text = "`" + e.getPlayer().getName() + " left the game.`";
            System.out.println(chat.text);
            Telegram.sendAll(chat);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (!this.getConfig().getBoolean("enable-chatmessages")) return;
        if (Telegram.connected) {
            String msg;
            msg = TextFormatter.stripMarkdown(e.getMessage());
            Chat chat = new Chat();
            chat.parse_mode = "Markdown";
            chat.text = "*" + e.getPlayer().getName() + "*: " + msg;
            Telegram.sendAll(chat);
        }
    }
}
