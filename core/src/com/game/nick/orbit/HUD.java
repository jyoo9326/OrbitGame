package com.game.nick.orbit;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by Nick on 4/23/2016.
 */
public class HUD extends Stage {

    final float MENU_ANIMATION_TIME = 0.1f;

    public enum SubMenu {
        NONE, ADD, EDIT, VIEW, SETTINGS
    }

    SubMenu subMenuOpen;

    public enum ClickFunction {
        NONE, ADD_SINGLE, ADD_MULTIPLE, ORBIT
    }

    ClickFunction currentClickFunction;



    TextButton //main menu buttons
            menuButton,
            editButton,
            viewButton,
            addButton,
            settingsButton;
    TextButton //add sub-menu buttons
            addSingleButton,
            addMultipleButton;
    TextButton //edit sub-menu buttons
            scaleButton,
            velocityButton,
            orbitButton,
            stickyButton,
            infoButton,
            deleteButton;
    TextButton //view sub-menu buttons
            zoomButton,
            panButton,
            resetButton,
            centerButton;
    TextButton //settings sub-menu buttons
            deleteAllButton,
            optionsButton,
            helpButton,
            appInfoButton;

    TextButton.TextButtonStyle textButtonStyle;
    BitmapFont font;
    GameScreen gameScreen;
    Table mainTable, addTable, editTable, viewTable, settingsTable;

    boolean menuOpen;

    float menuDisplacement, submenuYDisplacement, editDisplacement, addDisplacement, viewDisplacement, settingsDisplacement;

    public HUD(final GameScreen gameScreen, Viewport viewport) {
        super(viewport);

        this.gameScreen = gameScreen; //the game screen that this HUD will be used for

        font = new BitmapFont(); //default font
        textButtonStyle = new TextButton.TextButtonStyle(); //create style for buttons
        textButtonStyle.font = font; //assign style the font

        //MAIN MENU

        menuButton = new TextButton("Menu", textButtonStyle);
        createMenuButtonFunctionality();
        addButton = new TextButton("Add", textButtonStyle);
        createAddButtonFunctionality();
        editButton = new TextButton("Edit", textButtonStyle);
        createEditButtonFunctionality();
        viewButton = new TextButton("View", textButtonStyle);
        createViewButtonFunctionality();
        settingsButton = new TextButton("Settings", textButtonStyle);
        createSettingsButtonFunctionality();

        mainTable = new Table();  //create a table to hold all of the buttons for the main menu
        mainTable.setDebug(true); // turn on all debug lines (table, cell, and widget)

        mainTable.add(menuButton); //add the buttons to the table (left to right)
        mainTable.add(addButton);
        mainTable.add(editButton);
        mainTable.add(viewButton);
        mainTable.add(settingsButton);

        mainTable.setFillParent(true); //make the table boundaries fill the screen
        mainTable.right().bottom(); //put all of the table cells (buttons) in the bottom right corner
        addActor(mainTable); //add the table to the HUD (stage)

        // calculate the displacement of main menu table when it is contracted
        float x = 0;
        for(int i = 1; i < mainTable.getCells().size; i++) {
            //we just want to add the width of all the cells except for the first
            x += mainTable.getCells().get(i).getActor().getWidth();
        }
        menuDisplacement = x;

        //y displacement for sub-menus is equal to the height of the main menu
        submenuYDisplacement = mainTable.getCells().get(0).getActor().getHeight();

        //set the main menu at the contracted position
        mainTable.setPosition(menuDisplacement, 0);


        //ADD SUB MENU

        addSingleButton = new TextButton("Single", textButtonStyle);
        createAddSingleButtonFunctionality();
        addMultipleButton = new TextButton("Multiple", textButtonStyle);
        createAddMultipleButtonFunctionality();

        addTable = new Table(); //All of the sub-menus will also be tables
        addTable.setDebug(true);

        addTable.add(addSingleButton); //The "Add bodies" sub-menu will have 2 buttons
        addTable.add(addMultipleButton);

        addTable.setFillParent(true); //same as the main menu, we want to fill the screen with the boundaries of the table
        addTable.right().bottom(); //position the cells (buttons) in the bottom right corner
        addActor(addTable); //add the table to the HUD

        //calculate displacement of table when contracted
        x = 0;
        for(int i = 0; i < addTable.getCells().size; i++) {
            //add the width of all sub-menu buttons
            x += addTable.getCells().get(i).getActor().getWidth();
        }
        addDisplacement = x;

        //position Add sub-menu in contracted position
        addTable.setPosition(addDisplacement, submenuYDisplacement);


        //EDIT SUB MENU

        scaleButton = new TextButton("Scale", textButtonStyle);
        velocityButton = new TextButton("Velocity", textButtonStyle);
        orbitButton = new TextButton("Orbit", textButtonStyle);
        stickyButton = new TextButton("Sticky", textButtonStyle);
        infoButton = new TextButton("Info", textButtonStyle);
        deleteButton = new TextButton("Delete", textButtonStyle);

        editTable = new Table();
        editTable.setDebug(true);

        editTable.add(scaleButton);
        editTable.add(velocityButton);
        editTable.add(orbitButton);
        editTable.add(stickyButton);
        editTable.add(infoButton);
        editTable.add(deleteButton);

        editTable.setFillParent(true);
        editTable.right().bottom();
        addActor(editTable);

        x = 0;
        for(int i = 0; i < editTable.getCells().size; i++) {
            x += editTable.getCells().get(i).getActor().getWidth();
        }
        editDisplacement = x;

        editTable.setPosition(editDisplacement, submenuYDisplacement);


        //VIEW SUB MENU

        zoomButton = new TextButton("Zoom", textButtonStyle);
        panButton = new TextButton("Pan", textButtonStyle);
        resetButton = new TextButton("Reset", textButtonStyle);
        centerButton = new TextButton("Center", textButtonStyle);

        viewTable = new Table();
        viewTable.setDebug(true);

        viewTable.add(zoomButton);
        viewTable.add(panButton);
        viewTable.add(resetButton);
        viewTable.add(centerButton);

        viewTable.setFillParent(true);
        viewTable.right().bottom();
        addActor(viewTable);

        x = 0;
        for(int i = 0; i < viewTable.getCells().size; i++) {
            x += viewTable.getCells().get(i).getActor().getWidth();
        }
        viewDisplacement = x;

        viewTable.setPosition(viewDisplacement, submenuYDisplacement);


        //SETTINGS SUB MENU

        deleteAllButton = new TextButton("Delete All", textButtonStyle);
        optionsButton = new TextButton("Options", textButtonStyle);
        helpButton = new TextButton("Help", textButtonStyle);
        appInfoButton = new TextButton("App Info", textButtonStyle);

        settingsTable = new Table();
        settingsTable.setDebug(true);

        settingsTable.add(deleteAllButton);
        settingsTable.add(optionsButton);
        settingsTable.add(helpButton);
        settingsTable.add(appInfoButton);

        settingsTable.setFillParent(true);
        settingsTable.right().bottom();
        addActor(settingsTable);

        x = 0;
        for(int i = 0; i < settingsTable.getCells().size; i++) {
            x += settingsTable.getCells().get(i).getActor().getWidth();
        }
        settingsDisplacement = x;

        settingsTable.setPosition(settingsDisplacement, submenuYDisplacement);



        subMenuOpen = SubMenu.NONE;
        currentClickFunction = ClickFunction.NONE;
    }


