package com.scarasol.pillagers_gun.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Double> EQUIP_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<Double> DROP_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> BYPASS_INVULNERABLE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> FRIEND_FIRE;

    public static final ForgeConfigSpec.ConfigValue<Double> SHOTGUN_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<Integer> SHOTGUN_POWER;
    public static final ForgeConfigSpec.ConfigValue<Integer> SHOTGUN_COUNT;

    public static final ForgeConfigSpec.ConfigValue<Double> PISTOL_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<Integer> PISTOL_POWER;

    public static final ForgeConfigSpec.ConfigValue<Double> ASSAULT_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<Integer> ASSAULT_POWER;
    public static final ForgeConfigSpec.ConfigValue<Double> ASSAULT_BYPASS_RATE;

    static {
        EQUIP_CHANCE = BUILDER.comment("The chance of pillager armed with a gun.")
                .defineInRange("Armed Chance", 1.0, 0.0, 1.0);
        DROP_CHANCE = BUILDER.comment("The chance of pillagers will drop their guns on death.")
                .defineInRange("Drop Chance", 0, 0.0, 1.0);
        BYPASS_INVULNERABLE = BUILDER.comment("Whether bullets ignore damage immunity.")
                .define("Ignore Damage Immunity", false);
        FRIEND_FIRE = BUILDER.comment("Whether the gun has friend fire.")
                .define("Friend Fire", false);

        BUILDER.push("Shot Gun");
        SHOTGUN_CHANCE = BUILDER.comment("The chance of shotgun.")
                .defineInRange("Shotgun Chance", 0.2, 0.0, 1.0);
        SHOTGUN_POWER = BUILDER.comment("The power of each shotgun shell.")
                .defineInRange("Shotgun Power", 5, 0, 5000);
        SHOTGUN_COUNT = BUILDER.comment("The number of shotgun shells.")
                .defineInRange("Shells Number", 7, 0, 200);
        BUILDER.pop();

        BUILDER.push("Pistol");
        PISTOL_CHANCE = BUILDER.comment("The chance of pistol.")
                .defineInRange("Pistol Chance", 0.6, 0.0, 1.0);
        PISTOL_POWER = BUILDER.comment("The power of pistol bullet.")
                .defineInRange("Pistol Power", 9, 0, 5000);
        BUILDER.pop();

        BUILDER.push("Assault Rifle");
        ASSAULT_CHANCE = BUILDER.comment("The chance of assault rifle.")
                .defineInRange("Assault Rifle Chance", 0.2, 0.0, 1.0);
        ASSAULT_POWER = BUILDER.comment("The power of assault rifle bullet.")
                .defineInRange("Assault Rifle Power", 12, 0, 5000);
        ASSAULT_BYPASS_RATE = BUILDER.comment("The percentage of assault rifle bullet damage that ignore armor.")
                .defineInRange("Assault Rifle Bypass Armor Chance", 0.25, 0.0, 1.0);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

}
