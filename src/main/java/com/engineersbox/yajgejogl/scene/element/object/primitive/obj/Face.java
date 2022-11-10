package com.engineersbox.yajgejogl.scene.element.object.primitive.obj;

public class Face {

    private final IdxGroup[] idxGroups;

    public Face(final String[] v) {
        this.idxGroups = new IdxGroup[v.length];
        for (int i = 0; i < v.length; i++) {
            this.idxGroups[i] = Face.parseLine(v[i]);
        }
    }

    private static IdxGroup parseLine(final String line) {
        final IdxGroup idxGroup = new IdxGroup();
        final String[] lineTokens = line.split("/");
        final int length = lineTokens.length;
        idxGroup.idxPos = Integer.parseInt(lineTokens[0]) - 1;
        if (length > 1) {
            final String textCoord = lineTokens[1];
            idxGroup.idxTextCoord = !textCoord.isEmpty() ? Integer.parseInt(textCoord) - 1 : IdxGroup.NO_VALUE;
            if (length > 2) {
                idxGroup.idxVecNormal = Integer.parseInt(lineTokens[2]) - 1;
            }
        }

        return idxGroup;
    }

    public IdxGroup[] getFaceVertexIndices() {
        return this.idxGroups;
    }
}
