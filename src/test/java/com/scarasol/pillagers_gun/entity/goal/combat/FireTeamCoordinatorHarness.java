package com.scarasol.pillagers_gun.entity.goal.combat;

import com.scarasol.pillagers_gun.entity.goal.GunAttackGoal;
import com.scarasol.pillagers_gun.entity.goal.controller.GunController;
import com.scarasol.pillagers_gun.entity.goal.controller.GunReloadResult;
import com.scarasol.pillagers_gun.entity.goal.controller.GunRole;
import com.scarasol.pillagers_gun.entity.goal.controller.GunShotResult;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.UUID;

public final class FireTeamCoordinatorHarness {
    private static final String PREFIX = "com.scarasol.pillagers_gun.entity.goal.combat.FireTeamCoordinator$";
    private static final Class<?> FIRE_TEAM_CLASS;
    private static final Class<?> MEMBER_CLASS;
    private static final Class<?> TEAM_KEY_CLASS;
    private static final Constructor<FireTeamCoordinator> COORDINATOR_CONSTRUCTOR;
    private static final Constructor<?> FIRE_TEAM_CONSTRUCTOR;
    private static final Constructor<?> MEMBER_CONSTRUCTOR;
    private static final Constructor<?> TEAM_KEY_CONSTRUCTOR;

