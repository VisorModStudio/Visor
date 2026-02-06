package me.phoenixra.visor.core.client.gui.screens;

import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.client.tasks.types.TaskHotBar;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;


public class GameMenuScreen extends Screen {
    private static final Component OPEN_CHAT = Component.literal("Chat");
    private static final Component OPEN_INVENTORY = Component.literal("Inventory");
    private static final Component OPEN_PAUSE_MENU = Component.literal("Main Menu");
    private static final Component OPEN_KEYBOARD = Component.literal("Keyboard");

    public GameMenuScreen() {
        super(Component.literal("Game Menu"));
    }


    @Override
    protected void init() {
        create();
        //@TODO temporary. Get rid of it when player tick tasks
        // start to reset on player world leave
        TaskHotBar.setResetData(true);
        var keyboardAccessor = ClientContext.overlayManager
                .getKeyboardAccessor();
        keyboardAccessor.setVisible(false);
    }

    private void create(){
        GridLayout gridLayout = new GridLayout();
        gridLayout.defaultCellSetting().padding(4, 4, 4, 0);
        GridLayout.RowHelper rowHelper = gridLayout.createRowHelper(2);


        //INVENTORY
        rowHelper.addChild(Button.builder(OPEN_INVENTORY, (button) -> {
                    this.minecraft.setScreen(null);
                    this.minecraft.setScreen(new InventoryScreen(
                            this.minecraft.player)
                    );
                }).width(104).build(),
                2, gridLayout.newCellSettings().paddingTop(50));

        //CHAT
        rowHelper.addChild(new Button.Builder(OPEN_CHAT, (p) ->
        {
            this.minecraft.setScreen(null);
            this.minecraft.setScreen(new ChatScreen(""));
        }).width(104).build(),
                2, gridLayout.newCellSettings().paddingTop(20));
        //---CHAT END


        //KEYBOARD
        rowHelper.addChild(Button.builder(OPEN_KEYBOARD, (button) -> {
                    this.minecraft.setScreen(null);
                    var keyboardAccessor = ClientContext.overlayManager
                            .getKeyboardAccessor();
                    keyboardAccessor.setVisible(true);
                }).width(104).build(),
                2, gridLayout.newCellSettings().paddingTop(20)
        );

        rowHelper.addChild(new Button.Builder(Component.translatable("visor.button.calibrate_height"), (p) ->
        {
            VRClientSettings.calibrateHeight();
            ClientContext.settingsHandler.saveOptions();
            this.minecraft.setScreen(null);
        }).width(104).build(),
                2, gridLayout.newCellSettings().paddingTop(20));

        //PAUSE MENU
        rowHelper.addChild(Button.builder(OPEN_PAUSE_MENU, (button) -> {
                    this.minecraft.setScreen(null);
                    this.minecraft.setScreen(new PauseScreen(true));
        }).width(104).build(),
                2, gridLayout.newCellSettings().paddingTop(20)
        );


        gridLayout.arrangeElements();
        FrameLayout.alignInRectangle(gridLayout, 0, 0, this.width, this.height, 0.5F, 0.25F);
        gridLayout.visitWidgets(this::addRenderableWidget);
    }

}
