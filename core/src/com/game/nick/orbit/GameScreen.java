package com.game.nick.orbit;

/**
 * Created by Nick on 3/29/2016.
 */
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameScreen implements Screen, GestureDetector.GestureListener, InputProcessor {
    final GameActivity game;
    final float TIMESTEP = 1/60f;
    final float STANDARD_MASS = 1000;
    final float STANDARD_DENSITY = 1f;
    final float GRAVITY_CONSTANT = 10f;
    final float SIZE_ADJUSTMENT_FACTOR = 1/10f;
    final int BODY_MATRIX_N = 5;

    float screenWidth, screenHeight, worldWidth, worldHeight, hudHeight, hudWidth;

    Integer test;

    float timeStep;

    OrthographicCamera camera;
    World world;
    Box2DDebugRenderer debugRenderer;
    Body sun, planet, asteroid;
    ArrayList<Body> bodies;
    boolean running, launching, pickingOrbit, chaseCamOn, addingBody, addingBodyMatrix;
    ArrayList<CircleShape> circles;
    int selectedBody;
    HUD hud;
    ExtendViewport hudViewport;

    SpriteBatch batch, hudBatch;
    Texture earth;
    ArrayList<DynamicSprite> sprites;



    public GameScreen(final GameActivity game) {
        this.game = game;

        //debug renderer is used for box2d debugging. shows shape outlines around bodies
        debugRenderer = new Box2DDebugRenderer();

        //get screen dimensions
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();
        //define initial world dimensions for the camera. These can be adjusted later when a user zooms or resizes the window.
        worldHeight = 90;
        worldWidth = worldHeight / screenHeight * screenWidth;


        // create the camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, worldWidth, worldHeight); //give camera initial world dimensions

        //create box2d world. Assign gravity 0 vector
        world = new World(new Vector2(0, 0), true);
        //define array lists for bodies and circles. Bodies holds all of the planets, suns, and asteroids.
        bodies = new ArrayList<Body>();
        //circles holds all of the circle shape objects we create that will later need to be disposed of
        circles = new ArrayList<CircleShape>();

        batch = new SpriteBatch();
        // We need a sprite since it's going to move
        earth = new Texture("gfx/earth-cartoon-md.png");
        sprites = new ArrayList<DynamicSprite>();

        //create initial bodies
        sun = createCircle(100 * STANDARD_MASS, worldWidth /2, worldHeight /2 );
        bodies.add(sun);
        sun.setLinearDamping(100000f);
        planet = createCircle(STANDARD_MASS, sun.getWorldCenter().x + 100, worldHeight /2 );
        bodies.add(planet);
        asteroid = createCircle(STANDARD_MASS / 100, planet.getWorldCenter().x + 20, worldHeight /2);
        bodies.add(asteroid);

        //make planet orbit sun and asteroid orbit planet
        orbit(planet, sun);
        orbit(asteroid, planet);

        //no body is currently selected. This is used for lots of functionality. e.g. launching and scaling
        setSelectedBody(-1);

        //launching is false. This is true if the user is launching a planet
        launching = false;
        //running is true
        running = true;

        //create separate sprite batch for HUD
        hudBatch = new SpriteBatch();

        hudHeight = 300;
        hudWidth = hudHeight*screenWidth/screenHeight;

        OrthographicCamera hudCam = new OrthographicCamera(hudWidth, hudHeight);
        hudViewport = new ExtendViewport(hudWidth, hudHeight, hudCam);

        hud = new HUD(this, hudViewport);


        //create stuff to measure input (taps, keys, mouse clicks, etc)
        InputMultiplexer im = new InputMultiplexer();
        GestureDetector gd = new GestureDetector(this);
        im.addProcessor(hud);
        im.addProcessor(gd);
        im.addProcessor(this);

        Gdx.input.setInputProcessor(im);

        timeStep = TIMESTEP;

    }

    public void pauseGame() {
        running = false;
        timeStep = 0;
    }

    public void unpauseGame() {
        running = true;
        timeStep = TIMESTEP;
    }

    /**
     * This method is called every frame to check for zooming. This way zoom can be continuous.
     * TODO: set this to pinch when no planet is selected
     */
    private void handleZoom() {
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            //If the DOWN Key is pressed, zoom out
            setZoom(getZoom() + 0.2f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            //If the UP Key is pressed, zoom in
            setZoom(getZoom() - 0.2f);
        }
    }

    /**
     * This is a setter method for the zoom property of the camera
     * @param zoom the zoom with 1 = normal. 2 = 2x zoom
     */
    private void setZoom(float zoom) {
        camera.zoom = zoom;
        worldHeight = camera.viewportHeight;
        worldWidth = camera.viewportWidth;
    }

    /**
     * this is a getter method for the zoom property of the camera
     * @return the zoom
     */
    private float getZoom() {
        return camera.zoom;
    }

    /**
     * This method pans the camera by world coordinates deltaX, deltaY
     * @param deltaX
     * @param deltaY
     */
    private void translateCamera(float deltaX, float deltaY) {
        camera.translate(deltaX,deltaY);
        // tell the camera to update its matrices.
        camera.update();
    }

    /**
     * This pans the camera by the vector delta
     * @param delta
     */
    private void translateCamera(Vector2 delta) {
        translateCamera(delta.x, delta.y);
    }

    /**
     * This method centers the camera back on the main sun.
     */
    public void centerCamera() {
        centerCamera(sun.getWorldCenter());
    }

    /**
     * This method centers camera on pos
     * @param pos Vector2 position to center camera at
     */
    public void centerCamera(Vector2 pos) {
        centerCamera(pos.x, pos.y);
    }

    /**
     * This method centers camera on (x,y)
     * @param x
     * @param y
     */
    public void centerCamera(float x, float y) {
        camera.position.x = x;
        camera.position.y = y;
    }

    /**
     * This method resets the camera back to the original position and zoom (centered on the main sun with zoom = 1)
     */
    private void resetCamera() {
        centerCamera();
        camera.zoom = 1;
    }


    /**
     * this method creates a new body and adds it to the bodies arraylist
     * @param mass
     * @param x x position
     * @param y y position
     * @return the new body
     */
    private Body createBody(float mass, float x, float y) {
        Body body = createCircle(mass, x, y);
        bodies.add(body);
        return body;
    }

    /**
     * This method creates an nxn matrix of bodies. All have mass "mass". The top-leftmost is located at (x,y)
     * @param mass
     * @param x
     * @param y
     * @param n
     * @return an arraylist with all the bodies in the matrix
     */
    private ArrayList<Body> createBodyMatrix(float mass, float x, float y, float n) {
        float radius = getCircleRadius(mass);
        ArrayList<Body> matrixBodies = new ArrayList<Body>();
        for(int i = 0; i < n; i++) {
            for(int j = 0; j < n; j++) {
                Body newBody = createBody(mass, x + radius * 2*i, y + radius * 2*j);
                matrixBodies.add(newBody);
            }
        }
        return matrixBodies;
    }
    /**
     * Make body "planet" orbit body "sun". This method uses the planet's radius from the sun and the
     * sun's mass to calculate what tangential velocity the planet needs to have to orbit the sun. It
     * then gives the planet that velocity.
     * @param planet
     * @param sun
     */
    private void orbit(Body planet, Body sun) {
        float sun_mass = sun.getMass();
        float distance = sun.getWorldCenter().dst(planet.getWorldCenter()) / SIZE_ADJUSTMENT_FACTOR;
        //mv^2/r = G(m1)(m2)/r^2
        float velocity_magnitude = (float)Math.sqrt(GRAVITY_CONSTANT * sun_mass / distance);

        Vector2 r = sun.getWorldCenter().sub(planet.getWorldCenter());

        Vector2 r_hat = r.nor();

        Vector2 v_hat = r_hat.rotate90(-1);

        Vector2 velocity = v_hat.scl(velocity_magnitude);

        planet.setLinearVelocity(velocity);

    }

    /**
     * This method creates a circlular body of mass "mass" and coordinates x,y
     * @param mass
     * @param x
     * @param y
     * @return The circular body
     */
    private Body createCircle(float mass, float x, float y) {

        //mass = mass * SIZE_ADJUSTMENT_FACTOR;

        // First we create a body definition
        BodyDef bodyDef = new BodyDef();
        // We set our body to dynamic so it can experience forces, for something like ground which doesn't move we would set it to StaticBody
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        // Set our body's starting position in the world
        bodyDef.position.set(x, y);

        // Create our body in the world using our body definition
        Body body = world.createBody(bodyDef);

        // Create a circle shape and set its radius
        CircleShape circle = new CircleShape();
        circles.add(circle); //add the circle to the circles arraylist so that it can be disposed of on close
        double radius = getCircleRadius(mass); //get the circle's radius based on mass. This function uses a mass-volume conversion
        circle.setRadius((float)radius);


        // Use the mass to calculate density based on radius
        double density = mass / (Math.PI * Math.pow(radius, 2));

        // Create a fixture definition to apply our shape to. This is what gives a body its properties
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = (float)density;
        fixtureDef.friction = 5; //coefficient of friction on the body's surface

        // Create our fixture and attach it to the body
        body.createFixture(fixtureDef);

        //Now we create a sprite (graphic) for the body
        DynamicSprite sprite = new DynamicSprite(earth); //the dynamic sprite class is optimized for use with bodies
        sprite.attachBody(body);
        sprite.setPosition(body.getPosition().x, body.getPosition().y);
        sprites.add(sprite);

        //attach the sprite to the body so it can be identified later
        body.setUserData(sprite);


        return body;
    }

    /**
     * This method calculates a circle's (or actually a sphere's) radius based on a given mass
     * @param mass
     * @return the radius
     */
    private float getCircleRadius(float mass) {
        return (float)(Math.pow(3*mass * SIZE_ADJUSTMENT_FACTOR/4/Math.PI/STANDARD_DENSITY, 1/3.0));// * SIZE_ADJUSTMENT_FACTOR);
    }

    /**
     * This method changes a body (planet, sun, etc) mass by deltaMass
     * @param body The body whom's mass to change
     * @param deltaMass The amount to change it by
     */
    private void changeBodyMass(Body body, float deltaMass) {
        //get the fixture and shape of the body
        Fixture fixture = body.getFixtureList().get(0); //bodies can have multiple fixtures so the get() method returns a list. We want the fixture at index 0.
        Shape circle = fixture.getShape();
        float mass = body.getMass();

        mass = mass + deltaMass; //adjust mass

        //mass has a lower limit of 1 and an upper limit of 1,000,000 inclusive
        if (mass < 1) {
            mass = 1;
        } else if (mass > 1000000) {
            mass = 1000000;
        }

        //recalculate radius and density based on new mass
        double radius = getCircleRadius(mass);

        double density = mass / (Math.PI * Math.pow(radius, 2));

        //assign new properties
        fixture.setDensity((float) density);
        circle.setRadius((float) radius);

        //reset mass data of the body so that changes can take effect
        body.resetMassData();
    }


    private float accumulator = 0;

    /**
     * This method advances the world by TIMESTEP. It adjusts for lag.
     * @param deltaTime time elapsed since last call
     */
    private void doPhysicsStep(float deltaTime) {
        // fixed time step
        // max frame time to avoid spiral of death (on slow devices)
        float frameTime = Math.min(deltaTime, 0.25f);
        accumulator += frameTime;
        while (accumulator >= TIMESTEP) {
            world.step(TIMESTEP, 6, 2);
            accumulator -= TIMESTEP;
        }
    }

    /**
     * This method selects a body so that it can be launched, scaled in size, followed with the camera, or given an orbit.
     * @param bodyIndex the index corresponding to the body in the bodies arraylist. -1 = no body selected
     */
    private void setSelectedBody(int bodyIndex) {
        selectedBody = bodyIndex;
        //TODO: Add graphics to support selecting body
    }

    /**
     * This method sets the launching bool
     * @param bool
     */
    private void setLaunching(boolean bool) {
        launching = bool;
        //TODO Add visual
    }

    /**
     * Sets the addingBody bool. When true, users can click to add bodies.
     * @param bool
     */
    private void setAddingBody(boolean bool) {
        addingBody = bool;
        //TODO Add visual
    }

    /**
     * Sets the addingBodyMatrix bool. When true, users can click to add matrices of bodies.
     * @param bool
     */
    private void setAddingBodyMatrix(boolean bool) {
        addingBodyMatrix = bool;
        //TODO Add visual
    }


    /**
     * This method returns a boolean saying whether any body is selected (true) or not (false)
     * @return
     */
    private boolean isBodySelected() {
        return selectedBody != -1;
    }

    /**
     * This method sets the boolean pickingOrbit. This is true when the user has a selected body and they are in the process of
     * selecting another body for that body to orbit
     * @param bool
     */
    private void setPickingOrbit(boolean bool) {
        pickingOrbit = bool;
        //TODO: Add graphics to support picking orbit
    }

    /**
     * This method applies gravity the gravity from body1 to body2. It uses newton's law with G = 1.
     * @param body1
     * @param body2
     */
    public void applyGravityBetweenBodies(Body body1, Body body2) {
        float m1 = body1.getMass();
        float m2 = body2.getMass();
        Vector2 r = body1.getWorldCenter().sub(body2.getWorldCenter());

        //get r magnitude. It is important to get this before getting r_hat because calling r.nor() will actually normalize the r vector, not just return r_hat
        float r_mag = r.len() / (float)Math.sqrt(SIZE_ADJUSTMENT_FACTOR);

        //get r unit vector
        Vector2 r_hat = r.nor();

        //F = G(m1)(m2) / ||r||^2 * r_hat
        Vector2 f = r_hat.scl((float)(GRAVITY_CONSTANT * m1 * m2 / Math.pow(r_mag, 2)));

        //apply the force to body2
        body2.applyForceToCenter(f.x, f.y, true);

    }

    /**
     * This method scales vector screenPosition to get a vector representing its WORLD coordinates.
     * This is important because when registering locations of inputs (taps, pans, pinches, etc) the
     * functions give the position on the SCREEN in pixels instead of the scaled position in our Box2D
     * world.
     * @param screenPosition
     * @return
     */
    public Vector2 getWorldPosition(Vector2 screenPosition) {
        Vector3 pos = new Vector3(screenPosition, 0);
        camera.unproject(pos);
        Vector2 worldPosition = new Vector2(pos.x, pos.y);
        return worldPosition;
    }

    /**
     * This method performs the opposite of getWorldPosition(Vector2 screenPosition). It takes a
     * screen position as input and returns the world position. This is used to place sprites in the
     * correct location in the world (sprites are placed using screen coordinates).
     * @param worldPosition
     * @return
     */
    public Vector2 getScreenPosition(Vector2 worldPosition) {
        Vector3 pos = new Vector3(worldPosition, 0);
        camera.project(pos);
        Vector2 screenPosition = new Vector2(pos.x, pos.y);
        return screenPosition;
    }

    /**
     * This method scales a given screen distance to the world distance. The screen (measured in pixels)
     * has a different scale than the box2d world (meters)
     * @param distance
     * @return
     */
    public float scaleDistanceToWorld(float distance) {
        return distance * camera.viewportHeight*(1/getZoom() / screenHeight);
    }

    /**
     * This method scales a given world distance to the screen distance. The screen (measured in pixels)
     * has a different scale than the box2d world (meters)
     * @param distance
     * @return
     */
    public float scaleDistanceToScreen(float distance) {
        return distance * screenHeight / camera.viewportHeight*(1/getZoom()); //zoom needs to be accounted for
    }

    /**
     * This method runs continuously in an endless loop while the game is running.
     * @param delta time elapsed since last call. Used for frames
     */
    @Override
    public void render(float delta) {
        // clear the screen with a dark blue color. The
        // arguments to glClearColor are the red, green
        // blue and alpha component in the range [0,1]
        // of the color to be used to clear the screen.
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        //calling this method checks to see if the user is zooming. We want it to be a continuous zoom.
        handleZoom();

        //make camera follow selected planet if chaseCamOn
        if(isBodySelected() && chaseCamOn)
            centerCamera(bodies.get(selectedBody).getWorldCenter());

        // tell the camera to update its matrices.
        camera.update();

        //if the game is running
        if(running) {
            //for every body, apply gravity between itself and all the other bodies
            for (Body body1 : bodies) {
                for (Body body2 : bodies) {
                    if (body1 != body2) { //don't apply a body's own gravity to itself.
                        applyGravityBetweenBodies(body1, body2);
                    }
                }
            }
        }

        //debugRenderer.render(world, camera.combined);

        //Gdx.app.log("GameScreen", "delta = " + delta);

        //update all the sprites
        batch.begin();
        batch.setProjectionMatrix(camera.combined);
        for(DynamicSprite sprite : sprites) {
            sprite.update();
            sprite.draw(batch);
        }
        batch.end();


        //advance world by TIMESTEP (1/60 second)
        doPhysicsStep(timeStep);

        hud.act(TIMESTEP);
        hud.draw();
    }

    /**
     * This method is called when a user resizes the game on their computer. It adjusts the world dimensions
     * to maintain the right ratio
     * @param width
     * @param height
     */
    @Override
    public void resize(int width, int height) {
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        worldWidth = worldHeight / screenHeight * screenWidth;

        camera.setToOrtho(false, worldWidth, worldHeight);
        centerCamera(); //put the sun at the middle

        //float hudHeight = 300;
        //float hudWidth = hudHeight*screenWidth/screenHeight;

        hud.getViewport().update(width, height, true);




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

    /**
     * This method disposes of all visual elements when the game is closed
     */
    @Override
    public void dispose() {
        //dispose of circle shapes
        for (CircleShape circle : circles) {
            circle.dispose();
        }

        earth.dispose();
    }

    /**
     * This method is called when a user presses down on a key
     * @param keycode
     * @return
     */
    @Override
    public boolean keyDown (int keycode) {
        Gdx.app.log("GameScreen", "keyDown registered - keycode = " + keycode);

        if (keycode == Input.Keys.O) {
            //If the O key is pressed and a body is selected, turn on orbit selection. Now the next body tapped will be orbited by the selected planet
            if (isBodySelected()) {
                if(!pickingOrbit) {
                    setPickingOrbit(true);
                } else {
                    setPickingOrbit(false);
                }
            }
        }

        if (keycode == Input.Keys.C) {
            //If the C key is pressed, center camera and reset zoom
            resetCamera();
        }

        if (keycode == Input.Keys.F) {
            //If the F key is pressed, toggle camera following selected planet
            chaseCamOn = !chaseCamOn;
        }

        if (keycode == Input.Keys.N) {
            //If the N key is pressed, toggle adding new bodies
            setAddingBody(!addingBody);
            setAddingBodyMatrix(false);
            setSelectedBody(-1);
        }

        if (keycode == Input.Keys.M) {
            //If the M key is pressed, toggle adding matrices of bodies
            setAddingBodyMatrix(!addingBodyMatrix);
            setAddingBody(false);
            setSelectedBody(-1);
        }

        if (keycode == Input.Keys.P) {
            //If the P key is pressed, toggle playing/pausing of game
            if(running) {
                pauseGame();
            } else {
                unpauseGame();
            }
        }
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


    /**
     * This method is called when the user presses down on the screen or clicks.
     * @param x
     * @param y
     * @param pointer
     * @param button
     * @return
     */
    @Override
    public boolean touchDown (int x, int y, int pointer, int button) {

        Vector2 touchLocation = new Vector2(x, y);

        touchLocation = getWorldPosition(touchLocation); //adjust touch location to world coordinates

        Gdx.app.log("GameScreen", "touchDown registered at (" + touchLocation.x + "," + touchLocation.y + ")");

        float shortestDistance = Integer.MAX_VALUE;
        int tappedBody = -1;
        //check to see if any of the bodies were tapped.
        for(int i = 0; i < bodies.size(); i++) {
            Body body = bodies.get(i);
            Vector2 bodyCenter = body.getWorldCenter();
            float bodyRadius = body.getFixtureList().get(0).getShape().getRadius();
            float touchDistanceFromBodyCenter = Math.abs(bodyCenter.dst(touchLocation)); //get touch distance from the center of the body
            //the tap is considered to be inside the body if it is within the radius or 50 world units
            boolean tapInsideBody = touchDistanceFromBodyCenter < bodyRadius || touchDistanceFromBodyCenter < 10 * getZoom();
            if(tapInsideBody && touchDistanceFromBodyCenter < shortestDistance) { //we want to find the closest body to our tap in case multiple bodies are in range
                tappedBody = i;
                shortestDistance = touchDistanceFromBodyCenter;
            }
        }
        //if a body was tapped select the tapped body (this is the closest one to the tap)
        if(tappedBody >= 0 && !launching && selectedBody == tappedBody) {
            setLaunching(true);
        }
        return false;
    }

    /**
     * called when a user releases their finger from the screen
     * @param x
     * @param y
     * @param pointer
     * @param button
     * @return
     */
    @Override
    public boolean touchUp (int x, int y, int pointer, int button) {

        //if the user is currently launching a planet (their touch down was on a planet), launch the planet
        if(launching && isBodySelected()) {
            Body body = bodies.get(selectedBody);
            Vector2 releaseLocation = new Vector2(x, y);
            releaseLocation = getWorldPosition(releaseLocation); //convert release location to world coords

            Gdx.app.log("GameScreen", "touchUp registered at (" + releaseLocation.x + "," + releaseLocation.y + ")");

            Gdx.app.log("GameScreen", "launch release location = (" + x + "," + y + ")");
            Vector2 bodyLocation = body.getWorldCenter();
            Gdx.app.log("GameScreen", "body center = (" + bodyLocation.x + "," + bodyLocation.y + ")");

            Vector2 launchVector = releaseLocation.sub(bodyLocation); //create launch vector
            Gdx.app.log("GameScreen", "launchVector = (" + launchVector.x + "," + launchVector.y + ")");

            launchVector.scl(-1); //invert it so that the body launches away from the direction pulled. It's like pulling back a slingshot

            if(body.getMass() > STANDARD_MASS) {
                // scale the acceleration so that bigger bodies have lower accelerations for the same pull
                float scaleFactor = (float)(Math.pow(STANDARD_MASS/body.getMass(), 1/7.0));
                launchVector = launchVector.scl(scaleFactor);
            }

            //assign launch vector as new velocity for the body
            body.setLinearVelocity(launchVector);

            setLaunching(false);
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

    /**
     * Called on scrolling of the mouse wheel
     * @param amount
     * @return
     */
    @Override
    public boolean scrolled (int amount) {
        Gdx.app.log("GameScreen", "scroll registered - amount = " +  amount);

        //TODO scale mass of selected body

        if(isBodySelected()) {

            Body body = bodies.get(selectedBody);

            float scaled_amount = 10 * (float) Math.sqrt(body.getMass()) * amount;

            changeBodyMass(body, scaled_amount);

            Gdx.app.log("GameScreen", "Mass adjusted - mass = " + body.getMass());
        } else {
            setZoom(getZoom() + amount * 0.1f);
        }


        return false;
    }

    @Override
    public boolean touchDown (float x, float y, int pointer, int button) {
        Gdx.app.log("GameScreen", "touchDown registered");
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {

        Vector2 touchLocation = new Vector2(x, y);

        touchLocation = getWorldPosition(touchLocation); //adjust touch location to world coordinates

        Gdx.app.log("GameScreen", "tap registered at (" + touchLocation.x + "," + touchLocation.y + ")");

        float shortestDistance = Integer.MAX_VALUE;
        int tappedBody = -1;
        //check to see if any of the bodies were tapped.
        for(int i = 0; i < bodies.size(); i++) {
            Body body = bodies.get(i);
            Vector2 bodyCenter = body.getWorldCenter();
            float bodyRadius = body.getFixtureList().get(0).getShape().getRadius();
            float touchDistanceFromBodyCenter = Math.abs(bodyCenter.dst(touchLocation)); //get touch distance from the center of the body
            //the tap is considered to be inside the body if it is within the radius or 50 world units
            boolean tapInsideBody = touchDistanceFromBodyCenter < bodyRadius || touchDistanceFromBodyCenter < 10 * getZoom();
            if(tapInsideBody && touchDistanceFromBodyCenter < shortestDistance) { //we want to find the closest body to our tap in case multiple bodies are in range
                tappedBody = i;
                shortestDistance = touchDistanceFromBodyCenter;
            }
        }
        //if a body was tapped, tappedBody is the index of the tapped body in the bodies arraylist
        if(tappedBody >= 0) {
            //if the user is selecting an orbit for the already selected planet
            if(pickingOrbit) {
                //if they didn't tap the body already selected
                if(tappedBody != selectedBody) {
                    //make that selected planet orbit the tapped body
                    orbit(bodies.get(selectedBody), bodies.get(tappedBody));

                }
                // if they just picked an orbit or they tapped the planet already selected, turn off orbit selecting
                setPickingOrbit(false);
            } else {
                //if they aren't selecting an orbit, make the tapped body the new globally selected body
                setSelectedBody(tappedBody);
            }
        } else {
            //if they didn't select a planet
            if(pickingOrbit) {
                //if picking orbit, stop picking orbit but keep the current planet selected
                setPickingOrbit(false);
            } else if(addingBody) {
                //add new body at the tap location with STANDARD_MASS
                createBody(STANDARD_MASS, touchLocation.x, touchLocation.y);
                setSelectedBody(bodies.size() - 1); //select the new body
            } else if(addingBodyMatrix) {
                //add new body at the tap location with STANDARD_MASS
                createBodyMatrix(STANDARD_MASS, touchLocation.x, touchLocation.y, BODY_MATRIX_N);
                setSelectedBody(-1); //select no body
            } else {
                //otherwise, deselect the planet. The player can now zoom and pan around
                setSelectedBody(-1);
            }

        }



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

    /**
     * This method is called when a user drags their finger/mouse across the screen after tapping/clicking down without releasing
     * @param x
     * @param y
     * @param deltaX
     * @param deltaY
     * @return
     */
    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        Gdx.app.log("GameScreen", "pan registered");
        //if the user isn't currently launching a planet, we want them to be able to pan around the world
        if(!launching) {
            //convert pan's screen coordinates to world coordinates
            Vector2 pos = new Vector2(x,y);
            pos = getWorldPosition(pos);
            Vector2 pos0 = new Vector2(x-deltaX,y-deltaY);
            pos0 = getWorldPosition(pos0);

            //create pan vector
            Vector2 delta = pos.sub(pos0).scl(-1);
            translateCamera(delta); // pan camera
        }
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

    /**
     * Called when a user pinches their fingers on their touch screen
     * @param initialFirstPointer
     * @param initialSecondPointer
     * @param firstPointer
     * @param secondPointer
     * @return
     */
    @Override
    public boolean pinch (Vector2 initialFirstPointer, Vector2 initialSecondPointer, Vector2 firstPointer, Vector2 secondPointer){
        Gdx.app.log("GameScreen", "pinch registered");

        //TODO: Fix this up BIG TIME

        float initialDistance = Math.abs(initialFirstPointer.dst(initialSecondPointer));
        float finalDistance = Math.abs(firstPointer.dst(secondPointer));

        float changeInDistance = finalDistance - initialDistance;

        changeBodyMass(planet, changeInDistance);

        setLaunching(false); //make sure this scaling isn't registered as a launch

        return false;
    }

}
