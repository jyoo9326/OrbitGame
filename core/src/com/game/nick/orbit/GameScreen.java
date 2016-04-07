package com.game.nick.orbit;

/**
 * Created by Nick on 3/29/2016.
 */
import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.input.GestureDetector;
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
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import sun.rmi.runtime.Log;

public class GameScreen implements Screen, GestureDetector.GestureListener, InputProcessor {
    final GameActivity game;
    final float TIMESTEP = 1/60f;
    final float STANDARD_MASS = 5;
    final float STANDARD_DENSITY = 1;

    int SCREEN_WIDTH, SCREEN_HEIGHT, WORLD_WIDTH, WORLD_HEIGHT;

    OrthographicCamera camera;
    World world;
    Box2DDebugRenderer debugRenderer;
    Body sun, planet, asteroid;
    ArrayList<Body> bodies;
    boolean running, launching;
    ArrayList<CircleShape> circles;
    int selectedBody;



    public GameScreen(final GameActivity game) {
        this.game = game;

        debugRenderer = new Box2DDebugRenderer();

        //get screen dimensions
        SCREEN_WIDTH = Gdx.graphics.getWidth();
        SCREEN_HEIGHT = Gdx.graphics.getHeight();

        WORLD_HEIGHT = 180;
        WORLD_WIDTH = (int)(WORLD_HEIGHT / (float)SCREEN_HEIGHT * SCREEN_WIDTH);


        // create the camera and the SpriteBatch
        camera = new OrthographicCamera();
        camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);

        world = new World(new Vector2(0, 0), true);

        bodies = new ArrayList<Body>();
        circles = new ArrayList<CircleShape>();

        sun = createCircle(100, WORLD_WIDTH/2, WORLD_HEIGHT/2 );
        bodies.add(sun);
        planet = createCircle(STANDARD_MASS, WORLD_WIDTH*4/5f, WORLD_HEIGHT/2 );
        bodies.add(planet);
        asteroid = createCircle(1, WORLD_WIDTH/2f, WORLD_HEIGHT*9/10f);
        bodies.add(asteroid);

        selectedBody = -1;

        launching = false;
        running = false;

        InputMultiplexer im = new InputMultiplexer();
        GestureDetector gd = new GestureDetector(this);
        im.addProcessor(gd);
        im.addProcessor(this);

        Gdx.input.setInputProcessor(im);


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

        // Create a circle shape and set its radius
        CircleShape circle = new CircleShape();
        circles.add(circle);
        double radius = getCircleRadius(mass);
        circle.setRadius((float)radius);

        double density = mass / (Math.PI * Math.pow(radius, 2));

        // Create a fixture definition to apply our shape to
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = (float)density;
        fixtureDef.friction = 0;

        // Create our fixture and attach it to the body
        body.createFixture(fixtureDef);
        body.setLinearDamping(0.1f);