    static {
        try {
            FIRE_TEAM_CLASS = Class.forName(PREFIX + "FireTeam");
            MEMBER_CLASS = Class.forName(PREFIX + "FireTeamMember");
            TEAM_KEY_CLASS = Class.forName(PREFIX + "TeamKey");
            COORDINATOR_CONSTRUCTOR = FireTeamCoordinator.class.getDeclaredConstructor();
            COORDINATOR_CONSTRUCTOR.setAccessible(true);
            FIRE_TEAM_CONSTRUCTOR = FIRE_TEAM_CLASS.getDeclaredConstructor();
            FIRE_TEAM_CONSTRUCTOR.setAccessible(true);
            MEMBER_CONSTRUCTOR = MEMBER_CLASS.getDeclaredConstructor(UUID.class);
            MEMBER_CONSTRUCTOR.setAccessible(true);
            TEAM_KEY_CONSTRUCTOR = TEAM_KEY_CLASS.getDeclaredConstructor(ResourceKey.class, UUID.class);
            TEAM_KEY_CONSTRUCTOR.setAccessible(true);
        } catch (ReflectiveOperationException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private FireTeamCoordinatorHarness() {
    }

    public static void main(String[] args) throws Exception {
        run("empty reload bypasses tactical limits", FireTeamCoordinatorHarness::emptyReloadBypassesTacticalLimits);
        run("non-empty tactical reload is still constrained", FireTeamCoordinatorHarness::nonEmptyTacticalReloadIsConstrained);
        run("single member does not tactical reload early", FireTeamCoordinatorHarness::singleMemberDoesNotTacticalReloadEarly);
        run("single member ready to attack is not held", FireTeamCoordinatorHarness::singleMemberReadyToAttackIsNotHeld);
        run("vacant output slot can be acquired", FireTeamCoordinatorHarness::vacantOutputSlotCanBeAcquired);
        run("low power member cannot request takeover", FireTeamCoordinatorHarness::lowPowerMemberCannotRequestTakeover);
        run("low power output hands off to FIFO high power request", FireTeamCoordinatorHarness::lowPowerHandsOffToHighPowerRequest);
        run("empty reload ignores positioning delay", FireTeamCoordinatorHarness::emptyReloadIgnoresPositioningDelay);
        run("explicit remove releases output and exits team", FireTeamCoordinatorHarness::explicitRemoveReleasesOutputAndExitsTeam);
        run("movement profile combines forward and strafe", FireTeamCoordinatorHarness::movementProfileCombinesForwardAndStrafe);
        run("movement profile follows role preferences", FireTeamCoordinatorHarness::movementProfileFollowsRolePreferences);
        System.out.println("FireTeamCoordinatorHarness: all tests passed");
    }

    private static void emptyReloadBypassesTacticalLimits() throws Exception {
        Object team = newFireTeam();
        set(team, "tacticalMemberCount", 7);
        set(team, "reloadingCount", 3);

        CombatContext context = context(new FakeGunController(false, true, 0, 30, GunRole.RIFLE), GunAttackGoal.GunState.UNCHARGED, true, true, 100L);
        Object result = invoke(team, "canReload", new Class<?>[]{CombatContext.class, MEMBER_CLASS}, context, null);

        assertEquals(ReloadPermission.ALLOW, result, "empty reload must be allowed even when reload slots are full");
    }

    private static void nonEmptyTacticalReloadIsConstrained() throws Exception {
        Object team = newFireTeam();
        set(team, "tacticalMemberCount", 7);
        set(team, "reloadingCount", 3);
        set(team, "fireCapableCount", 4);
        set(team, "fireCapableContribution", 12.0D);
        set(team, "totalPower", 21.0D);

        CombatContext context = context(new FakeGunController(true, true, 5, 30, GunRole.RIFLE), GunAttackGoal.GunState.CHARGED, false, true, 100L);
        Object result = invoke(team, "canReload", new Class<?>[]{CombatContext.class, MEMBER_CLASS}, context, null);

        assertEquals(ReloadPermission.DELAY, result, "non-empty tactical reload should still obey max reloading members");
    }

    private static void singleMemberDoesNotTacticalReloadEarly() throws Exception {
        Object team = newFireTeam();
        Object member = member(UUID.randomUUID(), GunRole.RIFLE, 3.0D, 0.1D, TacticalFireState.HOLD_FIRE, 100L);
        set(team, "tacticalMemberCount", 1);
        set(member, "canReload", true);
        set(member, "hasAmmo", true);

        Object result = invoke(team, "shouldTacticalReload", new Class<?>[]{CombatContext.class, MEMBER_CLASS}, null, member);

        assertFalse((Boolean) result, "single member should not perform early tactical reload");
    }

    private static void singleMemberReadyToAttackIsNotHeld() throws Exception {
        Object team = newFireTeam();
        Object member = member(UUID.randomUUID(), GunRole.RIFLE, 3.0D, 1.0D, TacticalFireState.HOLD_FIRE, 100L);
        set(team, "tacticalMemberCount", 1);
        set(team, "totalPower", 3.0D);
        set(team, "fireCapableCount", 1);
        set(team, "fireCapableContribution", 3.0D);

        CombatContext context = context(new FakeGunController(true, true, 30, 30, GunRole.RIFLE), GunAttackGoal.GunState.READY_TO_ATTACK, false, true, 100L);
        Object result = invoke(team, "shouldHoldFire", new Class<?>[]{CombatContext.class, MEMBER_CLASS}, context, member);

        assertFalse((Boolean) result, "single member should not be held by cross-fire output control");
        assertEquals(TacticalFireState.HOLD_FIRE, get(member, "fireState"), "single member does not need to acquire FIRE_OUTPUT");
    }

    private static void vacantOutputSlotCanBeAcquired() throws Exception {
        Object team = newFireTeam();
        Object member = member(UUID.randomUUID(), GunRole.RIFLE, 3.0D, 1.0D, TacticalFireState.HOLD_FIRE, 100L);
        set(team, "tacticalMemberCount", 2);
        set(team, "totalPower", 6.0D);
        set(team, "fireCapableCount", 2);
        set(team, "fireCapableContribution", 6.0D);

        CombatContext context = context(new FakeGunController(true, true, 30, 30, GunRole.RIFLE), GunAttackGoal.GunState.READY_TO_ATTACK, false, true, 100L);
        Object result = invoke(team, "shouldHoldFire", new Class<?>[]{CombatContext.class, MEMBER_CLASS}, context, member);

        assertFalse((Boolean) result, "member should not hold fire after acquiring vacant output slot");
        assertEquals(TacticalFireState.FIRE_OUTPUT, get(member, "fireState"), "member should acquire FIRE_OUTPUT state");
        assertEquals(1, ((Number) get(team, "activeFireOutputCount")).intValue(), "active output count should increment");
    }

    private static void lowPowerMemberCannotRequestTakeover() throws Exception {
        Object team = newFireTeam();
        Object pistol = member(UUID.randomUUID(), GunRole.PISTOL, 1.0D, 1.0D, TacticalFireState.HOLD_FIRE, 100L);
        set(team, "tacticalMemberCount", 2);
        set(team, "totalPower", 6.0D);
        set(team, "activeFireOutputCount", 1);
        set(team, "activeFireOutputPower", 3.0D);

        CombatContext context = context(new FakeGunController(true, true, 8, 8, GunRole.PISTOL), GunAttackGoal.GunState.READY_TO_ATTACK, false, true, 100L);
        Object result = invoke(team, "shouldHoldFire", new Class<?>[]{CombatContext.class, MEMBER_CLASS}, context, pistol);

        assertTrue((Boolean) result, "low power member should keep holding when output is already covered");
        assertEquals(0, requestSet(team).size(), "low power member must not enqueue takeover request");
    }

    private static void lowPowerHandsOffToHighPowerRequest() throws Exception {
        Object team = newFireTeam();
        UUID pistolId = UUID.randomUUID();
        UUID rifleId = UUID.randomUUID();
        Object pistol = member(pistolId, GunRole.PISTOL, 1.0D, 1.0D, TacticalFireState.FIRE_OUTPUT, 100L);
        Object rifle = member(rifleId, GunRole.RIFLE, 3.0D, 1.0D, TacticalFireState.HOLD_FIRE, 100L);

        set(team, "tacticalMemberCount", 2);
        set(team, "totalPower", 4.0D);
        set(team, "fireCapableCount", 2);
        set(team, "fireCapableContribution", 4.0D);
        set(team, "activeFireOutputCount", 1);
        set(team, "activeFireOutputPower", 1.0D);
        members(team).put(pistolId, pistol);
        members(team).put(rifleId, rifle);
        requestSet(team).add(rifleId);

        CombatContext context = context(new FakeGunController(true, true, 8, 8, GunRole.PISTOL), GunAttackGoal.GunState.READY_TO_ATTACK, false, true, 100L);
        Object result = invoke(team, "shouldHoldFire", new Class<?>[]{CombatContext.class, MEMBER_CLASS}, context, pistol);

        assertTrue((Boolean) result, "low power holder should hold fire after handing off");
        assertEquals(TacticalFireState.HOLD_FIRE, get(pistol, "fireState"), "low power holder should release output state");
        assertEquals(TacticalFireState.FIRE_OUTPUT, get(rifle, "fireState"), "high power requester should receive output state");
        assertEquals(1, ((Number) get(team, "activeFireOutputCount")).intValue(), "handoff should preserve one active output slot");
        assertClose(3.0D, ((Number) get(team, "activeFireOutputPower")).doubleValue(), "handoff should replace pistol power with rifle power");
        assertEquals(0, requestSet(team).size(), "fulfilled FIFO request should be removed");
    }

    private static void emptyReloadIgnoresPositioningDelay() throws Exception {
        FakeGunController controller = new FakeGunController(false, true, 0, 30, GunRole.RIFLE);
        CombatContext context = context(controller, GunAttackGoal.GunState.UNCHARGED, true, true, 100L);

        CombatIntent noTacticsIntent = NoTacticsDirector.INSTANCE.selectIntent(context);
        assertEquals(CombatAction.RELOAD, noTacticsIntent.action(), "NoTactics should allow empty reload despite positioning delay");

        Method method = CrossFireDirector.class.getDeclaredMethod("shouldRequestReload", CombatContext.class);
        method.setAccessible(true);
        Object shouldRequestReload = method.invoke(CrossFireDirector.INSTANCE, context);
        assertTrue((Boolean) shouldRequestReload, "CrossFire should request empty reload despite positioning delay");
    }

    private static void explicitRemoveReleasesOutputAndExitsTeam() throws Exception {
        FireTeamCoordinator coordinator = newCoordinator();
        UUID mobId = UUID.randomUUID();
        Object teamKey = teamKey(UUID.randomUUID());
        Object team = newFireTeam();
        Object member = member(mobId, GunRole.RIFLE, 3.0D, 1.0D, TacticalFireState.FIRE_OUTPUT, 100L);

        set(team, "tacticalMemberCount", 1);
        set(team, "totalPower", 3.0D);
        set(team, "fireCapableCount", 1);
        set(team, "fireCapableContribution", 3.0D);
        set(team, "activeFireOutputCount", 1);
        set(team, "activeFireOutputPower", 3.0D);
        ((int[]) get(team, "bucketCounts"))[4] = 1;
        ((double[]) get(team, "bucketPower"))[4] = 3.0D;
        members(team).put(mobId, member);
        teams(coordinator).put(teamKey, team);
        memberTeams(coordinator).put(mobId, teamKey);

        coordinator.remove(mobId);

        assertEquals(0, members(team).size(), "removed mob should leave FireTeam members");
        assertFalse(memberTeams(coordinator).containsKey(mobId), "reverse membership should be cleared");
        assertFalse(teams(coordinator).containsKey(teamKey), "empty FireTeam should be removed");
        assertEquals(0, ((Number) get(team, "activeFireOutputCount")).intValue(), "output count should be released immediately");
        assertClose(0.0D, ((Number) get(team, "activeFireOutputPower")).doubleValue(), "output power should be released immediately");
    }

    private static void movementProfileCombinesForwardAndStrafe() {
        CombatMovementProfile rifle = CombatMovementProfile.forRole(GunRole.RIFLE);
        float forward = rifle.forward(0.95D);
        float strafe = rifle.strafe(true, true);

        assertTrue(forward > 0.0F, "far rifle should press forward");
        assertTrue(strafe > 0.0F, "rifle should strafe when target is looking at it");
        assertTrue(rifle.shouldMove(forward, strafe), "combined movement should be active");
    }

    private static void movementProfileFollowsRolePreferences() {
        CombatMovementProfile rifle = CombatMovementProfile.forRole(GunRole.RIFLE);
        CombatMovementProfile shotgun = CombatMovementProfile.forRole(GunRole.SHOTGUN);
        CombatMovementProfile sniper = CombatMovementProfile.forRole(GunRole.SNIPER);

        assertTrue(Math.abs(rifle.strafe(true, true)) > Math.abs(sniper.strafe(true, true)), "rifle should strafe more than sniper");
        assertTrue(shotgun.forward(0.75D) > rifle.forward(0.75D), "shotgun should press harder at mid range");
        assertTrue(CombatMovementProfile.forRole(GunRole.PISTOL).forwardWeight() > shotgun.forwardWeight(), "pistol should have higher forward/back speed weight than shotgun");
        assertClose(0.0D, rifle.strafe(false, true), "rifle should not strafe when target is not looking at it");
        assertClose(30.0D, rifle.targetLookAngleDegrees(0.0D), "close target look angle should be narrow");
        assertClose(5.0D, rifle.targetLookAngleDegrees(1.0D), "far target look angle should be very narrow");
    }

    private static FireTeamCoordinator newCoordinator() throws ReflectiveOperationException {
        return COORDINATOR_CONSTRUCTOR.newInstance();
    }

    private static Object newFireTeam() throws ReflectiveOperationException {
        return FIRE_TEAM_CONSTRUCTOR.newInstance();
    }

    private static Object teamKey(UUID targetId) throws ReflectiveOperationException {
        return TEAM_KEY_CONSTRUCTOR.newInstance(null, targetId);
    }

    private static Object member(UUID mobId, GunRole role, double suppressivePower, double fireHealth, TacticalFireState fireState, long gameTime) throws ReflectiveOperationException {
        Object member = MEMBER_CONSTRUCTOR.newInstance(mobId);
        set(member, "lastSeenTick", gameTime);
        set(member, "registered", true);
        set(member, "stunned", false);
        set(member, "reloading", false);
        set(member, "hasAmmo", true);
        set(member, "canReload", true);
        set(member, "canProvideFire", true);
        set(member, "role", role);
        set(member, "suppressivePower", suppressivePower);
        set(member, "fireHealth", fireHealth);
        set(member, "effectiveContribution", suppressivePower * fireHealth);
        set(member, "fireState", fireState);
        set(member, "fireOutputStartTick", fireState == TacticalFireState.FIRE_OUTPUT ? gameTime - 5L : Long.MIN_VALUE);
        set(member, "nextFireEligibleTick", 0L);
        set(member, "nextOutputRequestTick", 0L);
        return member;
    }

    private static CombatContext context(GunController controller, GunAttackGoal.GunState gunState, boolean delayReloadForPositioning, boolean targetValid, long gameTime) {
        return new CombatContext(
                null,
                null,
                controller,
                gunState,
                targetValid,
                true,
                false,
                delayReloadForPositioning,
                10.0D,
                32.0D,
                10,
                0,
                controller.getAmmoCount(),
                gameTime
        );
    }

    private static Object invoke(Object target, String name, Class<?>[] parameterTypes, Object... args) throws ReflectiveOperationException {
        Method method = target.getClass().getDeclaredMethod(name, parameterTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private static void set(Object target, String name, Object value) throws ReflectiveOperationException {
        Field field = field(target.getClass(), name);
        field.set(target, value);
    }

    private static Object get(Object target, String name) throws ReflectiveOperationException {
        Field field = field(target.getClass(), name);
        return field.get(target);
    }

    private static Field field(Class<?> type, String name) throws ReflectiveOperationException {
        Class<?> current = type;
        while (current != null) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    @SuppressWarnings("unchecked")
    private static Map<UUID, Object> members(Object team) throws ReflectiveOperationException {
        return (Map<UUID, Object>) get(team, "members");
    }

    @SuppressWarnings("unchecked")
    private static LinkedHashSet<UUID> requestSet(Object team) throws ReflectiveOperationException {
        return (LinkedHashSet<UUID>) get(team, "highPowerOutputRequests");
    }

    @SuppressWarnings("unchecked")
    private static Map<Object, Object> teams(FireTeamCoordinator coordinator) throws ReflectiveOperationException {
        return (Map<Object, Object>) get(coordinator, "teams");
    }

    @SuppressWarnings("unchecked")
    private static Map<UUID, Object> memberTeams(FireTeamCoordinator coordinator) throws ReflectiveOperationException {
        return (Map<UUID, Object>) get(coordinator, "memberTeams");
    }

    private static void run(String name, ThrowingRunnable runnable) throws Exception {
        try {
            runnable.run();
            System.out.println("[PASS] " + name);
        } catch (Throwable throwable) {
            System.err.println("[FAIL] " + name);
            throw throwable;
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if ((expected == null && actual != null) || (expected != null && !expected.equals(actual))) {
            throw new AssertionError(message + " expected=" + expected + " actual=" + actual);
        }
    }

    private static void assertClose(double expected, double actual, String message) {
        if (Math.abs(expected - actual) > 1.0E-6D) {
            throw new AssertionError(message + " expected=" + expected + " actual=" + actual);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static final class FakeGunController implements GunController {
        private final boolean hasAmmo;
        private final boolean canReload;
        private final int ammoCount;
        private final int maxAmmoCount;
        private final GunRole role;

        private FakeGunController(boolean hasAmmo, boolean canReload, int ammoCount, int maxAmmoCount, GunRole role) {
            this.hasAmmo = hasAmmo;
            this.canReload = canReload;
            this.ammoCount = ammoCount;
            this.maxAmmoCount = maxAmmoCount;
            this.role = role;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public boolean hasAmmo() {
            return this.hasAmmo;
        }

        @Override
        public boolean canReload() {
            return this.canReload;
        }

        @Override
        public int getAmmoCount() {
            return this.ammoCount;
        }

        @Override
        public int getMaxAmmoCount() {
            return this.maxAmmoCount;
        }

        @Override
        public GunRole getRole() {
            return this.role;
        }

        @Override
        public double getSuppressivePower() {
            return this.role.getSuppressivePower();
        }

        @Override
        public double getAmmoUsePerTick(double distanceToTarget) {
            return 0.5D;
        }

        @Override
        public int getReloadDurationTicks() {
            return 60;
        }

        @Override
        public int getReadyDelayAfterAmmoFound() {
            return 1;
        }

        @Override
        public int getReadyDelayAfterReload() {
            return 1;
        }

        @Override
        public void startReload() {
        }

        @Override
        public GunReloadResult tickReloading() {
            return GunReloadResult.RELOADING;
        }

        @Override
        public GunShotResult shoot(LivingEntity target, boolean isStunned, Vec3 lastTargetPosition, int ammoCount) {
            return GunShotResult.noShot(ammoCount);
        }
    }
}
