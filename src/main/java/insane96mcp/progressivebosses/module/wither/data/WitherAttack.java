package insane96mcp.progressivebosses.module.wither.data;

import com.google.gson.annotations.SerializedName;
import insane96mcp.progressivebosses.data.Difficulty;

import javax.annotation.Nullable;

public class WitherAttack {
    @SerializedName("skull_damage")
    public float skullDamage = 8f;
    @SerializedName("skull_speed_multiplier")
    public float skullSpeedMultiplier = 1f;
    @SerializedName("dangerous_skull_chance")
    public PoweredValue dangerousSkullChance = PoweredValue.of(0.05f);
    @SerializedName("attack_speed_near")
    public int attackSpeedNear = 40;
    @SerializedName("attack_speed_far")
    public int attackSpeedFar = 40;
    @SerializedName("side_heads_attack_speed_multiplier")
    public float sideHeadsAttackSpeedMultiplier = 1f;
    @SerializedName("effect_amplifier")
    public int effectAmplifier = 0;
    @SerializedName("effect_duration")
    public Difficulty effectDuration = Difficulty.of(10, 10, 20);
    @SerializedName("attack_to_heal_threshold")
    public float attackToHealThreshold;
    @SerializedName("heal_on_skull_kill")
    public float healOnSkullKill = 5;
    @SerializedName("charge")
    @Nullable
    public WitherCharge charge;
    @SerializedName("barrage")
    @Nullable
    public WitherBarrage barrage;

    /*private WitherAttack(float skullDamage, float skullSpeedMultiplier, PoweredValue dangerousSkullChance, int attackSpeedNear, int attackSpeedFar, float sideHeadsAttackSpeedMultiplier, int effectAmplifier, Difficulty effectDuration, float attackToHealThreshold, float healOnSkullKill, @Nullable WitherCharge charge, @Nullable WitherBarrage barrage) {
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
    }*/

    public static class Builder {
        private WitherAttack instance = new WitherAttack();

        public Builder skullDamage(float skullDamage) {
            instance.skullDamage = skullDamage;
            return this;
        }

        public Builder skullSpeedMultiplier(float skullSpeedMultiplier) {
            instance.skullSpeedMultiplier = skullSpeedMultiplier;
            return this;
        }

        public Builder dangerousSkullChance(PoweredValue dangerousSkullChance) {
            instance.dangerousSkullChance = dangerousSkullChance;
            return this;
        }

        public Builder attackSpeedNear(int attackSpeedNear) {
            instance.attackSpeedNear = attackSpeedNear;
            return this;
        }

        public Builder attackSpeedFar(int attackSpeedFar) {
            instance.attackSpeedFar = attackSpeedFar;
            return this;
        }

        public Builder sideHeadsAttackSpeedMultiplier(float sideHeadsAttackSpeedMultiplier) {
            instance.sideHeadsAttackSpeedMultiplier = sideHeadsAttackSpeedMultiplier;
            return this;
        }

        public Builder effectAmplifier(int effectAmplifier) {
            instance.effectAmplifier = effectAmplifier;
            return this;
        }

        public Builder effectDuration(Difficulty effectDuration) {
            instance.effectDuration = effectDuration;
            return this;
        }

        public Builder attackToHealThreshold(float attackToHealThreshold) {
            instance.attackToHealThreshold = attackToHealThreshold;
            return this;
        }

        public Builder healOnSkullKill(float healOnSkullKill) {
            instance.healOnSkullKill = healOnSkullKill;
            return this;
        }

        public Builder charge(@Nullable WitherCharge charge) {
            instance.charge = charge;
            return this;
        }

        public Builder barrage(@Nullable WitherBarrage barrage) {
            instance.barrage = barrage;
            return this;
        }

        public WitherAttack build() {
            return instance;
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
        public int secondPhaseMaxReduction = Integer.MAX_VALUE;
        @SerializedName("second_phase_barrage")
        public boolean secondPhaseBarrage;
        @SerializedName("second_phase_minion")
        public boolean secondPhaseMinion;

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

            public Builder secondPhaseBarrage(boolean secondPhaseBarrage) {
                instance.secondPhaseBarrage = secondPhaseBarrage;
                return this;
            }

            public Builder secondPhaseMinion(boolean secondPhaseMinion) {
                instance.secondPhaseMinion = secondPhaseMinion;
                return this;
            }

            public WitherCharge build() {
                return instance;
            }
        }
    }

    public static class WitherBarrage {
        @SerializedName("chance_on_hit")
        public PoweredValue chanceOnHit = PoweredValue.of(0.05f);
        @SerializedName("min_duration")
        public int minDuration = 40;
        @SerializedName("max_duration")
        public int maxDuration = 60;
        @SerializedName("attack_speed")
        public int attackSpeed = 5;
        @SerializedName("attack_cooldown_on_end")
        public int attackCooldownOnEnd = 0;

        public static class Builder {
            private final WitherBarrage instance = new WitherBarrage();

            public Builder chanceOnHit(PoweredValue chanceOnHit) {
                instance.chanceOnHit = chanceOnHit;
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

            public Builder attackCooldownOnEnd(int attackCooldownOnEnd) {
                instance.attackCooldownOnEnd = attackCooldownOnEnd;
                return this;
            }

            public WitherBarrage build() {
                return instance;
            }
        }
    }
}
