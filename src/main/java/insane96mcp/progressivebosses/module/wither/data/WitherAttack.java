package insane96mcp.progressivebosses.module.wither.data;

import com.google.gson.annotations.SerializedName;
import insane96mcp.progressivebosses.data.Difficulty;

import javax.annotation.Nullable;

public class WitherAttack {
    @SerializedName("skull_damage")
    public float skullDamage;
    @SerializedName("skull_speed_multiplier")
    public float skullSpeedMultiplier;
    @SerializedName("dangerous_skull_chance")
    public PoweredValue dangerousSkullChance;
    @SerializedName("attack_speed_near")
    public int attackSpeedNear;
    @SerializedName("attack_speed_far")
    public int attackSpeedFar;
    @SerializedName("side_heads_attack_speed_multiplier")
    public float sideHeadsAttackSpeedMultiplier;
    @SerializedName("effect_amplifier")
    public int effectAmplifier;
    @SerializedName("effect_duration")
    public Difficulty effectDuration;
    @SerializedName("attack_to_heal_threshold")
    public float attackToHealThreshold;
    @SerializedName("heal_on_skull_kill")
    public float healOnSkullKill;
    @SerializedName("charge")
    @Nullable
    public WitherCharge charge;
    @SerializedName("barrage")
    @Nullable
    public WitherBarrage barrage;

    private WitherAttack(float skullDamage, float skullSpeedMultiplier, PoweredValue dangerousSkullChance, int attackSpeedNear, int attackSpeedFar, float sideHeadsAttackSpeedMultiplier, int effectAmplifier, Difficulty effectDuration, float attackToHealThreshold, float healOnSkullKill, @Nullable WitherCharge charge, @Nullable WitherBarrage barrage) {
        this.skullDamage = skullDamage;
        this.skullSpeedMultiplier = skullSpeedMultiplier;
        this.dangerousSkullChance = dangerousSkullChance;
        this.attackSpeedNear = attackSpeedNear;
        this.attackSpeedFar = attackSpeedFar;
        this.sideHeadsAttackSpeedMultiplier = sideHeadsAttackSpeedMultiplier;
        this.effectAmplifier = effectAmplifier;
        this.effectDuration = effectDuration;
        this.attackToHealThreshold = attackToHealThreshold;
        this.healOnSkullKill = healOnSkullKill;
        this.charge = charge;
        this.barrage = barrage;
    }

    public static class Builder {
        private float skullDamage = 8f;
        private float skullSpeedMultiplier = 1.5f;
        private PoweredValue dangerousSkullChance = PoweredValue.of(0.4f, 0.3f);
        private int attackSpeedNear = 50;
        private int attackSpeedFar = 60;
        private float sideHeadsAttackSpeedMultiplier = 1.5f;
        private int effectAmplifier = 0;
        private Difficulty effectDuration = Difficulty.of(10, 10, 20);
        private float attackToHealThreshold = 0.1f;
        private float healOnSkullKill = 10;
        private WitherCharge charge = new WitherCharge.Builder().build();
        private WitherBarrage barrage = new WitherBarrage.Builder().build();

        public Builder skullDamage(float skullDamage) {
            this.skullDamage = skullDamage;
            return this;
        }

        public Builder skullSpeedMultiplier(float skullSpeedMultiplier) {
            this.skullSpeedMultiplier = skullSpeedMultiplier;
            return this;
        }

        public Builder dangerousSkullChance(PoweredValue dangerousSkullChance) {
            this.dangerousSkullChance = dangerousSkullChance;
            return this;
        }

        public Builder attackSpeedNear(int attackSpeedNear) {
            this.attackSpeedNear = attackSpeedNear;
            return this;
        }

        public Builder attackSpeedFar(int attackSpeedFar) {
            this.attackSpeedFar = attackSpeedFar;
            return this;
        }

        public Builder sideHeadsAttackSpeedMultiplier(float sideHeadsAttackSpeedMultiplier) {
            this.sideHeadsAttackSpeedMultiplier = sideHeadsAttackSpeedMultiplier;
            return this;
        }

