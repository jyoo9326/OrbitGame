package com.game.nick.orbit;

/**
 * Created by Nick on 3/29/2016.
 */
import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class GameScreen implements Screen {
    final GameActivity game;

    final int SCREEN_WIDTH = 800, SCREEN_HEIGHT = 480;

    OrthographicCamera camera;
    World world;
    Box2DDebugRenderer debugRenderer;
    Body sun, planet, asteroid;
    ArrayList<Body> bodies;

    public GameScreen(final GameActivity game) {
        this.game = game;

        debugRenderer = new Box2DDebugRenderer();

        // create the camera and the SpriteBatch
        camera = new OrthographicCamera();
        camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);

        world = new World(new Vector2(0, 0), true);

        bodies = new ArrayList<Body>();

        sun = createCircle(100, SCREEN_WIDTH/2, SCREEN_HEIGHT/2 );
        bodies.add(sun);
        planet = createCircle(5, SCREEN_WIDTH*4/5f, SCREEN_HEIGHT/2 );
        bodies.add(planet);
        asteroid = createCircle(1, SCREEN_WIDTH/2f, SCREEN_HEIGHT*9/10f);
        bodies.add(asteroid);

        planet.setLinearVelocity(-300f, 220f);

    }

    private Body createCircle(float mass, float x, float y) {
        // First we create a body definition
        BodyDef bodyDef = new BodyDef();
        // We set our body to dynamic, for something like ground which doesn't move we would set it to StaticBody
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        // Set our body's starting position in the world
        bodyDef.position.set(x, y);

        // Create our body in the world using our body definition
        Body body = world.createBody(bodyDef);

        // Create a circle shape and set its radius to 6
        CircleShape circle = new CircleShape();
        double radius = 5 * Math.log(Math.sqrt(mass) + 1);
        circle.setRadius((float)radius);

        double density = mass / (Math.PI * Math.pow(radius, 2));

        // Create a fixture definition to apply our shape to
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = (float)density;

        // Create our fixture and attach it to the body
        body.createFixture(fixtureDef);

        return body;
    }

    private float accumulator = 0;

    private void doPhysicsStep(float deltaTime) {
        // fixed time step
        // max frame time to avoid spiral of death (on slow devices)
        //float frameTime = Math.min(deltaTime, 0.25f);
        //accumulator += frameTime;
        //while (accumulator >= 1/60f) {
        world.step(deltaTime, 6, 2);
            //accumulator -= 1/60f;
        //}
    }

    public void applyGravityBetweenBodies(Body body1, Body body2) {
        float m1 = body1.getMass();
        float m2 = body2.getMass();
        Vector2 r = body1.getWorldCenter().sub(body2.getWorldCenter());

        Vector2 f = r.nor().scl((float) (m1 * m2 / Math.pow(r.len(), 2)));

        body2.applyForceToCenter(f.x, f.y, true);
        //body1.applyForceToCenter(-f.x, -f.y, true);

    }

    @Override
    public void render(float delta) {
        // clear the screen with a dark blue color. The
        // arguments to glClearColor are the red, green
        // blue and alpha component in the range [0,1]
        // of the color to be used to clear the screen.
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // tell the camera to update its matrices.
        camera.update();

        for(Body body1 : bodies) {
            for(Body body2 : bodies) {
                if(body1 != body2) {
                    applyGravityBetweenBodies(body1, body2);
                }
            }
        }

        debugRenderer.render(world, camera.combined);
        doPhysicsStep(1/90f);
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void show() {
        // start the playback of the background music
        // when the screen is shown
        //rainMusic.play();
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
    }

}
