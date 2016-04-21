package com.game.nick.orbit;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Shape;

/**
 * Created by Nick on 4/19/2016.
 */
public class DynamicSprite extends Sprite {
    Body physicsBody;

    public DynamicSprite(Texture texture) {
        super(texture);
    }

    public void attachBody(Body body) {
        this.physicsBody = body;
    }

    public Body getBody() {
        return physicsBody;
    }

    public Fixture getFixture(int n) {
        return physicsBody.getFixtureList().get(n); //bodies can have multiple fixtures so the get() method returns a list. We want the fixture at index n.
    }

    public Fixture getFixture() {
        return getFixture(0);
    }

    public Shape getShape() {
        return getFixture().getShape();
    }

    public void update(GameScreen screen) {
        Vector2 position = screen.getScreenPosition(getBody().getWorldCenter());
        float radius = screen.scaleDistanceToScreen(getShape().getRadius());
        setSize(radius * 2, radius * 2);
        setPositionCenter(position.x, position.y);
        setOrigin(getWidth() / 2, getHeight() / 2);
        setRotation((float)Math.toDegrees(physicsBody.getAngle()));


    }

    public void setPositionCenter(float x, float y) {
        float width = getWidth();
        float height = getHeight();
        setPosition(x-width/2, y-height/2);
    }


}