        return body;
    }

    private float getCircleRadius(float mass) {
        //return (float)(Math.pow(mass, 2/5.0));
        return (float)(Math.pow(3*mass/4/Math.PI/STANDARD_DENSITY, 1/3.0));
    }

    private void changeBodyMass(Body body, float deltaMass) {
        Fixture fixture = body.getFixtureList().get(0);
        Shape circle = fixture.getShape();
        float mass = body.getMass();

        mass = mass + deltaMass;

        if (mass < 1) {
            mass = 1;
        } else if (mass > 1000000) {
            mass = 1000000;
        }

        double radius = getCircleRadius(mass);

        double density = mass / (Math.PI * Math.pow(radius, 2));


        fixture.setDensity((float) density);
        circle.setRadius((float) radius);


        body.resetMassData();
    }

    private void doPhysicsStep(float deltaTime) {
        // fixed time step
        world.step(deltaTime, 6, 2);
    }

    public void applyGravityBetweenBodies(Body body1, Body body2) {
        float m1 = body1.getMass();
        float m2 = body2.getMass();
        Vector2 r = body1.getWorldCenter().sub(body2.getWorldCenter());

        Vector2 f = r.nor().scl((float) (m1 * m2 / Math.pow(r.len(), 2)));

        body2.applyForceToCenter(f.x, f.y, true);

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


        if(running) {
            Gdx.app.log("GameScreen", "planet linear v before forces = (" + planet.getLinearVelocity().x + "," + planet.getLinearVelocity().y + ")");

            for (Body body1 : bodies) {
                for (Body body2 : bodies) {
                    if (body1 != body2) {
                        applyGravityBetweenBodies(body1, body2);
                    }
                }
            }
        }

        if(running)
            Gdx.app.log("GameScreen", "planet linear v after forces= (" + planet.getLinearVelocity().x + "," + planet.getLinearVelocity().y + ")");

        debugRenderer.render(world, camera.combined);
        doPhysicsStep(TIMESTEP);
    }

    @Override
    public void resize(int width, int height) {
        SCREEN_WIDTH = Gdx.graphics.getWidth();
        SCREEN_HEIGHT = Gdx.graphics.getHeight();

        WORLD_HEIGHT = 180;
        WORLD_WIDTH = (int)(WORLD_HEIGHT / (float)SCREEN_HEIGHT * SCREEN_WIDTH);

        camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);

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
        for(CircleShape circle : circles) {
            circle.dispose();
        }
    }

    @Override
    public boolean keyDown (int keycode) {
        Gdx.app.log("GameScreen", "keyDown registered - keycode = " + keycode);


        return false;
    }

    @Override
    public boolean keyUp (int keycode) {
        Gdx.app.log("GameScreen", "keyUp registered - keycode = " + keycode);

        return false;
    }

    @Override
    public boolean keyTyped (char character) {
        Gdx.app.log("GameScreen", "keyTyped registered - char = " + character);

        return false;
    }


    @Override
    public boolean touchDown (int x, int y, int pointer, int button) {
        x = (int)(WORLD_WIDTH / (float)SCREEN_WIDTH * x); //convert screen x,y to world x,y
        y = (int)(WORLD_HEIGHT / (float)SCREEN_HEIGHT * y);
        y = WORLD_HEIGHT - y; //flip y axis because box2d is different than libgdx

        Gdx.app.log("GameScreen", "touchDown registered at ("+x+","+y+")");


        Vector2 touchLocation = new Vector2(x, y);

        float shortestDistance = Integer.MAX_VALUE;
        int tappedBody = -1;

        for(int i = 0; i < bodies.size(); i++) {
            Body body = bodies.get(i);
            Vector2 bodyCenter = body.getWorldCenter();
            float bodyRadius = body.getFixtureList().get(0).getShape().getRadius();
            float touchDistanceFromBodyCenter = Math.abs(bodyCenter.dst(touchLocation));
            boolean tapInsideBody = touchDistanceFromBodyCenter < bodyRadius || touchDistanceFromBodyCenter < 50;
            if(tapInsideBody && touchDistanceFromBodyCenter < shortestDistance) {
                tappedBody = i;
                shortestDistance = touchDistanceFromBodyCenter;
            }
        }
        if(tappedBody >= 0 && !launching) {
            launching = true;
            selectedBody = tappedBody;
        }
        return false;
    }

    @Override
    public boolean touchUp (int x, int y, int pointer, int button) {
        x = (int)(WORLD_WIDTH / (float)SCREEN_WIDTH * x); //convert screen x,y to world x,y
        y = (int)(WORLD_HEIGHT / (float)SCREEN_HEIGHT * y);
        y = WORLD_HEIGHT - y; //flip y axis because box2d is different than libgdx

        Gdx.app.log("GameScreen", "touchUp registered at (" + x + "," + y + ")");

        if(launching) {
            Body body = bodies.get(selectedBody);
            Vector2 releaseLocation = new Vector2(x, y);
            Gdx.app.log("GameScreen", "launch release location = (" + x + "," + y + ")");
            Vector2 bodyLocation = body.getWorldCenter();
            Gdx.app.log("GameScreen", "body center = (" + bodyLocation.x + "," + bodyLocation.y + ")");

            Vector2 launchVector = releaseLocation.sub(bodyLocation);
            Gdx.app.log("GameScreen", "launchVector = (" + launchVector.x + "," + launchVector.y + ")");

            launchVector.scl(2);

            Vector2 currentVelocity = body.getLinearVelocity();

            Vector2 changeInVelocity = launchVector.sub(currentVelocity);

            Vector2 acceleration = changeInVelocity.scl(1 / TIMESTEP);

            if(body.getMass() > STANDARD_MASS) {
                // scale the acceleration so that bigger bodies have lower accelerations for the same pull
                float scaleFactor = (float)(Math.pow(STANDARD_MASS/body.getMass(), 1/7.0));
                acceleration = acceleration.scl(scaleFactor);
            }

            Vector2 forceToApply = acceleration.scl(body.getMass());

            body.applyForceToCenter(forceToApply, true);
            //planet.setLinearVelocity(-200, 200);
            Gdx.app.log("GameScreen", "body linear v = (" + body.getLinearVelocity().x + "," + body.getLinearVelocity().y + ")");

            launching = false;
            running = true;
        }


        return false;
    }

    @Override
    public boolean touchDragged (int x, int y, int pointer) {
        //Gdx.app.log("GameScreen", "touchDragged registered");

        return false;
    }

    @Override
    public boolean mouseMoved (int x, int y) {
        //Gdx.app.log("GameScreen", "mouseMoved registered");

        return false;
    }

    @Override
    public boolean scrolled (int amount) {
        Gdx.app.log("GameScreen", "scroll registered - amount = " +  amount);

        float scaled_amount = 10 * (float)Math.sqrt(planet.getMass()) * amount;

        changeBodyMass(planet, scaled_amount);

        Gdx.app.log("GameScreen", "Mass adjusted - mass = " + planet.getMass());


        return false;
    }

    @Override
    public boolean touchDown (float x, float y, int pointer, int button) {
        Gdx.app.log("GameScreen", "touchDown registered");
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        Gdx.app.log("GameScreen", "tap registered");

        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        Gdx.app.log("GameScreen", "longPress registered");
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        Gdx.app.log("GameScreen", "fling registered");

        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        Gdx.app.log("GameScreen", "pan registered");
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        Gdx.app.log("GameScreen", "panStop registered");
        return false;
    }

    @Override
    public boolean zoom (float originalDistance, float currentDistance){
        Gdx.app.log("GameScreen", "zoom registered");
        return false;
    }

    @Override
    public boolean pinch (Vector2 initialFirstPointer, Vector2 initialSecondPointer, Vector2 firstPointer, Vector2 secondPointer){
        Gdx.app.log("GameScreen", "pinch registered");

        float initialDistance = Math.abs(initialFirstPointer.dst(initialSecondPointer));
        float finalDistance = Math.abs(firstPointer.dst(secondPointer));

        float changeInDistance = finalDistance - initialDistance;

        changeBodyMass(planet, changeInDistance);

        launching = false; //make sure this scaling isn't registered as a launch

        return false;
    }

}
