package net.tnn1nja.uuidMigrator;

import de.tr7zw.nbtapi.NBTEntity;
import de.tr7zw.nbtapi.NBTList;
import de.tr7zw.nbtapi.NBTTileEntity;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Tameable;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.logging.Logger;

public final class UUIDMigrator extends JavaPlugin implements CommandExecutor {


    Plugin me;
    Logger log = Bukkit.getLogger();
    UUID ItsAllGud = UUID.fromString("48a71f98-3e23-4e1c-aba9-39bd8a147b71");
    UUID Its4llGud = UUID.fromString("42609fbb-d926-4790-83d3-f034bd20064e");
    World w;
    int startX = -704;
    int currentX = startX;
    int endX = 287;
    int startZ = -928;
    int endZ = 319;


    @Override
    public void onEnable() {
        log.info("[UUIDMigrator] Successfully loaded.");
        getCommand("migrate").setExecutor(me);
        getCommand("fullmigrate").setExecutor(me);
        me = this;
        w = Bukkit.getWorlds().getFirst();
    }

    @Override
    public void onDisable() {
        log.info("[UUIDMigrator] Disabled");
    }


    public void migrateChunk(int x, int z){

        if(w.isChunkGenerated(x, z)){
            Chunk c = w.getChunkAt(x, z);
            boolean saveRequired = false;

            //Fix Vaults
            BlockState[] bss = c.getTileEntities();
            for(BlockState bs: bss){
                if(bs.getType() == Material.VAULT){
                    NBTList<UUID> uuids = new NBTTileEntity(bs)
                            .getCompound("server_data")
                            .getUUIDList("rewarded_players");
                    if(uuids.remove(ItsAllGud)){
                        uuids.add(Its4llGud);
                        saveRequired = true;
                    }
                }
            }

            //Fix Pets
            for (Entity en: c.getEntities()){
                if (en instanceof Tameable t && t.isTamed()){
                    NBTEntity pet = new NBTEntity(t);
                    if(pet.hasTag("Owner") && pet.getUUID("Owner").equals(ItsAllGud)){
                        pet.setUUID("Owner", Its4llGud);
                        saveRequired = true;
                    }
                }
            }

            c.unload(saveRequired);
        }

    }

    public void migrateChunkLine(int x){
        for(int z = startZ; z<endZ+1; z++){
            migrateChunk(x, z);
        }
    }

    public void migrateAllChunks(){
        log.info("World Migration Started, Server Unfrozen.");
        Bukkit.getServerTickManager().setFrozen(false);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(me, new Runnable() {
            @Override
            public void run() {
                if(currentX <= endX) {
                    migrateChunkLine(currentX);
                    currentX += 1;
                    log.info("Migrated Chunk Line at x = " + currentX);
                }else{
                    log.info("World Migration Completed, Server Shutting Down...");
                    Bukkit.shutdown();
                }
            }
        }, 0L, 20L);

    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("migrate")){
            World w = Bukkit.getWorlds().getFirst();
            migrateChunk(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
            if(w.isChunkGenerated(Integer.parseInt(args[0]), Integer.parseInt(args[1]))) {
                log.info("Migrated Chunk at " + args[0] + ", " + args[1]);
            }else{
                log.warning("Chunk not Generated, Aborting...");
            }
        }

        if (command.getName().equalsIgnoreCase("fullmigrate")){
            migrateAllChunks();
        }

        return true;

    }
}
