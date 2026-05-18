package com.scarasol.pillagers_gun.entity.goal.combat;

import com.scarasol.pillagers_gun.entity.goal.GunAttackGoal;
import com.scarasol.pillagers_gun.entity.goal.controller.GunRole;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.UUID;

public class FireTeamCoordinator {
    public static final FireTeamCoordinator INSTANCE = new FireTeamCoordinator();

    private static final int BUCKET_COUNT = 5;
    private static final int MEMBER_TTL_TICKS = 40;
    private static final int TEAM_CLEANUP_INTERVAL_TICKS = 100;
    private static final int MEMBER_UPDATE_INTERVAL_TICKS = 8;
    private static final int FIRE_HEALTH_SAFETY_BUFFER_TICKS = 12;
    private static final int MAX_FIRE_OUTPUT_TICKS = 100;
    private static final int FIRE_REJOIN_COOLDOWN_TICKS = 15;
    private static final int OUTPUT_REQUEST_COOLDOWN_TICKS = 15;
    private static final int OUTPUT_REQUEST_MAX_STALE_TICKS = 20;
    private static final double RELOAD_HEALTH_THRESHOLD = 0.25D;
    private static final double HIGH_POWER_THRESHOLD = 2.0D;
    private static final double HIGH_POWER_REQUEST_MIN_HEALTH = 0.4D;
    private static final double EPSILON = 1.0E-6D;

    private final Map<TeamKey, FireTeam> teams = new HashMap<>();
    private long lastCleanupTick;

    private FireTeamCoordinator() {
    }

    public void update(CombatContext context) {
        FireTeam fireTeam = getFireTeam(context);
        if (fireTeam == null) {
            return;
        }
        fireTeam.update(context, false);
        cleanupTeams(context.gameTime());
    }

    public boolean shouldTacticalReload(CombatContext context) {
        FireTeam fireTeam = getFireTeam(context);
        if (fireTeam == null) {
            return false;
        }

        FireTeamMember member = fireTeam.update(context, true);
        boolean shouldReload = fireTeam.shouldTacticalReload(context, member);
        cleanupTeams(context.gameTime());
        return shouldReload;
    }

    public ReloadPermission requestReload(CombatContext context) {
        FireTeam fireTeam = getFireTeam(context);
        if (fireTeam == null) {
            return ReloadPermission.ALLOW;
        }

        FireTeamMember member = fireTeam.update(context, true);
        ReloadPermission permission = fireTeam.canReload(context, member);
        if (permission == ReloadPermission.ALLOW) {
            fireTeam.reserveReload(member, context.gameTime());
        }
        cleanupTeams(context.gameTime());
        return permission;
    }

    public boolean shouldHoldFire(CombatContext context) {
        FireTeam fireTeam = getFireTeam(context);
        if (fireTeam == null) {
            return false;
        }

        FireTeamMember member = fireTeam.update(context, false);
        boolean shouldHoldFire = fireTeam.shouldHoldFire(context, member);
        cleanupTeams(context.gameTime());
        return shouldHoldFire;
    }

    private FireTeam getFireTeam(CombatContext context) {
        LivingEntity target = context.target();
        if (!context.targetValid() || target == null) {
            return null;
        }
        TeamKey teamKey = new TeamKey(context.mob().level().dimension(), target.getUUID());
        return this.teams.computeIfAbsent(teamKey, key -> new FireTeam());
    }

    private void cleanupTeams(long gameTime) {
        if (gameTime - this.lastCleanupTick < TEAM_CLEANUP_INTERVAL_TICKS) {
            return;
        }
        this.lastCleanupTick = gameTime;

        Iterator<Map.Entry<TeamKey, FireTeam>> iterator = this.teams.entrySet().iterator();
        while (iterator.hasNext()) {
            FireTeam fireTeam = iterator.next().getValue();
            fireTeam.removeExpired(gameTime);
            if (fireTeam.members.isEmpty()) {
                iterator.remove();
            }
        }
    }

    private record TeamKey(ResourceKey<Level> dimension, UUID targetId) {
    }

