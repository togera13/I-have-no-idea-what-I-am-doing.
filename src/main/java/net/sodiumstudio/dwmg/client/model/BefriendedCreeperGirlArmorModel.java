package net.sodiumstudio.dwmg.client.model;

import com.github.mechalopa.hmag.client.model.GirlArmorModel;
import com.github.mechalopa.hmag.client.util.ModClientUtils;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.sodiumstudio.dwmg.entities.hmag.HmagCreeperGirlEntity;

// Ported from HMaG CreeperGirlArmorModel
@OnlyIn(Dist.CLIENT)
public class BefriendedCreeperGirlArmorModel<T extends HmagCreeperGirlEntity> extends GirlArmorModel<T>
{
	public BefriendedCreeperGirlArmorModel(ModelPart modelPart)
	{
		super(modelPart);
	}

	public static MeshDefinition createPowerArmorMesh(CubeDeformation cd)
	{
		MeshDefinition md = HumanoidModel.createMesh(cd, 0.0F);
		PartDefinition pd = md.getRoot();
		ModClientUtils.addC(pd, cd, "body", 16, 16, -3.0F, 0.0F, -1.5F, 6.0F, 12.0F, 3.0F, 0.0F, 0.0F, 0.0F);
		ModClientUtils.addC(pd, cd, "right_arm", 40, 16, -1.0F, -2.0F, -1.5F, 3.0F, 12.0F, 3.0F, -5.0F, 2.0F, 0.0F);
		ModClientUtils.addC(pd, cd, "left_arm", 40, 16, -2.0F, -2.0F, -1.5F, 3.0F, 12.0F, 3.0F, 5.0F, 2.0F, 0.0F, true);
		ModClientUtils.addC(pd, cd, "right_leg", 0, 16, -1.5F, 0.0F, -1.5F, 3.0F, 12.0F, 3.0F, -1.9F, 12.0F, 0.0F);
		ModClientUtils.addC(pd, cd, "left_leg", 0, 16, -1.5F, 0.0F, -1.5F, 3.0F, 12.0F, 3.0F, 1.9F, 12.0F, 0.0F, true);
		return md;
	}

	@Override
	public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

		BefriendedCreeperGirlModel.doAnim(entityIn, ageInTicks, this.attackTime, this);
	}
}