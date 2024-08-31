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
        @SerializedName("chance_on_hit")
        @Nullable
        public PoweredValue chanceOnHit;
        @SerializedName("damage")
        public float damage;
        @SerializedName("base_time")
        public int time;
        @SerializedName("second_phase")
        public boolean secondPhase;
        @SerializedName("second_phase_times")
        public int secondPhaseTimes;
        @SerializedName("second_phase_tick_reduction")
        public int secondPhaseTickReduction;
        @SerializedName("second_phase_max_reduction")
        public int secondPhaseMaxReduction;

        private WitherCharge(@Nullable PoweredValue chanceOnHit, float damage, @Nullable int time, boolean secondPhase, int secondPhaseTimes, int secondPhaseTickReduction, int secondPhaseMaxReduction) {
            this.chanceOnHit = chanceOnHit;
            this.damage = damage;
            this.time = time;
            this.secondPhase = secondPhase;
            this.secondPhaseTimes = secondPhaseTimes;
            this.secondPhaseTickReduction = secondPhaseTickReduction;
            this.secondPhaseMaxReduction = secondPhaseMaxReduction;
        }

        public static class Builder {
            private PoweredValue chanceOnHit = PoweredValue.of(0.06f, 0.12f);
            private float damage = 8;
            private int time = 70;
            private boolean secondPhase = true;
            private int secondPhaseTimes = 3;
            private int secondPhaseTickReduction = 8;
            private int secondPhaseMaxReduction = 16;

            public Builder chanceOnHit(PoweredValue chanceOnHit) {
                this.chanceOnHit = chanceOnHit;
                return this;
            }

            public Builder damage(float damage) {
                this.damage = damage;
                return this;
            }

            public Builder time(int time) {
                this.time = time;
                return this;
            }

            public Builder secondPhase(boolean secondPhase) {
                this.secondPhase = secondPhase;
                return this;
            }

            public Builder secondPhaseTimes(int secondPhaseTimes) {
                this.secondPhaseTimes = secondPhaseTimes;
                return this;
            }

            public Builder secondPhaseTickReduction(int secondPhaseTickReduction) {
                this.secondPhaseTickReduction = secondPhaseTickReduction;
                return this;
            }

            public Builder secondPhaseMaxReduction(int secondPhaseMaxReduction) {
                this.secondPhaseMaxReduction = secondPhaseMaxReduction;
                return this;
            }

            public WitherCharge build() {
                return new WitherCharge(chanceOnHit, damage, time, secondPhase, secondPhaseTimes, secondPhaseTickReduction, secondPhaseMaxReduction);
            }
        }
    }

    public static class WitherBarrage {
        @SerializedName("chance")
        public PoweredValue chance;
        @SerializedName("min_duration")
        public int minDuration;
        @SerializedName("max_duration")
        public int maxDuration;
        @SerializedName("attack_speed")
        public int attackSpeed;

        private WitherBarrage(PoweredValue chance, int minDuration, int maxDuration, int attackSpeed) {
            this.chance = chance;
            this.minDuration = minDuration;
            this.maxDuration = maxDuration;
            this.attackSpeed = attackSpeed;
        }

        public static class Builder {
            private PoweredValue chance = PoweredValue.of(0.10f, 0.04f);
            private int minDuration = 40;
            private int maxDuration = 60;
            private int attackSpeed = 5;

            public Builder chance(PoweredValue chance) {
                this.chance = chance;
                return this;
            }

            public Builder minDuration(int minDuration) {
                this.minDuration = minDuration;
                return this;
            }

            public Builder maxDuration(int maxDuration) {
                this.maxDuration = maxDuration;
                return this;
            }

            public Builder attackSpeed(int attackSpeed) {
                this.attackSpeed = attackSpeed;
                return this;
            }

            public WitherBarrage build() {
                return new WitherBarrage(chance, minDuration, maxDuration, attackSpeed);
            }
        }
    }
}