    private static class FireTeam {
        private final Map<UUID, FireTeamMember> members = new HashMap<>();
        private final int[] bucketCounts = new int[BUCKET_COUNT];
        private final double[] bucketPower = new double[BUCKET_COUNT];
        private final LinkedHashSet<UUID> highPowerOutputRequests = new LinkedHashSet<>();
        private int tacticalMemberCount;
        private int reloadingCount;
        private int fireCapableCount;
        private int activeFireOutputCount;
        private double totalPower;
        private double fireCapableContribution;
        private double activeFireOutputPower;

        private FireTeamMember update(CombatContext context, boolean force) {
            Mob mob = context.mob();
            UUID mobId = mob.getUUID();
            FireTeamMember member = this.members.computeIfAbsent(mobId, FireTeamMember::new);
            member.lastSeenTick = context.gameTime();

            if (!force && !shouldRefresh(context, member)) {
                return member;
            }

            boolean wasFireOutput = member.fireState == TacticalFireState.FIRE_OUTPUT;
            if (member.registered) {
                removeStats(member);
            }

            member.stunned = context.stunned();
            member.reloading = context.gunState() == GunAttackGoal.GunState.CHARGING;
            member.hasAmmo = context.controller().hasAmmo();
            member.canReload = !context.stunned() && context.controller().canReload();
            member.canProvideFire = canProvideFire(context);
            member.ammoCount = context.controller().getAmmoCount();
            member.maxAmmoCount = context.controller().getMaxAmmoCount();
            member.reloadDurationTicks = Math.max(1, context.controller().getReloadDurationTicks());
            member.ammoUsePerTick = context.stunned() ? 0.0D : Math.max(0.0D, context.controller().getAmmoUsePerTick(context.distanceToTarget()));
            member.role = context.controller().getRole();
            member.suppressivePower = Math.max(0.0D, context.controller().getSuppressivePower());
            member.fireHealth = calculateFireHealth(member);
            member.bucket = toBucket(member.fireHealth);
            member.effectiveContribution = canProvideSustainedFire(member) ? member.suppressivePower * member.fireHealth : 0.0D;

            if (wasFireOutput && !canProvideSustainedFire(member)) {
                clearFireOutputState(member, context.gameTime());
            }
            if (!isValidHighPowerRequest(member, context.gameTime())) {
                this.highPowerOutputRequests.remove(member.mobId);
            }

            member.lastRefreshTick = context.gameTime();
            member.registered = true;
            addStats(member);
            return member;
        }

        private boolean shouldRefresh(CombatContext context, FireTeamMember member) {
            if (!member.registered) {
                return true;
            }
            if (context.gameTime() == member.lastRefreshTick) {
                return false;
            }

            boolean stunned = context.stunned();
            boolean reloading = context.gunState() == GunAttackGoal.GunState.CHARGING;
            boolean hasAmmo = context.controller().hasAmmo();
            boolean canReload = !stunned && context.controller().canReload();
            boolean canProvideFire = canProvideFire(context);
            if (member.stunned != stunned
                    || member.reloading != reloading
                    || member.hasAmmo != hasAmmo
                    || member.canReload != canReload
                    || member.canProvideFire != canProvideFire) {
                return true;
            }

            return Math.floorMod(context.gameTime() + member.updatePhase, MEMBER_UPDATE_INTERVAL_TICKS) == 0;
        }

        private void addStats(FireTeamMember member) {
            if (member.stunned) {
                return;
            }

            this.tacticalMemberCount++;
            this.totalPower += member.suppressivePower;
            this.bucketCounts[member.bucket]++;
            this.bucketPower[member.bucket] += getBucketPower(member);
            if (member.reloading) {
                this.reloadingCount++;
            }
            if (canProvideSustainedFire(member)) {
                this.fireCapableCount++;
                this.fireCapableContribution += member.effectiveContribution;
                if (member.fireState == TacticalFireState.FIRE_OUTPUT) {
                    this.activeFireOutputCount++;
                    this.activeFireOutputPower += member.effectiveContribution;
                }
            }
        }

        private void removeStats(FireTeamMember member) {
            if (member.stunned) {
                return;
            }

            this.tacticalMemberCount--;
            this.totalPower -= member.suppressivePower;
            this.bucketCounts[member.bucket]--;
            this.bucketPower[member.bucket] -= getBucketPower(member);
            if (member.reloading) {
                this.reloadingCount--;
            }
            if (canProvideSustainedFire(member)) {
                this.fireCapableCount--;
                this.fireCapableContribution -= member.effectiveContribution;
                if (member.fireState == TacticalFireState.FIRE_OUTPUT) {
                    this.activeFireOutputCount--;
                    this.activeFireOutputPower -= member.effectiveContribution;
                }
            }
        }

