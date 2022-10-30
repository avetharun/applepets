package com.avetharun.applepets;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

public class PlayerPetFile {
    public static HashMap<UUID, PlayerPetFile> PlayerPetFiles = new HashMap<>();
    public PlayerPetFile() {}

    //
    //  Actual file, rest is boilerplate for IO.
    //
    private HashMap<String, UserApplePet> pets = new HashMap<>();
    public void setPets(HashMap<String, UserApplePet> pets1) {
        pets = pets1;
    }
    public HashMap<String,UserApplePet> getPets(){
        return pets == null? new HashMap<>() : pets;
    }
    public static boolean AddPetToPlayerOffline(UUID player, String petUUID) {
        return false;
    }
    // assumes player is online
    public static String AddPetToPlayerOnline(UUID player, String petUUID) {
        Applepets.getInstance().getLogger().log(Level.WARNING, String.format("Adding pet with ID %s to UUID %s, name %s", petUUID, player, Applepets.getInstance().getServer().getPlayer(player).getName()));
        PlayerPetFile f = GetPlayerPets(player);
        if (f.pets.containsKey(petUUID)) {return "EXISTS";}
        UserApplePet p = new UserApplePet();
        p.RegistryUUID = petUUID;
        p.display = (ApplePetRegistry.GetOrDefault(petUUID).getDisplay());
        String s = alib.HashPlayerPet(player.toString(), petUUID);
        f.pets.put(s, p);
        return s;
    }
    private static final PlayerPetFile EmptyPlayerPetFile = new PlayerPetFile();
    public static PlayerPetFile GetPlayerPets(UUID u) {
        if (!PlayerPetFiles.containsKey(u)) {
            PushPlayerPetfile(u);
        }
        PlayerPetFile f = PlayerPetFiles.getOrDefault(u, EmptyPlayerPetFile);
        return f == null? PushPlayerPetfile(u) : f;
    }
    // Parses and stores the pet into a file
    public static PlayerPetFile ParsePlayerPetFile(File f) {
        Yaml Y = new Yaml(new CustomClassLoaderConstructor(PlayerPetFile.class.getClassLoader()));
        try {return Y.loadAs(new FileInputStream(f), PlayerPetFile.class);
        } catch (FileNotFoundException ignored) {}
        return EmptyPlayerPetFile;
    }
    public static String ConvertPetsToYaml(UUID u) {
        Applepets.getInstance().getLogger().log(Level.WARNING, GetPlayerPets(u).getPets().toString());
        String Y = new Yaml(new CustomClassLoaderConstructor(PlayerPetFile.class.getClassLoader())).dump(PlayerPetFiles.get(u));
        Applepets.getInstance().getLogger().log(Level.WARNING, Y);
        return Y;
    }
    // Appends Player U's pets into memory.
    public static PlayerPetFile PushPlayerPetfile(UUID u) {
        File PLAYER_FILE = new File(Applepets.APPLEPETS_PLAYERDATA_FOLDER.getAbsolutePath() +File.separator+ u.toString()+".yml");
        if (!PLAYER_FILE.exists()) {
            try {
                if (PLAYER_FILE.createNewFile()) {
                    Applepets.getInstance().getLogger().log(Level.INFO, "Created file for user");
                };
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        PlayerPetFile f = ParsePlayerPetFile(PLAYER_FILE);
        PlayerPetFiles.put(u, f);
        if (f == null) {
            // create an empty one
            return new PlayerPetFile();
        }
        return f;
    }
    // Removes Player U's pets from memory.
    public static void PopPlayerPetfile(UUID u) {
        File PLAYER_FILE = new File(Applepets.APPLEPETS_PLAYERDATA_FOLDER.getAbsolutePath() +File.separator+ u.toString()+".yml");
        try {
            FileWriter w = new FileWriter(PLAYER_FILE);
            w.write(ConvertPetsToYaml(u));
            w.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        PlayerPetFiles.remove(u);
    }
}