    /**
     * This method opens and closes the main menu. Called every time the menu button is clicked.
     */
    public void mainMenu() {

        if(!menuOpen) {
            mainTable.addAction(Actions.moveTo(0, 0, MENU_ANIMATION_TIME));
            gameScreen.pauseGame();
        } else {
            mainTable.addAction(Actions.moveTo(menuDisplacement, 0, MENU_ANIMATION_TIME));
            closeSubMenu(subMenuOpen);
            deactivateClickFunction(currentClickFunction);
            gameScreen.unpauseGame();
        }
        menuOpen = !menuOpen;

    }

    /**
     * This method is called whenever one of the sub-menu buttons is hit. If the clicked menu isn't open
     * it opens it. Otherwise, it closes it. If a menu is open already and a different button is clicked,
     * the open menu is closed, and the new one opened.
     * @param menu the menu to open/close
     */
    public void subMenu(SubMenu menu) {
        if(menu == subMenuOpen) {
            closeSubMenu(subMenuOpen);
        } else {
            closeSubMenu(subMenuOpen);
            openSubMenu(menu);
        }

    }

    /**
     * This method opens the given sub-menu
     * @param menu the sub-menu to open
     */
    public void openSubMenu(SubMenu menu) {
        subMenuOpen = menu;
        switch(menu) {
            case NONE:
                closeSubMenu(subMenuOpen);
                break;
            case ADD:
                addTable.addAction(Actions.moveTo(0, submenuYDisplacement, MENU_ANIMATION_TIME));
                break;
            case EDIT:
                editTable.addAction(Actions.moveTo(0, submenuYDisplacement, MENU_ANIMATION_TIME));
                break;
            case VIEW:
                viewTable.addAction(Actions.moveTo(0, submenuYDisplacement, MENU_ANIMATION_TIME));
                break;
            case SETTINGS:
                settingsTable.addAction(Actions.moveTo(0, submenuYDisplacement, MENU_ANIMATION_TIME));
                break;


        }
    }