        private boolean shouldTacticalReload(CombatContext context, FireTeamMember member) {
            return member != null
                    && this.tacticalMemberCount > 1
                    && !member.stunned
                    && member.canReload
                    && member.role != GunRole.EXPLOSIVE
                    && !member.reloading
                    && member.hasAmmo
                    && member.fireHealth <= RELOAD_HEALTH_THRESHOLD;
        }

        private ReloadPermission canReload(CombatContext context, FireTeamMember requester) {
            if (context.stunned()) {
                return ReloadPermission.DELAY;
            }

            if (!context.controller().canReload()) {
                return ReloadPermission.DELAY;
            }

            if (!context.controller().hasAmmo()) {
                return ReloadPermission.ALLOW;
            }

            int memberCount = this.tacticalMemberCount;
            if (memberCount <= 1) {
                return ReloadPermission.DELAY;
            }

            int availableShooters = this.fireCapableCount;
            double availableContribution = this.fireCapableContribution;
            int activeReloadingCount = this.reloadingCount;

            if (requester != null) {
                if (canProvideSustainedFire(requester)) {
                    availableShooters--;
                    availableContribution -= requester.effectiveContribution;
                }
                if (requester.reloading) {
                    activeReloadingCount--;
                }
            }

            int requiredShooters = getRequiredShooters(memberCount);
            int maxReloadingMembers = Math.max(1, memberCount - requiredShooters);

            if (activeReloadingCount >= maxReloadingMembers) {
                return ReloadPermission.DELAY;
            }

            double requiredPower = getRequiredPower(memberCount, this.totalPower);
            boolean enoughShooters = availableShooters >= requiredShooters;
            boolean enoughPower = availableContribution + EPSILON >= requiredPower;
            return enoughShooters || enoughPower ? ReloadPermission.ALLOW : ReloadPermission.DELAY;
        }

        private void reserveReload(FireTeamMember member, long gameTime) {
            if (member == null || !member.registered) {
                return;
            }

            removeStats(member);
            clearFireOutputState(member, gameTime);
            this.highPowerOutputRequests.remove(member.mobId);
            member.reloading = true;
            member.canProvideFire = false;
            member.effectiveContribution = 0.0D;
            addStats(member);
        }

        private boolean shouldHoldFire(CombatContext context, FireTeamMember member) {
            if (member == null || member.role == GunRole.EXPLOSIVE) {
                return false;
            }

            long gameTime = context.gameTime();
            if (!canProvideSustainedFire(member)) {
                if (member.fireState == TacticalFireState.FIRE_OUTPUT) {
                    releaseFireOutput(member, gameTime);
                }
                this.highPowerOutputRequests.remove(member.mobId);
                return false;
            }

            if (getRequiredShooters(this.tacticalMemberCount) <= 0) {
                if (member.fireState == TacticalFireState.FIRE_OUTPUT) {
                    releaseFireOutput(member, gameTime);
                }
                this.highPowerOutputRequests.remove(member.mobId);
                return false;
            }

            if (context.gunState() != GunAttackGoal.GunState.READY_TO_ATTACK) {
                if (member.fireState == TacticalFireState.HOLD_FIRE && !hasOutputVacancy()) {
                    registerHighPowerOutputRequest(member, gameTime);
                }
                return false;
            }

            if (member.fireState == TacticalFireState.FIRE_OUTPUT) {
                if (shouldYieldToHighPowerRequest(member)) {
                    FireTeamMember requester = pollValidHighPowerRequest(gameTime);
                    if (requester != null) {
                        handoffFireOutput(member, requester, gameTime);
                        return true;
                    }
                }
                if (shouldRotateFireOutput(member, gameTime)) {
                    releaseFireOutput(member, gameTime);
                    return true;
                }
                return false;
            }

            if (gameTime < member.nextFireEligibleTick) {
                return true;
            }

            if (hasOutputVacancy()) {
                acquireFireOutput(member, gameTime);
                return false;
            }

            registerHighPowerOutputRequest(member, gameTime);
            return true;
        }

