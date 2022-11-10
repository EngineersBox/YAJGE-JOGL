package com.engineersbox.yajgejogl.scene.gui;

import com.engineersbox.yajgejogl.scene.element.SceneElement;

public interface IHud {
    SceneElement[] getSceneElements();

    default void cleanup() {
        for (final SceneElement sceneElement : getSceneElements()) {
            sceneElement.getMesh().cleanUp();
        }
    }
}
