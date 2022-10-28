package com.avetharun.applepets;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

public class PlayerPetFile {
    public File _file;


    public static HashMap<UUID, PlayerPetFile> PlayerPetFiles = new HashMap<>();
    public PlayerPetFile() {}

    //
    //  Actual file, rest is boilerplate for IO.
    //
    private HashMap<String, UserApplePet> pets = new HashMap<>();
    public void setPets(HashMap<String, UserApplePet> pets1) {
        pets = pets1;

        pets.forEach((uuid, pet) -> {
            pet.RegistryUUID = uuid;
        });
    }
    public HashMap<String,UserApplePet> getPets(){
        return pets == null? new HashMap<>() : pets;
    }


    private static final PlayerPetFile EmptyPlayerPetFile = new PlayerPetFile();
    public static PlayerPetFile GetPlayerPets(UUID u) {
        if (!PlayerPetFiles.containsKey(u)) {
            PushPlayerPetfile(u);
        }
        PlayerPetFile f = PlayerPetFiles.getOrDefault(u, EmptyPlayerPetFile);
        return f == null? new PlayerPetFile() : f;
    }
    // Parses and stores the pet into a file
    public static PlayerPetFile ParsePlayerPetFile(File f) {
        Yaml Y = new Yaml(new CustomClassLoaderConstructor(PlayerPetFile.class.getClassLoader()));
        try {return Y.loadAs(new FileInputStream(f), PlayerPetFile.class);
        } catch (FileNotFoundException ignored) {}
        return EmptyPlayerPetFile;
    }
    public static String ConvertPetsToYaml(UUID u) {
        return new Yaml().dumpAsMap(GetPlayerPets(u));
    }
    // Appends Player U's pets into memory.
    public static void PushPlayerPetfile(UUID u) {
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
        PlayerPetFiles.putIfAbsent(u, ParsePlayerPetFile(PLAYER_FILE));
    }
    // Removes Player U's pets from memory.
    public static void PopPlayerPetfile(UUID u) {
        PlayerPetFiles.remove(u);
    }
}
