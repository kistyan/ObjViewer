package com.akn.objviewer;

import java.util.Arrays;

class FloatArrayKey {
    private final Float[][] array;

    public FloatArrayKey(Float[][] array) {
        this.array = array;
    }

    public Float[][] getArray() {
        return array;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof FloatArrayKey))
            return false;
        FloatArrayKey other = (FloatArrayKey) obj;
        return Arrays.deepEquals(this.array, other.array);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(array);
    }
}
