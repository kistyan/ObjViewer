package com.akn.objviewer;

public class Matrix3x3 {
    public static void from4x4(float[] to, float[] from) {
        for (int row = 0; row < 3; row++)
            System.arraycopy(from, row * 4, to, row * 3, 3);
    }

    public static float getDeterminant(float[] matrix) {
        return matrix[0] * (matrix[4] * matrix[8] - matrix[7] * matrix[5])
                - matrix[3] * (matrix[1] * matrix[8] - matrix[7] * matrix[2])
                + matrix[6] * (matrix[1] * matrix[5] - matrix[4] * matrix[2]);
    }

    public static void inverse(float[] matrix) {
        float determinant = getDeterminant(matrix);
        float cell00 = (matrix[4] * matrix[8] - matrix[5] * matrix[7]) / determinant,
                cell01 = -(matrix[1] * matrix[8] - matrix[2] * matrix[7]) / determinant,
                cell02 = (matrix[1] * matrix[5] - matrix[2] * matrix[4]) / determinant,
                cell10 = -(matrix[3] * matrix[8] - matrix[5] * matrix[6]) / determinant,
                cell11 = (matrix[0] * matrix[8] - matrix[2] * matrix[6]) / determinant,
                cell12 = -(matrix[0] * matrix[5] - matrix[2] * matrix[3]) / determinant,
                cell20 = (matrix[3] * matrix[7] - matrix[4] * matrix[6]) / determinant,
                cell21 = -(matrix[0] * matrix[7] - matrix[1] * matrix[6]) / determinant,
                cell22 = (matrix[0] * matrix[4] - matrix[1] * matrix[3]) / determinant;
        matrix[0] = cell00; matrix[1] = cell01; matrix[2] = cell02;
        matrix[3] = cell10; matrix[4] = cell11; matrix[5] = cell12;
        matrix[6] = cell20; matrix[7] = cell21; matrix[8] = cell22;
    }

    public static void transpose(float[] matrix) {
        for (int row = 0; row < 3; row++)
            for (int column = row + 1; column < 3; column++) {
                int firstIndex = row * 3 + column, secondIndex = column * 3 + row;
                float temp = matrix[firstIndex];
                matrix[firstIndex] = matrix[secondIndex];
                matrix[secondIndex] = temp;
            }
    }
}