    /**
     * This method closes the given sub-menu
     * @param menu the sub_menu to close
     */
    public void closeSubMenu(SubMenu menu) {
        subMenuOpen = SubMenu.NONE;
        switch(menu) {
            case ADD:
                addTable.addAction(Actions.moveTo(addDisplacement, submenuYDisplacement, MENU_ANIMATION_TIME));
                break;
            case EDIT:
                editTable.addAction(Actions.moveTo(editDisplacement, submenuYDisplacement, MENU_ANIMATION_TIME));
                break;
            case VIEW:
                viewTable.addAction(Actions.moveTo(viewDisplacement, submenuYDisplacement, MENU_ANIMATION_TIME));
                break;
            case SETTINGS:
                settingsTable.addAction(Actions.moveTo(settingsDisplacement, submenuYDisplacement, MENU_ANIMATION_TIME));



        }
    }

    /**
     * This method handles toggling of all of the click functions. Functions that require a tap/click.
     * If the passed click function is not already activated, it activates it. Otherwise, it deactivates
     * it. If a different one is activated, it deactivates that one before activating the new one.
     * @param function the click function to (de)activate
     */
    public void clickFunction(ClickFunction function) {
        if(currentClickFunction == function) {
            deactivateClickFunction(function);
        } else {
            deactivateClickFunction(currentClickFunction);
            activateClickFunction(function);
        }
    }

    /**
     * This method activates a desired click function
     * @param function the click function to activate
     */
    public void activateClickFunction(ClickFunction function) {
        currentClickFunction = function;
        switch(function) {
            case ADD_SINGLE:
                gameScreen.setAddingBody(true);
                break;
            case ADD_MULTIPLE:
                gameScreen.setAddingBodyMatrix(true);
                break;
            case ORBIT:
                gameScreen.setPickingOrbit(true);
                break;
        }
    }

    /**
     * THis method deactivates a desired click function
     * @param function the click function to activate
     */
    public void deactivateClickFunction(ClickFunction function) {
        currentClickFunction = ClickFunction.NONE;
        switch (function) {
            case ADD_SINGLE:
                gameScreen.setAddingBody(false);
                break;
            case ADD_MULTIPLE:
                gameScreen.setAddingBodyMatrix(false);
                break;
            case ORBIT:
                gameScreen.setPickingOrbit(false);
                break;
        }
    }


    /**
     * This method creates the functionality for the main menu button. Opens and closes on click.
     */
    private void createMenuButtonFunctionality() {
        menuButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                mainMenu();
            }
        });
    }

    /**
     * This method creates the functionality for the "Add" sub-menu button. Opens/closes the sub-menu
     * on click. If a different one is open, it closes it first.
     */
    private void createAddButtonFunctionality() {
        addButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                subMenu(SubMenu.ADD);
            }
        });
    }

    /**
     * This method creates the functionality for the "Edit" sub-menu button. Opens/closes the sub-menu
     * on click. If a different one is open, it closes it first.
     */
    private void createEditButtonFunctionality() {
        editButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                subMenu(SubMenu.EDIT);
            }
        });
    }

    /**
     * This method creates the functionality for the "View" sub-menu button. Opens/closes the sub-menu
     * on click. If a different one is open, it closes it first.
     */
    private void createViewButtonFunctionality() {
        viewButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                subMenu(SubMenu.VIEW);
            }
        });
    }

    /**
     * This method creates the functionality for the "Settings" sub-menu button. Opens/closes the sub-menu
     * on click. If a different one is open, it closes it first.
     */
    private void createSettingsButtonFunctionality() {
        settingsButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                subMenu(SubMenu.SETTINGS);
            }
        });
    }

    /**
     * This method creates the functionality for the add single body button. It toggles the adding
     * single body on tap setting.
     */
    private void createAddSingleButtonFunctionality() {
        addSingleButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                clickFunction(ClickFunction.ADD_SINGLE);
            }
        });
    }

    /**
     * This method creates the functionality for the add multiple body button. It toggles the adding
     * multiple body on tap setting.
     */
    private void createAddMultipleButtonFunctionality() {
        addMultipleButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                clickFunction(ClickFunction.ADD_MULTIPLE);
            }
        });
    }
}
