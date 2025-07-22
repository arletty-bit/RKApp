/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.rkapp;

/**
 *
 * @author arletty
 */
import java.util.List;

public interface AdaptiveODEIntegrator {
    List<double[]> integrate(RightCalculator calculator, Object parm, 
                             double t0, double[] y0, double tEnd);
}
