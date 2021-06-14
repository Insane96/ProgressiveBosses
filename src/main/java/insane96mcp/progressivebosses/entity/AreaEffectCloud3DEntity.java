package insane96mcp.progressivebosses.entity;

import com.google.common.collect.Lists;
import insane96mcp.insanelib.utils.RandomHelper;
import insane96mcp.progressivebosses.setup.ModEntities;
import net.minecraft.entity.*;
import net.minecraft.network.IPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AreaEffectCloud3DEntity extends AreaEffectCloudEntity {
	public AreaEffectCloud3DEntity(EntityType<? extends AreaEffectCloud3DEntity> cloud, World world) {
		super(cloud, world);
	}

	public AreaEffectCloud3DEntity(World worldIn, double x, double y, double z) {
		this(ModEntities.AREA_EFFECT_CLOUD_3D.get(), worldIn);
		this.setPosition(x, y, z);
	}

	@Override
	public void recalculateSize() {
		//float height = this.getHeight();
		super.recalculateSize();
		//float newHeight = this.getHeight();
		//this.setPosition(this.getPosX(), this.getPosY() - (newHeight - height), this.getPosZ());
		double radius = (double)this.getSize(Pose.STANDING).width / 2.0D;
		this.setBoundingBox(new AxisAlignedBB(this.getPosX() - radius, this.getPosY(), this.getPosZ() - radius, this.getPosX() + radius, this.getPosY() + radius * 2, this.getPosZ() + radius));
	}

	@Override
	public void tick() {
		boolean flag = this.shouldIgnoreRadius();
		float f = this.getRadius();
		if (this.world.isRemote) {
			IParticleData iparticledata = this.getParticleData();
			if (flag) {
				if (this.rand.nextBoolean()) {
					for(int i = 0; i < 2; ++i) {
						float f1 = this.rand.nextFloat() * ((float)Math.PI * 2F);
						float f2 = MathHelper.sqrt(this.rand.nextFloat()) * 0.2F;
						float x = MathHelper.cos(f1) * f2;
						float z = MathHelper.sin(f1) * f2;
						if (iparticledata.getType() == ParticleTypes.ENTITY_EFFECT) {
							int j = this.rand.nextBoolean() ? 16777215 : this.getColor();
							int k = j >> 16 & 255;
							int l = j >> 8 & 255;
							int i1 = j & 255;
							this.world.addOptionalParticle(iparticledata, this.getPosX() + (double)x, this.getPosY(), this.getPosZ() + (double)z, (double)((float)k / 255.0F), (double)((float)l / 255.0F), (double)((float)i1 / 255.0F));
						} else {
							this.world.addOptionalParticle(iparticledata, this.getPosX() + (double)x, this.getPosY(), this.getPosZ() + (double)z, 0.0D, 0.0D, 0.0D);
						}
					}
				}
			} else {
				float f5 = (float)Math.PI * f * f * 2;

				for(int k1 = 0; (float)k1 < f5; ++k1) {
					float f6 = this.rand.nextFloat() * ((float)Math.PI * 2F);
					float f7 = MathHelper.sqrt(this.rand.nextFloat()) * f;
					/*float y = MathHelper.sin( -(Math.PI / 2F) + Math.PI * r * R);
					float f8 = MathHelper.cos(f6) * f7;
					float f9 = MathHelper.sin(f6) * f7;*/
					float x = RandomHelper.getFloat(this.rand, -f, f);
					float y = RandomHelper.getFloat(this.rand, 0, f * 2);
					float z = RandomHelper.getFloat(this.rand, -f, f);
					if (iparticledata.getType() == ParticleTypes.ENTITY_EFFECT) {
						int l1 = this.getColor();
						int i2 = l1 >> 16 & 255;
						int j2 = l1 >> 8 & 255;
						int j1 = l1 & 255;
						this.world.addOptionalParticle(iparticledata, this.getPosX() + (double)x, this.getPosY() + (double)y, this.getPosZ() + (double)z, (double)((float)i2 / 255.0F), (double)((float)j2 / 255.0F), (double)((float)j1 / 255.0F));
					} else {
						this.world.addOptionalParticle(iparticledata, this.getPosX() + (double)x, this.getPosY() + (double)y, this.getPosZ() + (double)z, (0.5D - this.rand.nextDouble()) * 0.15D, (double)0.01F, (0.5D - this.rand.nextDouble()) * 0.15D);
					}
				}
			}
		} else {
			if (this.ticksExisted >= this.waitTime + this.duration) {
				this.remove();
				return;
			}

			boolean flag1 = this.ticksExisted < this.waitTime;
			if (flag != flag1) {
				this.setIgnoreRadius(flag1);
			}

			if (flag1) {
				return;
			}

			if (this.radiusPerTick != 0.0F) {
				f += this.radiusPerTick;
				if (f < 0.5F) {
					this.remove();
					return;
				}

				this.setRadius(f);
			}

			if (this.ticksExisted % 5 == 0) {
				Iterator<Map.Entry<Entity, Integer>> iterator = this.reapplicationDelayMap.entrySet().iterator();

				while(iterator.hasNext()) {
					Map.Entry<Entity, Integer> entry = iterator.next();
					if (this.ticksExisted >= entry.getValue()) {
						iterator.remove();
					}
				}

				List<EffectInstance> list = Lists.newArrayList();

				for(EffectInstance effectinstance1 : this.potion.getEffects()) {
					list.add(new EffectInstance(effectinstance1.getPotion(), effectinstance1.getDuration() / 4, effectinstance1.getAmplifier(), effectinstance1.isAmbient(), effectinstance1.doesShowParticles()));
				}

				list.addAll(this.effects);
				if (list.isEmpty()) {
					this.reapplicationDelayMap.clear();
				} else {
					List<LivingEntity> list1 = this.world.getEntitiesWithinAABB(LivingEntity.class, this.getBoundingBox());
					if (!list1.isEmpty()) {
						for(LivingEntity livingentity : list1) {
							if (!this.reapplicationDelayMap.containsKey(livingentity) && livingentity.canBeHitWithPotion()) {
								//double x = livingentity.getPosX() - this.getPosX();
								//double y = livingentity.getPosY() + (livingentity.getSize(livingentity.getPose()).height / 2) - (this.getPosY() + f);
								//double z = livingentity.getPosZ() - this.getPosZ();
								//double d2 = x * x + y * y + z * z;
								//LogHelper.info("%f %f %f %s", d2, f, f*f, this.getBoundingBox().toString());
								//LogHelper.info("%f %f %f", x, y, z);
								//LogHelper.info("%f %f", livingentity.getPosY(), this.getPosY());
								//if (d2 <= (double)(f * f)) {
								this.reapplicationDelayMap.put(livingentity, this.ticksExisted + this.reapplicationDelay);

								for(EffectInstance effectinstance : list) {
									if (effectinstance.getPotion().isInstant()) {
										effectinstance.getPotion().affectEntity(this, this.getOwner(), livingentity, effectinstance.getAmplifier(), 0.5D);
									} else {
										livingentity.addPotionEffect(new EffectInstance(effectinstance));
									}
								}

								if (this.radiusOnUse != 0.0F) {
									f += this.radiusOnUse;
									if (f < 0.5F) {
										this.remove();
										return;
									}

									this.setRadius(f);
								}

								if (this.durationOnUse != 0) {
									this.duration += this.durationOnUse;
									if (this.duration <= 0) {
										this.remove();
										return;
									}
								}
								//}
							}
						}
					}
				}
			}
		}

	}

	@Override
	public EntitySize getSize(Pose poseIn) {
		return EntitySize.flexible(this.getRadius() * 2.0F, this.getRadius() * 2.0F);
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}
