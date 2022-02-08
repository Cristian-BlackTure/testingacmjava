package UAW.entities.bullet;

import UAW.graphics.UAWFxS;
import arc.Core;
import arc.graphics.g2d.Draw;
import mindustry.gen.*;
import mindustry.graphics.*;

import static UAW.Vars.cruiseMissileBasic;
import static mindustry.Vars.tilesize;

public class CruiseMissileBulletType extends UAWBasicBulletType {
	public float sizeScl = 1;

	public CruiseMissileBulletType(float speed, float damage, String sprite) {
		super(speed, damage, sprite);
		this.speed = speed;
		this.damage = damage;
		this.splashDamage = damage * 0.8f;
		height = sizeScl;
		width = sizeScl / 2.4f;
		layer = Layer.effect + 1;
		shrinkX = shrinkY = 0;
		drag = -0.015f;
		homingRange = 30 * tilesize;
		homingPower = 0.035f;
		splashDamageRadius = 12 * tilesize;
		hitShake = 12f;
		hitSize = 1.5f * 8;
		hitSoundVolume = 2f;
		hitSound = Sounds.explosionbig;
		backColor = Pal.missileYellowBack;
		frontColor = Pal.missileYellow;
		trailInterval = 0.5f;
		trailEffect = UAWFxS.pyraSmokeTrail;
		trailRotation = true;
		despawnHit = true;
		keepVelocity = false;
	}

	public CruiseMissileBulletType(float speed, float damage) {
		this(speed, damage, cruiseMissileBasic);
	}

	public CruiseMissileBulletType() {
		this(1.8f, 225);
	}

	@Override
	public void load() {
		super.load();
		backRegion = Core.atlas.find(sprite + "-outline");
	}

	@Override
	public void draw(Bullet b) {
		super.draw(b);
		Draw.rect(frontRegion, b.x, b.y, frontRegion.width * sizeScl, frontRegion.height * sizeScl, b.rotation() - 90);
		Draw.rect(backRegion, b.x, b.y, backRegion.width * sizeScl, backRegion.height * sizeScl, b.rotation() - 90);
		Draw.reset();
	}
}

