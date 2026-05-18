package com.scarasol.pillagers_gun.entity.goal.combat;

public record CombatIntent(CombatAction action) {
    public static final CombatIntent CONTINUE = new CombatIntent(CombatAction.CONTINUE);
    public static final CombatIntent RELOAD = new CombatIntent(CombatAction.RELOAD);
    public static final CombatIntent HOLD_FIRE = new CombatIntent(CombatAction.HOLD_FIRE);
}
