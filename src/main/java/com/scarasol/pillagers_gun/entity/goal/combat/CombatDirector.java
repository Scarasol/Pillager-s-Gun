package com.scarasol.pillagers_gun.entity.goal.combat;

public interface CombatDirector {
    CombatIntent selectIntent(CombatContext context);
}
