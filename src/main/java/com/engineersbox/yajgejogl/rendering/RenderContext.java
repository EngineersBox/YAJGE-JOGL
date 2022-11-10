package com.engineersbox.yajgejogl.rendering;

import com.engineersbox.yajgejogl.core.Window;
import com.engineersbox.yajgejogl.rendering.scene.shadow.ShadowCascade;
import com.engineersbox.yajgejogl.rendering.view.Camera;
import com.engineersbox.yajgejogl.rendering.view.Transform;
import com.engineersbox.yajgejogl.resources.assets.shader.Shader;
import com.engineersbox.yajgejogl.resources.assets.shader.ShadowMap;
import com.engineersbox.yajgejogl.resources.assets.shader.internal.preprocess.DepthShader;
import com.engineersbox.yajgejogl.scene.Scene;
import com.engineersbox.yajgejogl.scene.element.SceneElement;
import com.engineersbox.yajgejogl.scene.gui.IHud;

import java.util.List;

public record RenderContext(Window window,
                            Camera camera,
                            Scene scene,
                            List<SceneElement> filteredElements,
                            Transform transform,
                            ShadowMap shadowMap,
                            float specularPower,
                            DepthShader depthShader,
                            IHud hud) {
}
