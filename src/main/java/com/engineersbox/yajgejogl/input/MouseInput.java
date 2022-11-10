package com.engineersbox.yajgejogl.input;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2d;
import org.joml.Vector2f;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class MouseInput implements MouseListener, MouseMotionListener {

    private static final Logger LOGGER = LogManager.getLogger(MouseInput.class);

    private final Vector2d previousPos;
    private final Vector2d currentPos;
    private final Vector2f displVec;
    private boolean inWindow = false;
    private boolean leftButtonPressed = false;
    private boolean rightButtonPressed = false;

    public MouseInput() {
        super();
        this.previousPos = new Vector2d(-1, -1);
        this.currentPos = new Vector2d(0, 0);
        this.displVec = new Vector2f();
    }

    public Vector2f getDisplayVec() {
        return this.displVec;
    }

    public void input() {
        this.displVec.x = 0;
        this.displVec.y = 0;
        if (this.previousPos.x > 0 && this.previousPos.y > 0 && this.inWindow) {
            final double deltaX = this.currentPos.x - this.previousPos.x;
            final double deltaY = this.currentPos.y - this.previousPos.y;
            if (deltaX != 0) {
                this.displVec.y = (float) deltaX;
            }
            if (deltaY != 0) {
                this.displVec.x = (float) deltaY;
            }
        }
        this.previousPos.x = this.currentPos.x;
        this.previousPos.y = this.currentPos.y;
    }

    public boolean isLeftButtonPressed() {
        return this.leftButtonPressed;
    }

    public boolean isRightButtonPressed() {
        return this.rightButtonPressed;
    }

    public Vector2d getCurrentPos() {
        return this.currentPos;
    }

    @Override
    public void mouseClicked(final MouseEvent mouseEvent) {

    }

    @Override
    public void mouseEntered(final MouseEvent mouseEvent) {
        this.inWindow = true;
    }

    @Override
    public void mouseExited(final MouseEvent mouseEvent) {
        this.inWindow = false;
    }

    @Override
    public void mousePressed(final MouseEvent mouseEvent) {
//        MouseInput.LOGGER.trace("[MOUSE EVENT] Pressed: [Button: {}] [Position: ({},{})]", mouseEvent.getButton(), mouseEvent.getX(), mouseEvent.getY());
        switch (mouseEvent.getButton()) {
            case MouseEvent.BUTTON1 -> this.leftButtonPressed = true;
            case MouseEvent.BUTTON3 -> this.rightButtonPressed = true;
        }
    }

    @Override
    public void mouseReleased(final MouseEvent mouseEvent) {
//        MouseInput.LOGGER.trace("[MOUSE EVENT] Released: [Button: {}] [Position: ({},{})]", mouseEvent.getButton(), mouseEvent.getX(), mouseEvent.getY());
        switch (mouseEvent.getButton()) {
            case MouseEvent.BUTTON1 -> this.leftButtonPressed = false;
            case MouseEvent.BUTTON3 -> this.rightButtonPressed = false;
        }
    }

    @Override
    public void mouseMoved(final MouseEvent mouseEvent) {
        final int button = mouseEvent.getButton();
//        MouseInput.LOGGER.trace("[MOUSE EVENT] Moved: [Button: {}] [Position: ({},{})]", button, mouseEvent.getX(), mouseEvent.getY());
        this.currentPos.set(
                mouseEvent.getX(),
                mouseEvent.getY()
        );
    }

    @Override
    public void mouseDragged(final MouseEvent mouseEvent) {
        final int button = mouseEvent.getButton();
//        MouseInput.LOGGER.trace("[MOUSE EVENT] Dragged: [Button: {}] [Position: ({},{})]", button, mouseEvent.getX(), mouseEvent.getY());
        this.currentPos.set(
                mouseEvent.getX(),
                mouseEvent.getY()
        );
    }
}