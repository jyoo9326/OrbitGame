package com.game.nick.orbit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by Nick on 4/23/2016.
 */
public class HUD extends Stage {

    final float MENU_ANIMATION_TIME = 0.1f;

    TextButton
            menuButton,
            editButton,
            viewButton,
            addButton,
            settingsButton;
    TextButton
            addSingleButton,
            addMultipleButton;
    TextButton
            scaleButton,
            velocityButton,
            orbitButton,
            infoButton,
            deleteButton;
    TextButton
            zoomButton,
            panButton,
            resetButton,
            centerButton;

    TextButton.TextButtonStyle textButtonStyle;
    BitmapFont font;
    GameScreen gameScreen;
    Table closedTable, openTable, addTable, editTable, viewTable, settingsTable;

    boolean menuOpen;

    float menuDisplacement;

    public HUD(final GameScreen gameScreen, Viewport viewport) {
        super(viewport);

        this.gameScreen = gameScreen;

        font = new BitmapFont();
        textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = font;

        menuButton = new TextButton("Menu", textButtonStyle);
        createMenuButtonFunctionality();
        addButton = new TextButton("Add", textButtonStyle);
        editButton = new TextButton("Edit", textButtonStyle);
        viewButton = new TextButton("View", textButtonStyle);
        settingsButton = new TextButton("Settings", textButtonStyle);


        openTable = new Table();
        openTable.setDebug(true); // turn on all debug lines (table, cell, and widget)
        addCoreButtons(openTable);
        openTable.setFillParent(true);
        openTable.right().bottom();
        addActor(openTable);
        openTable.setVisible(true);

        float x = 0;
        for(int i = 1; i < openTable.getCells().size; i++) {
            x += openTable.getCells().get(i).getActor().getWidth();
        }

        menuDisplacement = x;

        openTable.setPosition(menuDisplacement, 0);


    }


    public void openMenu() {

        if(!menuOpen) {
            openTable.addAction(Actions.moveTo(0, 0, MENU_ANIMATION_TIME));
            //openTable.setPosition(0,0);
        } else {
            openTable.addAction(Actions.moveTo(menuDisplacement, 0, MENU_ANIMATION_TIME));
            //openTable.setPosition(100,0);
        }
        menuOpen = !menuOpen;

    }

    private void addCoreButtons(Table table) {
        table.add(menuButton);
        table.add(addButton);
        table.add(editButton);
        table.add(viewButton);
        table.add(settingsButton);
    }

    private void createMenuButtonFunctionality() {
        menuButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                openMenu();
            }
        });
    }

    private void createAddButtonFunctionality() {
        addButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

            }
        });
    }

    private void createEditButtonFunctionality() {
        editButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

            }
        });
    }

    private void createViewButtonFunctionality() {
        viewButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

            }
        });
    }

    private void createSettingsButtonFunctionality() {
        settingsButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

            }
        });
    }
}