        private void acquireFireOutput(FireTeamMember member, long gameTime) {
            if (member == null
                    || member.fireState == TacticalFireState.FIRE_OUTPUT
                    || !canProvideSustainedFire(member)) {
                return;
            }

            this.highPowerOutputRequests.remove(member.mobId);
            member.fireState = TacticalFireState.FIRE_OUTPUT;
            member.fireOutputStartTick = gameTime;
            member.nextFireEligibleTick = 0L;
            if (member.registered) {
                this.activeFireOutputCount++;
                this.activeFireOutputPower += member.effectiveContribution;
            }
        }

        private void releaseFireOutput(FireTeamMember member, long gameTime) {
            if (member == null || member.fireState != TacticalFireState.FIRE_OUTPUT) {
                return;
            }

            if (member.registered && canProvideSustainedFire(member)) {
                this.activeFireOutputCount--;
                this.activeFireOutputPower -= member.effectiveContribution;
            }
            clearFireOutputState(member, gameTime);
        }

        private void handoffFireOutput(FireTeamMember holder, FireTeamMember requester, long gameTime) {
            releaseFireOutput(holder, gameTime);
            acquireFireOutput(requester, gameTime);
        }

        private void clearFireOutputState(FireTeamMember member, long gameTime) {
            member.fireState = TacticalFireState.HOLD_FIRE;
            member.fireOutputStartTick = Long.MIN_VALUE;
            member.nextFireEligibleTick = gameTime + FIRE_REJOIN_COOLDOWN_TICKS;
        }

        private boolean hasOutputVacancy() {
            int requiredShooters = getRequiredShooters(this.tacticalMemberCount);
            if (requiredShooters <= 0) {
                return false;
            }

            double requiredPower = getRequiredPower(this.tacticalMemberCount, this.totalPower);
            return this.activeFireOutputCount < requiredShooters
                    || this.activeFireOutputPower + EPSILON < requiredPower;
        }

        private boolean shouldYieldToHighPowerRequest(FireTeamMember holder) {
            return !isHighPowerMember(holder) && !this.highPowerOutputRequests.isEmpty();
        }

        private boolean shouldRotateFireOutput(FireTeamMember member, long gameTime) {
            return member.fireOutputStartTick != Long.MIN_VALUE
                    && gameTime - member.fireOutputStartTick >= MAX_FIRE_OUTPUT_TICKS
                    && this.fireCapableCount > this.activeFireOutputCount;
        }

        private void registerHighPowerOutputRequest(FireTeamMember member, long gameTime) {
            if (!canRequestHighPowerOutput(member, gameTime)) {
                return;
            }
            if (this.highPowerOutputRequests.add(member.mobId)) {
                member.nextOutputRequestTick = gameTime + OUTPUT_REQUEST_COOLDOWN_TICKS;
            }
        }

        private FireTeamMember pollValidHighPowerRequest(long gameTime) {
            Iterator<UUID> iterator = this.highPowerOutputRequests.iterator();
            while (iterator.hasNext()) {
                UUID requesterId = iterator.next();
                iterator.remove();
                FireTeamMember requester = this.members.get(requesterId);
                if (isValidHighPowerRequest(requester, gameTime)) {
                    return requester;
                }
            }
            return null;
        }

        private boolean canRequestHighPowerOutput(FireTeamMember member, long gameTime) {
            return member.fireState == TacticalFireState.HOLD_FIRE
                    && canProvideSustainedFire(member)
                    && isHighPowerMember(member)
                    && member.fireHealth >= HIGH_POWER_REQUEST_MIN_HEALTH
                    && gameTime >= member.nextFireEligibleTick
                    && gameTime >= member.nextOutputRequestTick;
        }

        private boolean isValidHighPowerRequest(FireTeamMember member, long gameTime) {
            return member != null
                    && member.registered
                    && gameTime - member.lastSeenTick <= OUTPUT_REQUEST_MAX_STALE_TICKS
                    && member.fireState == TacticalFireState.HOLD_FIRE
                    && canProvideSustainedFire(member)
                    && isHighPowerMember(member)
                    && member.fireHealth >= HIGH_POWER_REQUEST_MIN_HEALTH
                    && gameTime >= member.nextFireEligibleTick;
        }

