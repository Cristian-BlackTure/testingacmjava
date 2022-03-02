package UAW.world.drawer;

import arc.Core;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.Time;
import gas.world.GasBlock;
import gas.world.blocks.production.GasGenericCrafter;
import gas.world.draw.GasDrawBlock;
import mindustry.graphics.*;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.consumers.*;

/** The cursed amalgamation of every block drawer code */
public class GasDrawEverything extends GasDrawBlock {
	public TextureRegion bottomRegion, primaryRotatorRegion, secondaryRotatorRegion, heatRegion, inputLiquidRegion, outputLiquidRegion, smelterFlameRegion, topRegion;
	public TextureRegion clearSprite;
	/** Rotation Spin of the main rotator */
	public float rotatorSpinSpeed = 12f;
	/** Rotation speed multiplier for secondary rotator */
	public float secondaryRotatorSpinSpeedMult = 0.5f;

	/** The color of the heat light | Only valid if heatRegion exists */
	public Color heatColor = Color.valueOf("ff5512");

	// Arc Smelter
	public boolean drawArcSmelter = false;
	public boolean circleSpark = false;
	public Color arcFlameColor = Color.valueOf("f58349"), arcMidColor = Color.valueOf("f2d585");
	public float arcFlameRad = 1f, arcCircleSpace = 2f, arcFlameRadiusScl = 3f, arcFlameRadiusMag = 0.3f, arcCircleStroke = 1.5f;
	public float arcAlpha = 0.68f;
	public int arcParticles = 25;
	public float arcParticleLife = 40f, arcParticleRad = 7f, arcParticleSmoke = 1.1f, argParticleLength = 3f;

	// Cells
	public boolean drawSmokeCells = false;
	public Color smokeColor = Color.white.cpy(), smokeParticleColorFrom = Color.black.cpy(), smokeParticleColorTo = Color.black.cpy();
	public int smokeParticles = 12;
	public float smokeRange = 4f, smokeRecurrance = 6f, smokeRadius = 3f, lifetime = 60f;

	// Smelter
	public Color smelterFlameColor = Color.valueOf("ffc999");
	public float smelterLightRadius = 60f, smelterLightAlpha = 0.65f, smelterLightSinScl = 10f, smelterLightSinMag = 5;
	public float smelterFlameRadius = 3f, smelterFlameRadiusIn = 1.9f, smelterFlameRadiusScl = 5f, smelterFlameRadiusMag = 2f, smelterFlameRadiusInMag = 1f;


