package com.scarasol.pillagers_gun.config;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class CommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Double> EQUIP_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<Double> DROP_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> BYPASS_INVULNERABLE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> DYNAMIC_INACCURACY;
    public static final ForgeConfigSpec.ConfigValue<Boolean> FRIEND_FIRE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_CROSS_FIRE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> TAG_FRIENDLY_FIRE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> BREAK_GLASS;
    public static final ForgeConfigSpec.ConfigValue<Double> LASER_ANGLE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> GUN_MODEL;

    public static final ForgeConfigSpec.ConfigValue<Double> SHOTGUN_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<Integer> SHOTGUN_POWER;
    public static final ForgeConfigSpec.ConfigValue<Integer> SHOTGUN_COUNT;
    public static final ForgeConfigSpec.ConfigValue<Integer> SHOTGUN_INACCURACY;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SHOTGUN_RENDER_LASER;

    public static final ForgeConfigSpec.ConfigValue<Double> PISTOL_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<Integer> PISTOL_POWER;
    public static final ForgeConfigSpec.ConfigValue<Integer> PISTOL_INACCURACY;
    public static final ForgeConfigSpec.ConfigValue<Boolean> PISTOL_RENDER_LASER;

    public static final ForgeConfigSpec.ConfigValue<Double> ASSAULT_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<Integer> ASSAULT_POWER;
    public static final ForgeConfigSpec.ConfigValue<Double> ASSAULT_BYPASS_RATE;
    public static final ForgeConfigSpec.ConfigValue<Integer> ASSAULT_INACCURACY;
    public static final ForgeConfigSpec.ConfigValue<Boolean> AUTOMATIC_SHOOTING;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ASSAULT_RENDER_LASER;

    public static final ForgeConfigSpec.ConfigValue<Double> SNIPERS_RIFLE_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<Integer> SNIPERS_RIFLE_POWER;
    public static final ForgeConfigSpec.ConfigValue<Integer> SNIPERS_RIFLE_BONUS;
    public static final ForgeConfigSpec.ConfigValue<Double> SNIPERS_RIFLE_BYPASS_RATE;
    public static final ForgeConfigSpec.ConfigValue<Integer> SNIPERS_RIFLE_INACCURACY;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SNIPERS_RENDER_LASER;

    public static final ForgeConfigSpec.ConfigValue<Double> BAZOOKA_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<Integer> BAZOOKA_EXPLOSION_LEVEL;
    public static final ForgeConfigSpec.ConfigValue<Boolean> BAZOOKA_BREAK;
    public static final ForgeConfigSpec.ConfigValue<Integer> BAZOOKA_INACCURACY;
    public static final ForgeConfigSpec.ConfigValue<Boolean> BAZOOKA_RENDER_LASER;

    public static final ForgeConfigSpec.ConfigValue<Double> FLAMETHROWER_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<Double> FUEL_CANISTER_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<Double> NAPALM_FUEL_CANISTER_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<Double> HIGH_TEMPERATURE_CANISTER_CHANCE;

    public static final ForgeConfigSpec.ConfigValue<Boolean> TACZ_GUN_USE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> TACZ_GUN_SPAWN;
    public static final ForgeConfigSpec.ConfigValue<Boolean> TACZ_GUNNERS_NEED_AMMO;

    public static final ForgeConfigSpec.ConfigValue<Double> GUARD_TACZ_GUN_MIN_AMMO;
    public static final ForgeConfigSpec.ConfigValue<Double> GUARD_TACZ_GUN_MAX_AMMO;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> TACZ_GUN_TYPE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> TACZ_GUN_INACCURACY;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> TACZ_RENDER_LASER;
    public static final ForgeConfigSpec.ConfigValue<Boolean> GUARD_SPAWN_WITH_TACZ_GUN;

    public static final ForgeConfigSpec.ConfigValue<Boolean> SBW_GUN_USE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SBW_GUN_SPAWN;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SBW_GUNNERS_NEED_AMMO;
    public static final ForgeConfigSpec.ConfigValue<Double> SBW_GUN_MIN_AMMO;
    public static final ForgeConfigSpec.ConfigValue<Double> SBW_GUN_MAX_AMMO;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> SBW_GUN_TYPE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> SBW_GUN_INACCURACY;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> SBW_RENDER_LASER;


    public static final ForgeConfigSpec.ConfigValue<Boolean> GUARD_SPAWN_WITH_GUN;
    public static final ForgeConfigSpec.ConfigValue<Double> GUARD_EQUIP_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<Double> GUARD_SHOTGUN_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<Double> GUARD_PISTOL_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<Double> GUARD_ASSAULT_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<Double> GUARD_SNIPERS_RIFLE_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<Double> GUARD_BAZOOKA_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<Double> GUARD_FLAMETHROWER_CHANCE;

    static {
        EQUIP_CHANCE = BUILDER.comment("The chance of pillager armed with a gun.")
                .defineInRange("Armed Chance", 1.0, 0.0, 1.0);
        DROP_CHANCE = BUILDER.comment("The chance of pillagers will drop their guns on death.")
                .defineInRange("Drop Chance", 0, 0.0, 1.0);
        BYPASS_INVULNERABLE = BUILDER.comment("Whether bullets ignore damage immunity.")
                .define("Ignore Damage Immunity", true);
        DYNAMIC_INACCURACY = BUILDER.comment("Whether inaccuracy changes dynamically based on the target’s speed and distance.")
                .define("Dynamic Inaccuracy", true);
        FRIEND_FIRE = BUILDER.comment("Whether the gun has friend fire.")
                .define("Friend Fire", false);
        ENABLE_CROSS_FIRE = BUILDER.comment("""
                Enables cross-fire tactical AI for gun-wielding mobs.
                This option is the tactical feature entry point for fire-team coordination and tactical reload scheduling.
                """)
                .define("Enable Cross Fire", false);
        TAG_FRIENDLY_FIRE = BUILDER.comment("Entities sharing the same tag from this list will be treated as allies.")
                .defineList("Friendly Tag", ArrayList::new, entry -> true);
        BREAK_GLASS = BUILDER.comment("Whether the bullet will break the glass.")
                .define("Break Glass", true);
        LASER_ANGLE = BUILDER.comment("""
                The laser will be rendered when the angle between the gun-wielding entity’s facing direction and the player is smaller than a certain threshold. 
                Note that this angle gradually decreases as the distance between the player and the gun-wielding entity increases.
                """)
                .defineInRange("Laser Render Angle", 15.0, 5, 180);
        GUN_MODEL = BUILDER.comment("Whether the gun will use the models in tacz and sbw.")
                .define("Gun Model Switch", false);


        BUILDER.push("Shot Gun");
        SHOTGUN_CHANCE = BUILDER.comment("The chance of shotgun.")
                .defineInRange("Shotgun Chance", 0.2, 0.0, 1.0);
        SHOTGUN_POWER = BUILDER.comment("The power of each shotgun shell.")
                .defineInRange("Shotgun Power", 5, 0, 5000);
        SHOTGUN_COUNT = BUILDER.comment("The number of shotgun shells.")
                .defineInRange("Shells Number", 7, 0, 200);
        SHOTGUN_INACCURACY = BUILDER.comment("The shooting inaccuracy of shotgun AI; the lower the value, the higher the accuracy.")
                .defineInRange("Shotgun Inaccuracy", 2, 0, 10);
        SHOTGUN_RENDER_LASER = BUILDER.comment("Whether to render the laser when mobs within a certain distance aim their guns at the player.")
                .define("ShotGun Render Laser", false);
        BUILDER.pop();

        BUILDER.push("Pistol");
        PISTOL_CHANCE = BUILDER.comment("The chance of pistol.")
                .defineInRange("Pistol Chance", 0.5, 0.0, 1.0);
        PISTOL_POWER = BUILDER.comment("The power of pistol bullet.")
                .defineInRange("Pistol Power", 9, 0, 5000);
        PISTOL_INACCURACY = BUILDER.comment("The shooting inaccuracy of pistol AI; the lower the value, the higher the accuracy.")
                .defineInRange("Pistol Inaccuracy", 2, 0, 10);
        PISTOL_RENDER_LASER = BUILDER.comment("Whether to render the laser when mobs within a certain distance aim their guns at the player.")
                .define("Pistol Render Laser", false);
        BUILDER.pop();

        BUILDER.push("Assault Rifle");
        ASSAULT_CHANCE = BUILDER.comment("The chance of assault rifle.")
                .defineInRange("Assault Rifle Chance", 0.2, 0.0, 1.0);
        ASSAULT_POWER = BUILDER.comment("The power of assault rifle bullet.")
                .defineInRange("Assault Rifle Power", 12, 0, 5000);
        ASSAULT_BYPASS_RATE = BUILDER.comment("The percentage of assault rifle bullet damage that ignore armor.")
                .defineInRange("Assault Rifle Bypass Armor Chance", 0.25, 0.0, 1.0);
        ASSAULT_INACCURACY = BUILDER.comment("The shooting inaccuracy of assault rifle AI; the lower the value, the higher the accuracy.")
                .defineInRange("Assault Rifle Inaccuracy", 2, 0, 10);
        AUTOMATIC_SHOOTING = BUILDER.comment("Whether or not the assault rifle will fire automatically. If false, the assault rifle will use the three-round burst mode.")
                .define("Automatic Shooting", true);
        ASSAULT_RENDER_LASER = BUILDER.comment("Whether to render the laser when mobs within a certain distance aim their guns at the player.")
                .define("Assault Render Laser", false);
        BUILDER.pop();

        BUILDER.push("Sniper's Rifle");
        SNIPERS_RIFLE_CHANCE = BUILDER.comment("The chance of sniper's rifle.")
                .defineInRange("Sniper's Rifle Chance", 0.05, 0.0, 1.0);
        SNIPERS_RIFLE_POWER = BUILDER.comment("The power of sniper's rifle bullet.")
                .defineInRange("Sniper's Rifle Power", 25, 0, 5000);
        SNIPERS_RIFLE_BONUS = BUILDER.comment("Sniper Rifle AI Attack Range Multiplier.")
                .defineInRange("Sniper's Rifle Bonus", 1, 0, 5);
        SNIPERS_RIFLE_BYPASS_RATE = BUILDER.comment("The percentage of sniper's rifle bullet damage that ignore armor.")
                .defineInRange("Sniper's Rifle Bypass Armor Chance", 0.7, 0.0, 1.0);
        SNIPERS_RIFLE_INACCURACY = BUILDER.comment("The shooting inaccuracy of sniper's rifle AI; the lower the value, the higher the accuracy.")
                .defineInRange("Sniper's Rifle Inaccuracy", 2, 0, 10);
        SNIPERS_RENDER_LASER = BUILDER.comment("Whether to render the laser when mobs within a certain distance aim their guns at the player.")
                .define("Snipers Render Laser", true);
        BUILDER.pop();

        BUILDER.push("Bazooka");
        BAZOOKA_CHANCE = BUILDER.comment("The chance of bazooka.")
                .defineInRange("Bazooka Chance", 0.05, 0.0, 1.0);
        BAZOOKA_EXPLOSION_LEVEL = BUILDER.comment("The explosion level of bazooka rocket.")
                .defineInRange("Bazooka Explosion Level", 5, 1, 50);
        BAZOOKA_BREAK = BUILDER.comment("Whether the explosion of bazooka rocket will break block.")
                .define("Bazooka Break Block", false);
        BAZOOKA_INACCURACY = BUILDER.comment("The shooting inaccuracy of bazooka AI; the lower the value, the higher the accuracy.")
                .defineInRange("Bazooka Inaccuracy", 2, 0, 10);
        BAZOOKA_RENDER_LASER = BUILDER.comment("Whether to render the laser when mobs within a certain distance aim their guns at the player.")
                .define("Bazooka Render Laser", false);
        BUILDER.pop();

        BUILDER.push("Combat - Zombie Survival Kit");
        FLAMETHROWER_CHANCE = BUILDER.comment("The chance of flamethrower.")
                .defineInRange("Flamethrower Chance", 0.05, 0.0, 1.0);
        FUEL_CANISTER_CHANCE = BUILDER.comment("The chance of flamethrower with fuel canister.")
                .defineInRange("Fuel Canister Chance", 0.8, 0.0, 1.0);
        NAPALM_FUEL_CANISTER_CHANCE = BUILDER.comment("The chance of flamethrower with napalm fuel canister.")
                .defineInRange("Napalm Fuel Canister Chance", 0.1, 0.0, 1.0);
        HIGH_TEMPERATURE_CANISTER_CHANCE = BUILDER.comment("The chance of flamethrower with high temperature fuel canister.")
                .defineInRange("High Temperature Fuel Canister Chance", 0.1, 0.0, 1.0);
        BUILDER.pop();

        BUILDER.push("Compat - TACZ");
        TACZ_GUN_USE = BUILDER.comment("Whether gunners are allowed to use TACZ firearms.")
                .define("TACZ Compat", true);
        TACZ_GUN_SPAWN = BUILDER.comment("""
                Whether gunners are allowed to spawn with TACZ firearms.
                WARNING: Spawning with TACZ firearms will significantly increase the game's difficulty.
                """).define("Spawn With TACZ", false);
        TACZ_GUNNERS_NEED_AMMO = BUILDER.comment("""
                Whether gunners consume ammo when using TACZ firearms.
                If enabled, gunners that spawn with TACZ weapons will also receive some spare ammunition.
                """)
                .define("Gunner Needs Ammo In TACZ", false);
        GUARD_TACZ_GUN_MIN_AMMO = BUILDER.comment("""
                If gunners consume ammo when using TACZ firearms, this sets the minimum number of magazines they will spawn with.
                """).defineInRange("TACZ Min Ammo", 3, 0, 100.0);
        GUARD_TACZ_GUN_MAX_AMMO = BUILDER.comment("""
                If gunners consume ammo when using TACZ firearms, this sets the maximum number of magazines they will spawn with.
                """).defineInRange("TACZ Max Ammo", 6, 0, 100.0);
        TACZ_GUN_TYPE = BUILDER.comment("""
                TACZ firearm IDs and their weights for gunners upon spawning.
                Format: "tacz:m1911, 5" — this means the M1911 has a spawn weight of 5, and the probability will be calculated based on the total weight sum.
                """).defineList("TACZ Gun Type", ArrayList::new, element -> true);
        TACZ_GUN_INACCURACY = BUILDER.comment("""
                The inaccuracy values for different types of TACZ firearms used by gunners.
                The lower the value, the higher the shooting accuracy.
                Format: "pistol, 5" — this means the inaccuracy value for pistols is 5.
                """).defineList("TACZ Gun Inaccuracy", Lists.newArrayList("pistol, 2", "sniper, 0.15", "rifle, 2", "shotgun, 5", "smg, 5", "rpg, 5", "mg, 5"), element -> true);
        TACZ_RENDER_LASER = BUILDER.comment("Guns that cause mobs to render a laser when they aim at the player within a certain distance.")
                .defineList("TACZ Render Laser", Lists.newArrayList("sniper"), element -> true);
        BUILDER.push("Compat - Guard Villager & Recruits");
        GUARD_SPAWN_WITH_TACZ_GUN = BUILDER.comment("Whether Guard or Recruit will spawn with gun in TACZ.")
                .define("Villager Spawn With TACZ Gun", false);
        BUILDER.pop();
        BUILDER.pop();

        BUILDER.push("Compat - SBW");
        SBW_GUN_USE = BUILDER.comment("Whether gunners are allowed to use Superb Warfare firearms.")
                .define("SBW Compat", true);
        SBW_GUN_SPAWN = BUILDER.comment("""
                Whether gunners are allowed to spawn with Superb Warfare firearms.
                WARNING: Spawning with Superb Warfare firearms will significantly increase the game's difficulty.
                """).define("Spawn With SBW", false);
        SBW_GUNNERS_NEED_AMMO = BUILDER.comment("""
                Whether gunners consume ammo when using Superb Warfare firearms.
                If disabled, SBW gunners use infinite backup ammo without adding spare ammo to dropped guns.
                """)
                .define("Gunner Needs Ammo In SBW", false);
        SBW_GUN_MIN_AMMO = BUILDER.comment("""
                If SBW gunners consume ammo, this sets the minimum number of magazines they will spawn with.
                """).defineInRange("SBW Min Ammo", 3, 0, 100.0);
        SBW_GUN_MAX_AMMO = BUILDER.comment("""
                If SBW gunners consume ammo, this sets the maximum number of magazines they will spawn with.
                """).defineInRange("SBW Max Ammo", 6, 0, 100.0);
        SBW_GUN_TYPE = BUILDER.comment("""
                Superb Warfare firearm item IDs and their weights for gunners upon spawning.
                Format: "superbwarfare:m_1911, 5" means the M1911 has a spawn weight of 5.
                """).defineList("SBW Gun Type", Lists.newArrayList(
                "superbwarfare:m_1911, 0.5",
                "superbwarfare:ak_47, 0.2",
                "superbwarfare:m_870, 0.2",
                "superbwarfare:m_98b, 0.05",
                "superbwarfare:rpg, 0.05"
        ), element -> true);
        SBW_GUN_INACCURACY = BUILDER.comment("""
                The inaccuracy values for different types of Superb Warfare firearms used by gunners.
                The lower the value, the higher the shooting accuracy.
                Format: "handgun, 2" means the inaccuracy value for handguns is 2.
                """).defineList("SBW Gun Inaccuracy", Lists.newArrayList(
                "handgun, 2",
                "sniper, 0.15",
                "rifle, 2",
                "shotgun, 5",
                "smg, 5",
                "machinegun, 5",
                "directlauncher, 5",
                "curvedlauncher, 5",
                "special, 2"
        ), element -> true);
        SBW_RENDER_LASER = BUILDER.comment("Superb Warfare gun types that cause mobs to render a laser when aiming at the player.")
                .defineList("SBW Render Laser", Lists.newArrayList("sniper"), element -> true);
        BUILDER.pop();

        BUILDER.push("Compat - Guard Villager & Recruits");
        GUARD_SPAWN_WITH_GUN = BUILDER.comment("Whether Guard or Recruit will spawn with gun.")
                .define("Villager Spawn With Gun", true);
        GUARD_EQUIP_CHANCE = BUILDER.comment("The chance of Guard or Recruit armed with a gun.")
                .defineInRange("Villager Armed Chance", 1.0, 0.0, 1.0);
        GUARD_SHOTGUN_CHANCE = BUILDER.comment("The chance of shotgun.")
                .defineInRange("Villager Shotgun Chance", 0.2, 0.0, 1.0);
        GUARD_PISTOL_CHANCE = BUILDER.comment("The chance of pistol.")
                .defineInRange("Villager Pistol Chance", 0.5, 0.0, 1.0);
        GUARD_ASSAULT_CHANCE = BUILDER.comment("The chance of assault rifle.")
                .defineInRange("Villager Assault Rifle Chance", 0.2, 0.0, 1.0);
        GUARD_SNIPERS_RIFLE_CHANCE = BUILDER.comment("The chance of sniper's rifle.")
                .defineInRange("Villager Sniper's Rifle Chance", 0.05, 0.0, 1.0);
        GUARD_BAZOOKA_CHANCE = BUILDER.comment("The chance of bazooka.")
                .defineInRange("Villager Bazooka Chance", 0.05, 0.0, 1.0);
        GUARD_FLAMETHROWER_CHANCE = BUILDER.comment("The chance of flamethrower.")
                .defineInRange("Villager Flamethrower Chance", 0.05, 0.0, 1.0);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

}
