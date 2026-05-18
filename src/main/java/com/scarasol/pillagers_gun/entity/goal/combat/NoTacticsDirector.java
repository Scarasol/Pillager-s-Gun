package com.scarasol.pillagers_gun.entity.goal.combat;

import com.scarasol.pillagers_gun.entity.goal.GunAttackGoal;

public class NoTacticsDirector implements CombatDirector {
    public static final NoTacticsDirector INSTANCE = new NoTacticsDirector();

    protected NoTacticsDirector() {
    }

    @Override
    public CombatIntent selectIntent(CombatContext context) {
        if (context.gunState() == GunAttackGoal.GunState.UNCHARGED
                && context.controller().canReload()
                && !context.stunned()
                && (!context.delayReloadForPositioning() || !context.controller().hasAmmo())) {
            return CombatIntent.RELOAD;
        }
        return CombatIntent.CONTINUE;
    }
}