        private void removeExpired(long gameTime) {
            Iterator<FireTeamMember> iterator = this.members.values().iterator();
            while (iterator.hasNext()) {
                FireTeamMember member = iterator.next();
                if (gameTime - member.lastSeenTick <= MEMBER_TTL_TICKS) {
                    continue;
                }
                if (member.registered) {
                    removeStats(member);
                }
                this.highPowerOutputRequests.remove(member.mobId);
                iterator.remove();
            }
        }

        private static boolean canProvideFire(CombatContext context) {
            return context.targetValid()
                    && context.hasLineOfSight()
                    && !context.stunned()
                    && context.gunState() != GunAttackGoal.GunState.CHARGING
                    && context.controller().hasAmmo();
        }

        private static boolean canProvideSustainedFire(FireTeamMember member) {
            return member.canProvideFire && member.role != GunRole.EXPLOSIVE;
        }

        private static boolean isHighPowerMember(FireTeamMember member) {
            return member.suppressivePower >= HIGH_POWER_THRESHOLD;
        }

        private static double getBucketPower(FireTeamMember member) {
            return member.suppressivePower * member.fireHealth;
        }

        private static double calculateFireHealth(FireTeamMember member) {
            if (member.stunned) {
                return 0.0D;
            }
            if (member.ammoCount < 0) {
                return 1.0D;
            }
            if (member.ammoCount <= 0) {
                return 0.0D;
            }

            double coverageWindow = member.reloadDurationTicks + FIRE_HEALTH_SAFETY_BUFFER_TICKS;
            double timeDenominator = member.ammoUsePerTick * coverageWindow;
            double timeHealth = timeDenominator <= EPSILON ? 1.0D : member.ammoCount / timeDenominator;
            double magazineHealth = member.maxAmmoCount <= 0 ? 1.0D : member.ammoCount / (double) member.maxAmmoCount;
            return clamp(Math.min(timeHealth, magazineHealth), 0.0D, 1.0D);
        }

        private static int toBucket(double fireHealth) {
            return Math.min(BUCKET_COUNT - 1, Math.max(0, (int) Math.floor(fireHealth * BUCKET_COUNT)));
        }

        private static double clamp(double value, double min, double max) {
            return Math.max(min, Math.min(max, value));
        }

        private static int getRequiredShooters(int memberCount) {
            if (memberCount <= 1) {
                return 0;
            }
            if (memberCount == 2) {
                return 1;
            }
            if (memberCount <= 4) {
                return 2;
            }
            return Math.max(2, (int) Math.ceil(memberCount * 0.45D));
        }

        private static double getRequiredPower(int memberCount, double totalPower) {
            if (memberCount == 2) {
                return 1.0D;
            }
            if (memberCount <= 4) {
                return 3.0D;
            }
            return Math.max(3.0D, totalPower * 0.45D);
        }

        @Override
        public String toString() {
            return "FireTeam{" +
                    "tacticalMemberCount=" + tacticalMemberCount +
                    ", reloadingCount=" + reloadingCount +
                    ", fireCapableCount=" + fireCapableCount +
                    ", activeFireOutputCount=" + activeFireOutputCount +
                    ", activeFireOutputPower=" + activeFireOutputPower +
                    ", buckets=" + Arrays.toString(bucketCounts) +
                    ", queuedRequests=" + highPowerOutputRequests.size() +
                    '}';
        }
    }

    private static class FireTeamMember {
        private final UUID mobId;
        private final int updatePhase;
        private long lastSeenTick;
        private long lastRefreshTick = Long.MIN_VALUE;
        private boolean registered;
        private boolean stunned;
        private boolean reloading;
        private boolean hasAmmo;
        private boolean canReload;
        private boolean canProvideFire;
        private int ammoCount;
        private int maxAmmoCount;
        private int reloadDurationTicks = 60;
        private int bucket = BUCKET_COUNT - 1;
        private long fireOutputStartTick = Long.MIN_VALUE;
        private long nextFireEligibleTick;
        private long nextOutputRequestTick;
        private double ammoUsePerTick;
        private GunRole role = GunRole.OTHER;
        private TacticalFireState fireState = TacticalFireState.HOLD_FIRE;
        private double suppressivePower = GunRole.OTHER.getSuppressivePower();
        private double fireHealth = 1.0D;
        private double effectiveContribution;

        private FireTeamMember(UUID mobId) {
            this.mobId = mobId;
            this.updatePhase = Math.floorMod(mobId.hashCode(), MEMBER_UPDATE_INTERVAL_TICKS);
        }
    }
}
