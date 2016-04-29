package com.game.nick.orbit;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * This class simulates an expected orbit by mapping it out with dots.
 * Created by Nick on 4/28/2016.
 */
public class LaunchSimulation extends Stage {

    final int NUM_DOTS = 10;
    final float TIME_STEP = 1/60f;

    GameScreen gameScreen;
    Dot[] dots;

    public LaunchSimulation(final GameScreen gameScreen, Viewport viewport) {
        super(viewport);

        this.gameScreen = gameScreen;

        dots = new Dot[NUM_DOTS];
        for(int i = 0; i < dots.length; i++) {
            dots[i] = new Dot(gameScreen.bodyHighlight);
            addActor(dots[i]);
            dots[i].setVisible(false);
        }

    }

    /**
     * This method simulates the orbit for "body" given "velocity". It uses Euler's method to simulate
     * where the body will be at each iteration. It assumes that none of the other bodies on the
     * screen will move. It uses NUM_DOTS Dot objects (basically just an image of a dot) to map out
     * where the body will be over time.
     * @param body the body to simulate
     * @param velocity The velocity as a Vector2 object
     */
    public void doSimulation(Body body, Vector2 velocity) {
        Vector2[] positions = new Vector2[NUM_DOTS*10]; //we will simulate 10 steps in between each dot
        positions[0] = body.getWorldCenter(); //the starting position for the simulation will be the current location of the body
        for(int i = 1; i < positions.length; i++) {
            //update position: x1 = x0 + v0t , x2 = x1 + v1t, x(n) = x(n-1) + v(n-1)*t
            positions[i] = new Vector2(positions[i-1].x + velocity.x * TIME_STEP, positions[i-1].y + velocity.y * TIME_STEP);
            //update velocity for the next position iteration
            for(Body body2 : gameScreen.bodies) { //loop through all of the bodies on the screen for each new
                if(body != body2) { //don't apply gravity between a body and itself
                    float m1 = body.getMass();
                    float m2 = body2.getMass();
                    Vector2 r = body2.getWorldCenter().sub(positions[i]); //radius vector between the body and the latest simulated position

                    //get r magnitude. It is important to get this before getting r_hat because calling r.nor() will actually normalize the r vector, not just return r_hat
                    float r_mag = r.len() / (float) Math.sqrt(gameScreen.SIZE_ADJUSTMENT_FACTOR);

                    //get r unit vector
                    Vector2 r_hat = r.nor();

                    //F = G(m1)(m2) / ||r||^2 * r_hat
                    Vector2 f = r_hat.scl((float) (gameScreen.GRAVITY_CONSTANT * m1 * m2 / Math.pow(r_mag, 2)));

                    //get acceleration from f
                    Vector2 a = f.scl(1 / body.getMass());

                    //add that body's component of the overall velocity change: v1 = v0 + at
                    velocity.add(a.scl(TIME_STEP));
                }
            }
        }

        //now update the positions, visibility, and sizes of the dots
        for(int i = 0; i < dots.length; i++) {
            dots[i].setVisible(true);
            float length = 2 * gameScreen.getZoom();
            dots[i].setSize(length, length);
            Vector2 position = positions[10*i];
            dots[i].setPositionCenter(position.x, position.y);
        }


    }

    /**
     * This method stops the simulation by hiding all of the dots
     */
    public void stopSimulation() {
        for(int i = 0; i < dots.length; i++) {
            dots[i].setVisible(false);
        }
    }

}
