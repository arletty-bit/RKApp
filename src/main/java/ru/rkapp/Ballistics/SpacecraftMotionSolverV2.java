package ru.rkapp.Ballistics;


import java.util.ArrayList;
import java.util.List;
import ru.rkapp.methods.Everhart;

/**
 * Полный аналог функциональности V2.c с использованием метода Эверхарта
 */
public class SpacecraftMotionSolverV2 {
    private final Everhart integrator;
    private final SpacecraftForcesCalculator calculator;
    
    public SpacecraftMotionSolverV2(int order) {
        this.calculator = new SpacecraftForcesCalculator();
        this.integrator = new Everhart(calculator, order, 6); // 6 уравнений
    }
    
    /**
     * Устанавливает баллистический коэффициент.
     */
    public void setBallisticCoefficient(double bc) {
        calculator.setBallisticCoefficient(bc);
    }
    
    /**
     * Устанавливает текущую дату для расчета звездного времени.
     */
    public void setCurrentDate(int day, int month, int year) {
        calculator.setCurrentDate(day, month, year);
    }
    
    /**
     * Выполняет прогноз движения в ГСК.
     */
public List<double[]> predictMotionInGSK(double[] initialStateGSK, double t0, double tEnd, double step) {
    List<double[]> results = new ArrayList<>();
    results.add(initialStateGSK.clone());
    
    double[] currentState = initialStateGSK.clone();
    double t = t0;
    
    while (t < tEnd) {
        double[] nextState = new double[6];
        double currentStep = Math.min(step, tEnd - t);
        
        if (!integrator.step(t, currentState, currentStep, nextState, null)) {
            throw new RuntimeException("Ошибка интегрирования на шаге t=" + t);
        }
        
        results.add(nextState);
        currentState = nextState;
        t += currentStep;
    }
    
    return results;
}
    
    /**
     * Выполняет прогноз движения в ИСК.
     */
    public List<double[]> predictMotionInISK(double[] initialStateISK, double t0, double tEnd, double step) {
        // Преобразуем начальные условия в ГСК
        double[] initialStateGSK = new double[6];
        calculator.convertISKtoGSK(t0, initialStateISK, initialStateGSK);
        
        // Интегрируем в ГСК
        List<double[]> trajectoryGSK = predictMotionInGSK(initialStateGSK, t0, tEnd, step);
        
        // Преобразуем результаты обратно в ИСК
        List<double[]> trajectoryISK = new ArrayList<>();
        for (double[] stateGSK : trajectoryGSK) {
            double[] stateISK = new double[6];
            calculator.convertGSKtoISK(t0 + (trajectoryISK.size() * step), stateGSK, stateISK);
            trajectoryISK.add(stateISK);
        }
        
        return trajectoryISK;
    }
    
//    /**
//     * Пример использования.
//     */
//    public static void main(String[] args) {
//        // Начальные условия в ГСК (пример)
//        double[] initialStateGSK = {
//            7000.0, 0.0, 0.0,  // Положение, км
//            0.0, 7.5, 0.0       // Скорость, км/с
//        };
//        
//        SpacecraftMotionSolverV2 solver = new SpacecraftMotionSolverV2(15); // Порядок метода 15
//        solver.setBallisticCoefficient(0.0413);
//        solver.setCurrentDate(1, 1, 2023); // Устанавливаем текущую дату
//        
//        // Прогноз на 1 час с шагом 10 секунд
//        List<double[]> trajectory = solver.predictMotionInGSK(initialStateGSK, 0.0, 3600.0, 10.0);
//        
//        // Вывод результатов
//        System.out.println("Время, X, Y, Z, Vx, Vy, Vz");
//        for (int i = 0; i < trajectory.size(); i++) {
//            double[] state = trajectory.get(i);
//            System.out.printf("%.1f, %.3f, %.3f, %.3f, %.6f, %.6f, %.6f%n",
//                i * 10.0, state[0], state[1], state[2], state[3], state[4], state[5]);
//        }
//    }
    
        public SpacecraftForcesCalculator getCalculator() {
        return calculator;
    }
}