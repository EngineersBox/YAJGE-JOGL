package com.engineersbox.yajgejogl.scene.element.object.composite.virtualisation;

import java.util.List;
import java.util.Set;

public record LODLevel(int index,
                       int groupIndex,
                       List<VertexCluster> clusters,
                       List<LODLevel> parents) {

}
