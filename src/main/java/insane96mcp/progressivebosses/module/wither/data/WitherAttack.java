package insane96mcp.progressivebosses.module.wither.data;

import com.google.gson.annotations.SerializedName;
import insane96mcp.progressivebosses.data.Difficulty;
import insane96mcp.progressivebosses.module.wither.entity.PBWither;

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
        @SerializedName("on_hit")
        public OnHit onHit;
        @SerializedName("second_phase")
        public SecondPhase secondPhase;
        @SerializedName("target_unseen")
        public TargetUnseen targetUnseen;

        public static class Builder {
            private final WitherCharge instance = new WitherCharge();

            public Builder onHit(OnHit onHit) {
                instance.onHit = onHit;
                return this;
            }

            public Builder secondPhase(SecondPhase secondPhase) {
                instance.secondPhase = secondPhase;
                return this;
            }

            public Builder targetUnseen(TargetUnseen targetUnseen) {
                instance.targetUnseen = targetUnseen;
                return this;
            }

            public WitherCharge build() {
                return instance;
            }
        }

        public static abstract class BaseCharge {
            @SerializedName("damage")
            public float damage;
            @SerializedName("time_to_charge")
            public int timeToCharge;
        }

        public static class OnHit extends BaseCharge {
            @SerializedName("chance")
            public PoweredValue chance;

            public static class Builder {
                private final OnHit instance = new OnHit();

                public Builder chance(PoweredValue chance) {
                    instance.chance = chance;
                    return this;
                }

                public Builder damage(float damage) {
                    instance.damage = damage;
                    return this;
                }

                public Builder timeToCharge(int timeToCharge) {
                    instance.timeToCharge = timeToCharge;
                    return this;
                }

                public OnHit build() {
                    return instance;
                }
            }
        }

        public static class SecondPhase extends BaseCharge {
            @SerializedName("times")
            public int times;
            @SerializedName("tick_reduction")
            public int tickReduction;
            @SerializedName("max_reduction")
            public int maxReduction;
            @SerializedName("barrage")
            public boolean barrage;
            @SerializedName("minion")
            public boolean minion;

            public static class Builder {
                private final SecondPhase instance = new SecondPhase();

                public Builder times(int times) {
                    instance.times = times;
                    return this;
                }

                public Builder tickReduction(int tickReduction) {
                    instance.tickReduction = tickReduction;
                    return this;
                }

                public Builder maxReduction(int maxReduction) {
                    instance.maxReduction = maxReduction;
                    return this;
                }

                public Builder barrage(boolean barrage) {
                    instance.barrage = barrage;
                    return this;
                }

                public Builder minion(boolean minion) {
                    instance.minion = minion;
                    return this;
                }

                public Builder damage(float damage) {
                    instance.damage = damage;
                    return this;
                }

                public Builder timeToCharge(int timeToCharge) {
                    instance.timeToCharge = timeToCharge;
                    return this;
                }

                public SecondPhase build() {
                    return instance;
                }
            }
        }

        public static class TargetUnseen extends BaseCharge {
            @SerializedName("seconds_unseen")
            public int secondsUnseen;
            @SerializedName("chance_per_second")
            public float chancePerSecond;
            @SerializedName("max_chance")
            public float maxChance = 1f;

            public static class Builder {
                private final TargetUnseen instance = new TargetUnseen();

                public Builder secondsUnseen(int secondsUnseen) {
                    instance.secondsUnseen = secondsUnseen;
                    return this;
                }

                public Builder chancePerSecond(float chancePerSecond) {
                    instance.chancePerSecond = chancePerSecond;
                    return this;
                }

                public Builder maxChance(float max_chance) {
                    instance.maxChance = max_chance;
                    return this;
                }

                public Builder damage(float damage) {
                    instance.damage = damage;
                    return this;
                }

                public Builder timeToCharge(int timeToCharge) {
                    instance.timeToCharge = timeToCharge;
                    return this;
                }

                public TargetUnseen build() {
                    return instance;
                }
            }
        }

        public static float getDamage(PBWither wither) {
            return wither.chargeType.getDamage(wither);
        }

        public static int getTimeToCharge(PBWither wither) {
            return wither.chargeType.getTimeToCharge(wither);
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
