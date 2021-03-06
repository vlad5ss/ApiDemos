/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.apis.graphics.spritetext;

import javax.microedition.khronos.opengles.GL10;

/**
 * Class which fetches and saves the current model view and projection view matrices in its fields.
 */
@SuppressWarnings("WeakerAccess")
class MatrixGrabber {

    /**
     * Our copy of the model view matrix
     */
    public float[] mModelView;
    /**
     * Our copy of the projection view matrix
     */
    public float[] mProjection;

    /**
     * Our constructor, we simply allocate storage for our fields {@code float[] mModelView} and
     * {@code float[] mProjection}.
     */
    public MatrixGrabber() {
        mModelView = new float[16];
        mProjection = new float[16];
    }

    /**
     * Record the current modelView and projection matrix state. Has the side effect of setting the
     * current matrix state to GL_MODELVIEW -- UNUSED
     *
     * @param gl the gl interface
     */
    @SuppressWarnings("unused")
    public void getCurrentState(GL10 gl) {
        getCurrentProjection(gl);
        getCurrentModelView(gl);
    }

    /**
     * Record the current modelView matrix state. Has the side effect of setting the current matrix
     * state to GL_MODELVIEW. We simply call our method {@code getMatrix} to read the current model
     * view matrix into our field {@code float[] mModelView}
     *
     * @param gl the gl interface
     */
    public void getCurrentModelView(GL10 gl) {
        getMatrix(gl, GL10.GL_MODELVIEW, mModelView);
    }

    /**
     * Record the current projection matrix state. Has the side effect of setting the current matrix
     * state to GL_PROJECTION. We simply call our method {@code getMatrix} to read the current
     * projection matrix into our field {@code float[] mProjection}.
     *
     * @param gl the gl interface
     */
    public void getCurrentProjection(GL10 gl) {
        getMatrix(gl, GL10.GL_PROJECTION, mProjection);
    }

    /**
     * Sets the current matrix to its parameter {@code int mode}, and reads that matrix into its
     * parameter {@code float[] mat}. To do this we cast our parameter {@code GL10 gl} to
     * {@code MatrixTrackingGL gl2}, use it to set the current matrix to {@code int mode}, and
     * then use the {@code gl2} method {@code getMatrix} to copy that matrix into our parameter
     * {@code mat}.
     *
     * @param gl   the gl interface
     * @param mode matrix we are interested in, either GL_MODELVIEW, or GL_PROJECTION
     * @param mat  {@code float[]} array to hold the matrix requested
     */
    private void getMatrix(GL10 gl, int mode, float[] mat) {
        MatrixTrackingGL gl2 = (MatrixTrackingGL) gl;
        gl2.glMatrixMode(mode);
        gl2.getMatrix(mat, 0);
    }
}
