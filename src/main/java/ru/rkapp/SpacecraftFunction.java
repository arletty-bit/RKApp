package ru.rkapp;

public enum SpacecraftFunction implements TestFunction {
    SPACECRAFT("Движение КА") {
        @Override
        public double value(double x) {
            return 0;
        }

        @Override
        public double derivative(double x) {
            return 0;
        }

        @Override
        public double numericalDerivative(double x) {
            return 0;
        }

        @Override
        public String getExpression() {
            return "Spacecraft Motion";
        }
    };
    
    private final String name;
    
    SpacecraftFunction(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return name;
    }
}