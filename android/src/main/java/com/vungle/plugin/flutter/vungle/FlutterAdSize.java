package com.vungle.plugin.flutter.vungle;

import androidx.annotation.NonNull;

import com.vungle.warren.AdConfig.AdSize;

class FlutterAdSize {
    @NonNull final AdSize size;
    final String name;
    final int width;
    final int height;

    FlutterAdSize(String name, int width, int height) {
        this.size = AdSize.fromName(name);
        this.name = name;
        this.width = width;
        this.height = height;
    }

    FlutterAdSize(@NonNull AdSize size) {
        this.size = size;
        this.name = size.getName();
        this.width = size.getWidth();
        this.height = size.getHeight();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof FlutterAdSize)) {
            return false;
        }

        final FlutterAdSize that = (FlutterAdSize) o;

        return size == that.size;
    }

    @Override
    public int hashCode() {
        return size.hashCode();
    }

    public AdSize getAdSize() {
        return size;
    }
}
