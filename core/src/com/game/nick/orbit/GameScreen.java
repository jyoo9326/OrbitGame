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

    int SCREEN_WIDTH, SCREEN_HEIGHT, WORLD_WIDTH, WORLD_HEIGHT;

    OrthographicCamera camera;
    World world;
    Box2DDebugRenderer debugRenderer;
    Body sun, planet, asteroid;
    ArrayList<Body> bodies;
    boolean running, launching;
    ArrayList<CircleShape> circles;


    public GameScreen(final GameActivity game) {
        this.game = game;

        debugRenderer = new Box2DDebugRenderer();

        //get screen dimensions
        SCREEN_WIDTH = Gdx.graphics.getWidth();
        SCREEN_HEIGHT = Gdx.graphics.getHeight();

        WORLD_HEIGHT = 720;
        WORLD_WIDTH = (int)(WORLD_HEIGHT / (float)SCREEN_HEIGHT * SCREEN_WIDTH);


        // create the camera and the SpriteBatch
        camera = new OrthographicCamera();
        camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);

        world = new World(new Vector2(0, 0), true);

        bodies = new ArrayList<Body>();
        circles = new ArrayList<CircleShape>();

        sun = createCircle(1000, WORLD_WIDTH/2, WORLD_HEIGHT/2 );
        bodies.add(sun);
        planet = createCircle(5, WORLD_WIDTH*4/5f, WORLD_HEIGHT/2 );
        bodies.add(planet);
        asteroid = createCircle(1, WORLD_WIDTH/2f, WORLD_HEIGHT*9/10f);
        bodies.add(asteroid);

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

        // Create a circle shape and set its radius to 6
        CircleShape circle = new CircleShape();
        circles.add(circle);
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

        if(running) {
            for (Body body1 : bodies) {
                for (Body body2 : bodies) {
                    if (body1 != body2) {
                        applyGravityBetweenBodies(body1, body2);
                    }
                }
            }
        }

        if(running)
            Gdx.app.log("GameScreen", "planet linear v = (" + planet.getLinearVelocity().x + "," + planet.getLinearVelocity().y + ")");

        debugRenderer.render(world, camera.combined);
        doPhysicsStep(1 / 90f);
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
        Vector2 planetCenter = planet.getWorldCenter();
        float planetRadius = planet.getFixtureList().get(0).getShape().getRadius();
        float touchDistanceFromPlanetCenter = Math.abs(planetCenter.dst(touchLocation));
        boolean tapInsidePlanet = touchDistanceFromPlanetCenter < planetRadius || touchDistanceFromPlanetCenter < 50;
        if(tapInsidePlanet && !launching) {
            launching = true;
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
            Vector2 releaseLocation = new Vector2(x, y);
            Gdx.app.log("GameScreen", "launch release location = (" + x + "," + y + ")");
            Vector2 tapLocation = planet.getWorldCenter();
            Gdx.app.log("GameScreen", "planet center = (" + tapLocation.x + "," + tapLocation.y + ")");

            Vector2 launchVector = releaseLocation.sub(tapLocation);
            launchVector.scl(launchVector.len());
            Gdx.app.log("GameScreen", "launchVector = (" + launchVector.x + "," + launchVector.y + ")");


            planet.setLinearVelocity(launchVector.x, launchVector.y);
            //planet.setLinearVelocity(-200, 200);
            Gdx.app.log("GameScreen", "planet linear v = (" + planet.getLinearVelocity().x + "," + planet.getLinearVelocity().y + ")");

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

        float adustmentFactor = finalDistance/initialDistance;

        Fixture fixture = planet.getFixtureList().get(0);
        Shape circle = fixture.getShape();
        float mass = planet.getMass();

        mass = adustmentFactor * mass;

        double radius = 5 * Math.log(Math.sqrt(mass) + 1);

        double density = mass / (Math.PI * Math.pow(radius, 2));


        if(mass >= 1) {
            fixture.setDensity((float) density);

            circle.setRadius((float) radius);
        }

        planet.resetMassData();

        return false;
    }

}
