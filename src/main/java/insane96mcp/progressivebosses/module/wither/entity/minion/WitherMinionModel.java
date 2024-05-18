package insane96mcp.progressivebosses.module.wither.entity.minion;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WitherMinionModel<T extends Mob & RangedAttackMob> extends HumanoidModel<T> {

	public WitherMinionModel(ModelPart part) {
		super(part);
		//if (!part.visible) {
		//	this.rightArm = new ModelPart(this, 40, 16);
		//	this.rightArm.setPos(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F, modelSize);
		//	this.rightArm.setPos(-5.0F, 2.0F, 0.0F);
		//	this.leftArm = new ModelPart(this, 40, 16);
		//	this.leftArm.mirror = true;
		//	this.leftArm.addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F, modelSize);
		//	this.leftArm.setPos(5.0F, 2.0F, 0.0F);
		//	this.rightLeg = new ModelPart(this, 0, 16);
		//	this.rightLeg.addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F, modelSize);
		//	this.rightLeg.setPos(-2.0F, 12.0F, 0.0F);
		//	this.leftLeg = new ModelPart(this, 0, 16);
		//	this.leftLeg.mirror = true;
		//	this.leftLeg.addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F, modelSize);
		//	this.leftLeg.setPos(2.0F, 12.0F, 0.0F);
		//}

	}

	public void prepareMobModel(T entityIn, float limbSwing, float limbSwingAmount, float partialTick) {
		this.rightArmPose = HumanoidModel.ArmPose.EMPTY;
		this.leftArmPose = HumanoidModel.ArmPose.EMPTY;
		ItemStack itemstack = entityIn.getItemInHand(InteractionHand.MAIN_HAND);
		if (itemstack.getItem() == Items.BOW && entityIn.isAggressive()) {
			if (entityIn.getMainArm() == HumanoidArm.RIGHT) {
				this.rightArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
			} else {
				this.leftArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
			}
		}

		super.prepareMobModel(entityIn, limbSwing, limbSwingAmount, partialTick);
	}

	/**
	 * Sets this entity's model rotation angles
	 */
	public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		ItemStack itemstack = entityIn.getMainHandItem();
		if (entityIn.isAggressive() && (itemstack.isEmpty() || itemstack.getItem() != Items.BOW)) {
			float f = Mth.sin(this.attackTime * (float)Math.PI);
			float f1 = Mth.sin((1.0F - (1.0F - this.attackTime) * (1.0F - this.attackTime)) * (float)Math.PI);
			this.rightArm.zRot = 0.0F;
			this.leftArm.zRot = 0.0F;
			this.rightArm.yRot = -(0.1F - f * 0.6F);
			this.leftArm.yRot = 0.1F - f * 0.6F;
			this.rightArm.xRot = (-(float)Math.PI / 2F);
			this.leftArm.xRot = (-(float)Math.PI / 2F);
			this.rightArm.xRot -= f * 1.2F - f1 * 0.4F;
			this.leftArm.xRot -= f * 1.2F - f1 * 0.4F;
			AnimationUtils.bobArms(this.rightArm, this.leftArm, ageInTicks);
		}

	}

	public void translateToHand(HumanoidArm sideIn, PoseStack matrixStackIn) {
		float f = sideIn == HumanoidArm.RIGHT ? 1.0F : -1.0F;
		ModelPart modelrenderer = this.getArm(sideIn);
		modelrenderer.x += f;
		modelrenderer.translateAndRotate(matrixStackIn);
		modelrenderer.x -= f;
	}
}