	@Override
	public void draw(GasGenericCrafter.GasGenericCrafterBuild build) {
		GasGenericCrafter type = (GasGenericCrafter) build.block;

		// Draws Bottom Region
		if (bottomRegion.found()) {
			Draw.rect(bottomRegion, build.x, build.y);
		}

		// Draws Arc Smelter
		if (drawArcSmelter) {
			if (build.warmup > 0f && arcFlameColor.a > 0.001f) {
				float si = Mathf.absin(arcFlameRadiusScl, arcFlameRadiusMag);
				float a = arcAlpha * build.warmup;
				if (!circleSpark) {
					Lines.stroke(arcCircleStroke * build.warmup);

					Draw.blend(Blending.additive);

					Draw.color(arcMidColor, a);
					Fill.circle(build.x, build.y, arcFlameRad + si);

					Draw.color(arcFlameColor, a);
					Lines.circle(build.x, build.y, (arcFlameRad + arcCircleSpace + si) * build.warmup);

					Lines.stroke(arcParticleSmoke * build.warmup);
				}
				float base = (Time.time / arcParticleLife);
				rand.setSeed(build.id);
				for (int i = 0; i < arcParticles; i++) {
					float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin;
					float angle = rand.random(360f);
					float len = arcParticleRad * Interp.pow2Out.apply(fin);
					if (!circleSpark) {
						Lines.lineAngle(build.x + Angles.trnsx(angle, len), build.y + Angles.trnsy(angle, len), angle, argParticleLength * fout * build.warmup);
					} else
						Fill.circle(build.x + Angles.trnsx(angle, len), build.y + Angles.trnsy(angle, len), argParticleLength * fout * build.warmup);
				}

				Draw.blend();
				Draw.reset();
			}
		}

		if (drawSmokeCells && build.warmup > 0.001f) {
			rand.setSeed(build.id);
			for (int i = 0; i < smokeParticles; i++) {
				float offset = rand.nextFloat() * 999999f;
				float x = rand.range(smokeRange), y = rand.range(smokeRange);
				float fin = 1f - (((Time.time + offset) / lifetime) % smokeRecurrance);
				float ca = rand.random(0.1f, 1f);
				float fslope = Mathf.slope(fin);

				if (fin > 0) {
					Draw.color(smokeParticleColorFrom, smokeParticleColorTo, ca);
					Draw.alpha(build.warmup);
					Fill.circle(build.x + x, build.y + y, fslope * smokeRadius);
				}
			}
			Draw.reset();
		}

		// Draws the block
		Draw.rect(build.block.region, build.x, build.y);

		// Draws Heat
		if (heatRegion.found()) {
			Draw.color(heatColor);
			Draw.alpha(build.warmup * 0.6f * (1f - 0.3f + Mathf.absin(Time.time, 3f, 0.3f)));
			Draw.blend(Blending.additive);
			Draw.rect(heatRegion, build.x, build.y);
			Draw.blend();
			Draw.color();
			Draw.reset();
		}

		// Rotator
		if (primaryRotatorRegion.found()) {
			Drawf.spinSprite(primaryRotatorRegion, build.x, build.y, build.totalProgress * rotatorSpinSpeed);
		}

		if (secondaryRotatorRegion.found()) {
			Drawf.spinSprite(secondaryRotatorRegion, build.x, build.y, build.totalProgress * (rotatorSpinSpeed * secondaryRotatorSpinSpeedMult));
		}

		// Liquid Input
		if ((inputLiquidRegion.found()) && type.consumes.has(ConsumeType.liquid)) {
			Liquid input = type.consumes.<ConsumeLiquid>get(ConsumeType.liquid).liquid;
			Drawf.liquid(inputLiquidRegion, build.x, build.y,
				build.liquids.get(input) / type.liquidCapacity,
				input.color
			);
		}

		// Liquid Output
		if (outputLiquidRegion.found() && type.outputLiquid != null && build.liquids.get(type.outputLiquid.liquid) > 0) {
			Drawf.liquid(outputLiquidRegion, build.x, build.y,
				build.liquids.get(type.outputLiquid.liquid) / type.liquidCapacity,
				type.outputLiquid.liquid.color
			);
		}

		// Top Region
		if (topRegion.found()) Draw.rect(topRegion, build.x, build.y);

		// Smelter Flame
		if (smelterFlameRegion.found()) {
			if (build.warmup > 0f && smelterFlameColor.a > 0.001f) {
				float g = 0.3f;
				float r = 0.06f;
				float cr = Mathf.random(0.1f);

				Draw.z(Layer.block + 0.01f);

				Draw.alpha(build.warmup);
				Draw.rect(smelterFlameRegion, build.x, build.y);

				Draw.alpha(((1f - g) + Mathf.absin(Time.time, 8f, g) + Mathf.random(r) - r) * build.warmup);

				Draw.tint(smelterFlameColor);
				Fill.circle(build.x, build.y, smelterFlameRadius + Mathf.absin(Time.time, smelterFlameRadiusScl, smelterFlameRadiusMag) + cr);
				Draw.color(1f, 1f, 1f, build.warmup);
				Fill.circle(build.x, build.y, smelterFlameRadiusIn + Mathf.absin(Time.time, smelterFlameRadiusScl, smelterFlameRadiusInMag) + cr);
				Draw.color();
				Draw.reset();
			}
		}
	}

	@Override
	public void drawLight(GasGenericCrafter.GasGenericCrafterBuild build) {
		if (smelterFlameRegion.found())
			Drawf.light(build.team, build.x, build.y, (smelterLightRadius + Mathf.absin(smelterLightSinScl, smelterLightSinMag)) * build.warmup * build.block.size, smelterFlameColor, smelterLightAlpha);
	}

	@Override
	public void load(GasBlock block) {
		this.bottomRegion = Core.atlas.find(block.name + "-bottom");
		heatRegion = Core.atlas.find(block.name + "-heat");
		primaryRotatorRegion = Core.atlas.find(block.name + "-rotator-1");
		secondaryRotatorRegion = Core.atlas.find(block.name + "-rotator-2");
		inputLiquidRegion = Core.atlas.find(block.name + "-input-liquid");
		outputLiquidRegion = Core.atlas.find(block.name + "-output-liquid");
		topRegion = Core.atlas.find(block.name + "-top");
		smelterFlameRegion = Core.atlas.find(block.name + "-smelterFlame");
		clearSprite = Core.atlas.find("clear");
	}

	@Override
	public TextureRegion[] icons(GasBlock block) {
		return new TextureRegion[]{
			bottomRegion.found() ? bottomRegion : clearSprite,
			block.region,
			primaryRotatorRegion.found() ? primaryRotatorRegion : clearSprite,
			secondaryRotatorRegion.found() ? secondaryRotatorRegion : clearSprite,
			topRegion.found() ? topRegion : clearSprite
		};
	}
}