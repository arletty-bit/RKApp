package ru.rkapp;

public interface InterpolatableMethod {
    boolean interpolate(double t, double[] y);
    boolean supportsInterpolation();
}