        public Builder effectAmplifier(int effectAmplifier) {
            this.effectAmplifier = effectAmplifier;
            return this;
        }

        public Builder effectDuration(Difficulty effectDuration) {
            this.effectDuration = effectDuration;
            return this;
        }

        public Builder attackToHealThreshold(float attackToHealThreshold) {
            this.attackToHealThreshold = attackToHealThreshold;
            return this;
        }

        public Builder healOnSkullKill(float healOnSkullKill) {
            this.healOnSkullKill = healOnSkullKill;
            return this;
        }

        public Builder charge(@Nullable WitherCharge charge) {
            this.charge = charge;
            return this;
        }

        public Builder barrage(@Nullable WitherBarrage barrage) {
            this.barrage = barrage;
            return this;
        }

        public WitherAttack build() {
            return new WitherAttack(
                    skullDamage,
                    skullSpeedMultiplier,
                    dangerousSkullChance,
                    attackSpeedNear,
                    attackSpeedFar,
                    sideHeadsAttackSpeedMultiplier,
                    effectAmplifier,
                    effectDuration,
                    attackToHealThreshold,
                    healOnSkullKill,
                    charge,
                    barrage
            );
        }
    }

    public static class WitherCharge {
        @SerializedName("damage")
        public float damage = 8f;
        @SerializedName("base_time")
        public int baseTime = 60;
        @SerializedName("chance_on_hit")
        public PoweredValue chanceOnHit = PoweredValue.ZERO;
        @SerializedName("second_phase")
        public boolean secondPhase = false;
        @SerializedName("second_phase_times")
        public int secondPhaseTimes;
        @SerializedName("second_phase_tick_reduction")
        public int secondPhaseTickReduction;
        @SerializedName("second_phase_max_reduction")
        public int secondPhaseMaxReduction;

        public static class Builder {
            private final WitherCharge instance = new WitherCharge();

            public Builder damage(float damage) {
                instance.damage = damage;
                return this;
            }

            public Builder baseTime(int baseTime) {
                instance.baseTime = baseTime;
                return this;
            }

            public Builder chanceOnHit(PoweredValue chanceOnHit) {
                instance.chanceOnHit = chanceOnHit;
                return this;
            }

            public Builder secondPhase(boolean secondPhase) {
                instance.secondPhase = secondPhase;
                return this;
            }

            public Builder secondPhaseTimes(int secondPhaseTimes) {
                instance.secondPhaseTimes = secondPhaseTimes;
                return this;
            }

            public Builder secondPhaseTickReduction(int secondPhaseTickReduction) {
                instance.secondPhaseTickReduction = secondPhaseTickReduction;
                return this;
            }

            public Builder secondPhaseMaxReduction(int secondPhaseMaxReduction) {
                instance.secondPhaseMaxReduction = secondPhaseMaxReduction;
                return this;
            }

            public WitherCharge build() {
                return instance;
            }
        }
    }

    public static class WitherBarrage {
        @SerializedName("chance")
        public PoweredValue chance = PoweredValue.of(0.05f);
        @SerializedName("min_duration")
        public int minDuration = 40;
        @SerializedName("max_duration")
        public int maxDuration = 60;
        @SerializedName("attack_speed")
        public int attackSpeed = 5;

        public static class Builder {
            private final WitherBarrage instance = new WitherBarrage();

            public Builder chance(PoweredValue chance) {
                instance.chance = chance;
                return this;
            }

            public Builder minDuration(int minDuration) {
                instance.minDuration = minDuration;
                return this;
            }

            public Builder maxDuration(int maxDuration) {
                instance.maxDuration = maxDuration;
                return this;
            }

            public Builder attackSpeed(int attackSpeed) {
                instance.attackSpeed = attackSpeed;
                return this;
            }

            public WitherBarrage build() {
                return instance;
            }
        }
    }
}
