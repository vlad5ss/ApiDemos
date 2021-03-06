/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.example.android.apis.graphics.kube;

/**
 * A 4x4 float matrix which is used to move {@code GLVertex} x,y,z locations
 */
@SuppressWarnings("WeakerAccess")
public class M4 {
    /**
     * Our 4x4 float matrix which are methods operate on.
     */
    public float[][] m = new float[4][4];

    /**
     * Our basic constructor, which does nothing, but gives us an all zero field {@code float[][] m}
     * to use.
     */
    public M4() {
    }

    /**
     * Constructor which clones another instance of {@code M4} using deep copy.
     *
     * @param other {@code M4} object we are to deep copy.
     */
    public M4(M4 other) {
        for (int i = 0; i < 4; i++) {
            //noinspection ManualArrayCopy
            for (int j = 0; j < 4; j++) {
                m[i][j] = other.m[i][j];
            }
        }
    }

    /**
     * Multiplies a {@code GLVertex src} by our field {@code float[][] m} and places the results in
     * {@code GLVertex dest}. Simple multiplication of a vector by a matrix. The {@code m[3]} array
     * appears to be superfluous since only {@code m[3][3]} is non-zero (set to 1.0 by the method
     * {@code setIdentity} and it is never used oddly enough.
     *
     * @param src Source {@code GLVertex} to multiply by our field {@code float[][] m}.
     * @param dest Destination {@code GLVertex} to place results in.
     */
    public void multiply(GLVertex src, GLVertex dest) {
        dest.x = src.x * m[0][0] + src.y * m[1][0] + src.z * m[2][0] + m[3][0];
        dest.y = src.x * m[0][1] + src.y * m[1][1] + src.z * m[2][1] + m[3][1];
        dest.z = src.x * m[0][2] + src.y * m[1][2] + src.z * m[2][2] + m[3][2];
    }

    /**
     * Simple 4x4 matrix multiplication, the 4x4 float matrix in the field {@code other.m} is
     * multiplied by our own field {@code float[][] m} and the result is returned to the caller.
     *
     * @param other {@code M4} matrix to multiply by our own matrix
     * @return the result of multiplying {@code M4 other} by our own matrix.
     */
    public M4 multiply(M4 other) {
        M4 result = new M4();
        float[][] m1 = m;
        float[][] m2 = other.m;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result.m[i][j] = m1[i][0] * m2[0][j] + m1[i][1] * m2[1][j] + m1[i][2] * m2[2][j] + m1[i][3] * m2[3][j];
            }
        }

        return result;
    }

    /**
     * Sets the contents of our field {@code float[][] m} to the identity matrix.
     */
    public void setIdentity() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                m[i][j] = (i == j ? 1f : 0f);
            }
        }
    }

    /**
     * Turns our field {@code float[][] m} into a string for debugging purposes.
     *
     * @return printable string version of our 4x4 float matrix
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("[ ");
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                builder.append(m[i][j]);
                builder.append(" ");
            }
            if (i < 2)
                builder.append("\n  ");
        }
        builder.append(" ]");
        return builder.toString();
    }
}
