package ru.rkapp.methods;

import org.apache.logging.log4j.LogManager;
import ru.rkapp.RungeKuttaMethod;
import ru.rkapp.RightCalculator;

public class AdaptiveDormandPrince853Integrator extends RungeKuttaMethod {
    
    private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(AdaptiveDormandPrince853Integrator.class);

    // Константы управления шагом
    private static final double SAFETY = 0.9;
    private static final double MIN_FACTOR = 0.2;
    private static final double MAX_FACTOR = 5.0;
    private static final double EXPONENT = 1.0 / 8.0; // Порядок метода = 8
    
    // Параметры адаптивности
    private final double minStep;
    private final double maxStep;
    private final double scalAbsoluteTolerance;
    private final double scalRelativeTolerance;
    private final double[] vecAbsoluteTolerance;
    private final double[] vecRelativeTolerance;
    
    // Состояние метода
    private double nextStep;
    private double[] lastDerivative;
    private boolean firstStep = true;
    
    // Количество стадий
    private static final int STAGES = 13;
    private static final double sqrt6 = Math.sqrt(6.0);
    
    // Коэффициенты c
    private static final double[] C = {
        (12.0 - 2.0 * sqrt6) / 135.0,
        (6.0 - sqrt6) / 45.0,
        (6.0 - sqrt6) / 30.0,
        (6.0 + sqrt6) / 30.0,
        1.0/3.0,
        1.0/4.0,
        4.0/13.0,
        127.0/195.0,
        3.0/5.0,
        6.0/7.0,
        1.0,
        1.0,
        1.0/10.0
    };
    
    // Основные веса (8-й порядок)
    private static final double[] B8 = {
        104257.0/1920240.0,
        0.0,
        0.0,
        0.0,
        0.0,
        3399327.0/763840.0,
        66578432.0/35198415.0,
        -1674902723.0/288716400.0,
        54980371265625.0/176692375811392.0,
        -734375.0/4826304.0,
        171414593.0/851261400.0,
        137909.0/3084480.0,
        0.0
    };
    
    // Веса для оценки ошибки (E1)
    private static final double[] E1 = new double[STAGES];
    static {
        E1[0] =  116092271.0 / 8848465920.0;
        E1[5] =  -1871647.0 / 1527680.0;
        E1[6] = -69799717.0 / 140793660.0;
        E1[7] =  1230164450203.0 / 739113984000.0;
        E1[8] = -1980813971228885.0 / 5654156025964544.0;
        E1[9] =   464500805.0 / 1389975552.0;
        E1[10] = 1606764981773.0 / 19613062656000.0;
        E1[11] =     -137909.0 / 6168960.0;
    }
    
    // Веса для оценки ошибки (E2)
    private static final double[] E2 = new double[STAGES];
    static {
        E2[0] =  -364463.0 / 1920240.0;
        E2[5] =  3399327.0 / 763840.0;
        E2[6] = 66578432.0 / 35198415.0;
        E2[7] = -1674902723.0 / 288716400.0;
        E2[8] = -74684743568175.0 / 176692375811392.0;
        E2[9] =    -734375.0 / 4826304.0;
        E2[10] = 171414593.0 / 851261400.0;
        E2[11] =     69869.0 / 3084480.0;
    }
    
    // Коэффициенты A (13 строк)
    private static final double[][] A = {
        {(12.0 - 2.0 * sqrt6) / 135.0},
        {(6.0 - sqrt6) / 180.0, (6.0 - sqrt6) / 60.0},
        {(6.0 - sqrt6) / 120.0, 0.0, (6.0 - sqrt6) / 40.0},
        {(462.0 + 107.0 * sqrt6) / 3000.0, 0.0, (-402.0 - 197.0 * sqrt6) / 1000.0, (168.0 + 73.0 * sqrt6) / 375.0},
        {1.0/27.0, 0.0, 0.0, (16.0 + sqrt6) / 108.0, (16.0 - sqrt6) / 108.0},
        {19.0/512.0, 0.0, 0.0, (118.0 + 23.0 * sqrt6) / 1024.0, (118.0 - 23.0 * sqrt6) / 1024.0, -9.0/512.0},
        {13772.0/371293.0, 0.0, 0.0, (51544.0 + 4784.0 * sqrt6) / 371293.0, (51544.0 - 4784.0 * sqrt6) / 371293.0, -5688.0/371293.0, 3072.0/371293.0},
        {58656157643.0/93983540625.0, 0.0, 0.0, (-1324889724104.0 - 318801444819.0 * sqrt6) / 626556937500.0, (-1324889724104.0 + 318801444819.0 * sqrt6) / 626556937500.0, 96044563816.0/3480871875.0, 5682451879168.0/281950621875.0, -165125654.0/3796875.0},
        {8909899.0/18653125.0, 0.0, 0.0, (-4521408.0 - 1137963.0 * sqrt6) / 2937500.0, (-4521408.0 + 1137963.0 * sqrt6) / 2937500.0, 96663078.0/4553125.0, 2107245056.0/137915625.0, -4913652016.0/147609375.0, -78894270.0/3880452869.0},
        {-20401265806.0/21769653311.0, 0.0, 0.0, (354216.0 + 94326.0 * sqrt6) / 112847.0, (354216.0 - 94326.0 * sqrt6) / 112847.0, -43306765128.0/5313852383.0, -20866708358144.0/1126708119789.0, 14886003438020.0/654632330667.0, 35290686222309375.0/14152473387134411.0, -1477884375.0/485066827.0},
        {39815761.0/17514443.0, 0.0, 0.0, (-3457480.0 - 960905.0 * sqrt6) / 551636.0, (-3457480.0 + 960905.0 * sqrt6) / 551636.0, -844554132.0/47026969.0, 8444996352.0/302158619.0, -2509602342.0/877790785.0, -28388795297996250.0/3199510091356783.0, 226716250.0/18341897.0, 1371316744.0/2131383595.0},
        {58656157643.0/93983540625.0, 0.0, 0.0, (-1324889724104.0 - 318801444819.0 * sqrt6) / 626556937500.0, (-1324889724104.0 + 318801444819.0 * sqrt6) / 626556937500.0, 96044563816.0/3480871875.0, 5682451879168.0/281950621875.0, -165125654.0/3796875.0, 8909899.0/18653125.0, -20401265806.0/21769653311.0, 39815761.0/17514443.0, 0.0},
        {14005451.0/335480064.0, 0.0, 0.0, 0.0, 0.0, -59238493.0/1068277825.0, 181606767.0/758867731.0, 561292985.0/797845732.0, -1041891430.0/1371343529.0, 760417239.0/1151165299.0, 118820643.0/751138087.0, -528747749.0/2220607170.0}
    };

    public AdaptiveDormandPrince853Integrator(RightCalculator calculator, 
                                             double minStep, double maxStep,
                                             double absTol, double relTol) {
        super(calculator);
        this.minStep = Math.abs(minStep);
        this.maxStep = Math.abs(maxStep);
        this.scalAbsoluteTolerance = absTol;
        this.scalRelativeTolerance = relTol;
        this.vecAbsoluteTolerance = null;
        this.vecRelativeTolerance = null;
        reset();
    }

    public AdaptiveDormandPrince853Integrator(RightCalculator calculator, 
                                             double minStep, double maxStep,
                                             double[] absTol, double[] relTol) {
        super(calculator);
        this.minStep = Math.abs(minStep);
        this.maxStep = Math.abs(maxStep);
        this.scalAbsoluteTolerance = 0;
        this.scalRelativeTolerance = 0;
        this.vecAbsoluteTolerance = absTol.clone();
        this.vecRelativeTolerance = relTol.clone();
        reset();
    }

    @Override
    public boolean step(double t, double[] y, double h, double[] yNew, Object parm) {
        final int n = y.length;
        double[][] k = new double[STAGES][n];
        double[] yTmp = new double[n];
        double[] y1 = new double[n];
        
        double hToUse = firstStep ? 
            Math.max(minStep, Math.min(maxStep, Math.abs(h))) * Math.signum(h) : 
            nextStep;

        boolean stepAccepted = false;
        double error = 0;
        
        while (!stepAccepted) {
            hToUse = applyStepBounds(hToUse);
            
            // Стадия 0 (FSAL)
            if (firstStep || lastDerivative == null) {
                if (!rightCalculator.compute(t, y, k[0], parm)) {
                    return false;
                }
            } else {
                System.arraycopy(lastDerivative, 0, k[0], 0, n);
            }
            
            // Промежуточные стадии (1-12)
            for (int i = 1; i < STAGES; i++) {
                System.arraycopy(y, 0, yTmp, 0, n);
                double[] aRow = A[i-1];
                for (int j = 0; j < aRow.length; j++) {
                    double aij = aRow[j];
                    if (aij != 0.0) {
                        for (int m = 0; m < n; m++) {
                            yTmp[m] += hToUse * aij * k[j][m];
                        }
                    }
                }
                double tStage = t + C[i-1] * hToUse;
                if (!rightCalculator.compute(tStage, yTmp, k[i], parm)) {
                    return false;
                }
            }
            
            // Вычисление нового состояния
            System.arraycopy(y, 0, y1, 0, n);
            for (int j = 0; j < STAGES; j++) {
                double bj = B8[j];
                if (bj != 0.0) {
                    for (int m = 0; m < n; m++) {
                        y1[m] += hToUse * bj * k[j][m];
                    }
                }
            }
            
            // Оценка ошибки по схеме Hipparchus
            error = estimateError(k, y, y1, hToUse, n);
            
            // Проверка допустимости ошибки
            if (error <= 1.0) {
                stepAccepted = true;
            } else {
                double factor = Math.max(MIN_FACTOR, SAFETY * Math.pow(error, -EXPONENT));
                hToUse *= factor;
                if (Math.abs(hToUse) < minStep) {
                    stepAccepted = true;
                }
            }
        }
        
        // Сохранение результатов
        System.arraycopy(y1, 0, yNew, 0, n);
        lastDerivative = k[STAGES-1]; // FSAL для следующего шага
        
        // Расчет следующего шага
        double factor = SAFETY * Math.pow(error, -EXPONENT);
        factor = Math.min(MAX_FACTOR, Math.max(MIN_FACTOR, factor));
        nextStep = hToUse * factor;
        nextStep = applyStepBounds(nextStep);
        
        firstStep = false;
        return true;
    }
    
    private double applyStepBounds(double step) {
        double absStep = Math.abs(step);
        if (absStep < minStep) {
            return minStep * Math.signum(step);
        } else if (absStep > maxStep) {
            return maxStep * Math.signum(step);
        }
        return step;
    }
    
    private double estimateError(double[][] k, double[] y0, double[] y1, double h, int n) {
        double error1 = 0.0;
        double error2 = 0.0;
        
        for (int i = 0; i < n; i++) {
            double errSum1 = 0.0;
            double errSum2 = 0.0;
            
            for (int j = 0; j < STAGES; j++) {
                errSum1 += E1[j] * k[j][i];
                errSum2 += E2[j] * k[j][i];
            }
            
            double tol;
            if (vecAbsoluteTolerance != null) {
                tol = vecAbsoluteTolerance[i] + vecRelativeTolerance[i] * Math.max(Math.abs(y0[i]), Math.abs(y1[i]));
            } else {
                tol = scalAbsoluteTolerance + scalRelativeTolerance * Math.max(Math.abs(y0[i]), Math.abs(y1[i]));
            }
            
            double ratio1 = errSum1 * h / tol;
            error1 += ratio1 * ratio1;
            
            double ratio2 = errSum2 * h / tol;
            error2 += ratio2 * ratio2;
        }
        
        error1 = Math.sqrt(error1 / n);
        error2 = Math.sqrt(error2 / n);
        double den = error1 + 0.01 * error2;
        
        if (den <= 0.0) {
            den = 1.0;
        }
        
        return Math.abs(h) * error1 / Math.sqrt(n * den);
    }
    
    public void reset() {
        firstStep = true;
        lastDerivative = null;
        nextStep = Double.NaN;
    }
}

////package ru.rkapp.methods;
////
////import java.util.ArrayList;
////import java.util.Arrays;
////import java.util.List;
////import ru.rkapp.AdaptiveODEIntegrator;
////import ru.rkapp.RightCalculator;
////
////public class AdaptiveDormandPrince853Integrator implements AdaptiveODEIntegrator {
////    private final double minStep;
////    private final double maxStep;
////    private final double scalAbsoluteTolerance;
////    private final double scalRelativeTolerance;
////    private final double[] vecAbsoluteTolerance;
////    private final double[] vecRelativeTolerance;
////    private static final double sqrt6 = Math.sqrt(6.0);
////    private static final int N_STAGES = 12;
////    private boolean firstStep = true;
////    private double[] lastDeriv;
////
////    // Коэффициенты c_i
////    private static final double[] C = {
////        (12.0 - 2.0 * sqrt6) / 135.0,
////        (6.0 - sqrt6) / 45.0,
////        (6.0 - sqrt6) / 30.0,
////        (6.0 + sqrt6) / 30.0,
////        1.0/3.0,
////        1.0/4.0,
////        4.0/13.0,
////        127.0/195.0,
////        3.0/5.0,
////        6.0/7.0,
////        1.0,
////        1.0
////    };
////
////    // Нижняя треугольная матрица A
////    private static final double[][] A = {
////        {},
////        {(6.0 - sqrt6) / 180.0},
////        {(6.0 - sqrt6) / 120.0, (6.0 - sqrt6) / 40.0},
////        {(462.0 + 107.0 * sqrt6) / 3000.0, 0.0, (-402.0 - 197.0 * sqrt6) / 1000.0},
////        {1.0/27.0, 0.0, 0.0, (16.0 + sqrt6) / 108.0},
////        {19.0/512.0, 0.0, 0.0, (118.0 + 23.0 * sqrt6) / 1024.0, -9.0/512.0},
////        {13772.0/371293.0, 0.0, 0.0, (51544.0 + 4784.0 * sqrt6) / 371293.0, -5688.0/371293.0, 3072.0/371293.0},
////        {58656157643.0/93983540625.0, 0.0, 0.0, (-1324889724104.0 - 318801444819.0 * sqrt6) / 626556937500.0, 
////         96044563816.0/3480871875.0, 5682451879168.0/281950621875.0, -165125654.0/3796875.0},
////        {8909899.0/18653125.0, 0.0, 0.0, (-4521408.0 - 1137963.0 * sqrt6) / 2937500.0, 
////         96663078.0/4553125.0, 2107245056.0/137915625.0, -4913652016.0/147609375.0, -78894270.0/3880452869.0},
////        {-20401265806.0/21769653311.0, 0.0, 0.0, (354216.0 + 94326.0 * sqrt6) / 112847.0,
////         -43306765128.0/5313852383.0, -20866708358144.0/1126708119789.0, 14886003438020.0/654632330667.0,
////         35290686222309375.0/14152473387134411.0, -1477884375.0/485066827.0},
////        {39815761.0/17514443.0, 0.0, 0.0, (-3457480.0 - 960905.0 * sqrt6) / 551636.0,
////         -844554132.0/47026969.0, 8444996352.0/302158619.0, -2509602342.0/877790785.0,
////         -28388795297996250.0/3199510091356783.0, 226716250.0/18341897.0, 1371316744.0/2131383595.0},
////        {104257.0/1920240.0, 0.0, 0.0, 0.0, 0.0, 3399327.0/763840.0, 
////         66578432.0/35198415.0, -1674902723.0/288716400.0, 54980371265625.0/176692375811392.0, 
////         -734375.0/4826304.0, 171414593.0/851261400.0}
////    };
////
////    // Веса для основного порядка (8)
////    private static final double[] B8 = {
////        104257.0/1920240.0,
////        0.0,
////        0.0,
////        0.0,
////        0.0,
////        3399327.0/763840.0,
////        66578432.0/35198415.0,
////        -1674902723.0/288716400.0,
////        54980371265625.0/176692375811392.0,
////        -734375.0/4826304.0,
////        171414593.0/851261400.0,
////        137909.0/3084480.0
////    };
////
////    // Веса для оценки ошибки (E1)
////    private static final double[] E1 = new double[12];
////    static {
////        Arrays.fill(E1, 0.0);
////        E1[0] =  116092271.0 / 8848465920.0;
////        E1[5] =  -1871647.0 / 1527680.0;
////        E1[6] = -69799717.0 / 140793660.0;
////        E1[7] =  1230164450203.0 / 739113984000.0;
////        E1[8] = -1980813971228885.0 / 5654156025964544.0;
////        E1[9] =   464500805.0 / 1389975552.0;
////        E1[10] = 1606764981773.0 / 19613062656000.0;
////        E1[11] =     -137909.0 / 6168960.0;
////    }
////
////    // Веса для оценки ошибки (E2)
////    private static final double[] E2 = new double[12];
////    static {
////        Arrays.fill(E2, 0.0);
////        E2[0] =  -364463.0 / 1920240.0;
////        E2[5] =  3399327.0 / 763840.0;
////        E2[6] = 66578432.0 / 35198415.0;
////        E2[7] = -1674902723.0 / 288716400.0;
////        E2[8] = -74684743568175.0 / 176692375811392.0;
////        E2[9] =    -734375.0 / 4826304.0;
////        E2[10] = 171414593.0 / 851261400.0;
////        E2[11] =     69869.0 / 3084480.0;
////    }
////
////    public AdaptiveDormandPrince853Integrator(double minStep, double maxStep,
////                                             double scalAbsoluteTolerance,
////                                             double scalRelativeTolerance) {
////        this.minStep = Math.abs(minStep);
////        this.maxStep = Math.abs(maxStep);
////        this.scalAbsoluteTolerance = scalAbsoluteTolerance;
////        this.scalRelativeTolerance = scalRelativeTolerance;
////        this.vecAbsoluteTolerance = null;
////        this.vecRelativeTolerance = null;
////    }
////
////    public AdaptiveDormandPrince853Integrator(double minStep, double maxStep,
////                                             double[] vecAbsoluteTolerance,
////                                             double[] vecRelativeTolerance) {
////        this.minStep = Math.abs(minStep);
////        this.maxStep = Math.abs(maxStep);
////        this.scalAbsoluteTolerance = 0;
////        this.scalRelativeTolerance = 0;
////        this.vecAbsoluteTolerance = vecAbsoluteTolerance.clone();
////        this.vecRelativeTolerance = vecRelativeTolerance.clone();
////    }
////
////    public List<double[]> integrate(RightCalculator calculator, Object parm,
////                                   double t0, double[] y0,
////                                   double tEnd) {
////        
////        List<double[]> solution = new ArrayList<>();
////        solution.add(y0.clone()); // Добавляем начальную точку
////        
////        double h = Math.copySign(maxStep, tEnd - t0);
////        double t = t0;
////        double[] y = y0.clone();
////        double[] yTmp = new double[y.length];
////        lastDeriv = null;
////        firstStep = true;
////
////         while (!Double.isNaN(h) && Math.abs(t - tEnd) > 1e-12) {
////            if (Math.abs(h) < minStep) h = Math.copySign(minStep, h);
////            if (t + h >= tEnd) h = tEnd - t;
////
////            double[] yDotK = new double[N_STAGES * y.length];
////            double error = step(calculator, parm, t, y, h, yTmp, yDotK);
////
////            if (Double.isNaN(error)) {
////                throw new RuntimeException("Ошибка вычислений");
////            }
////
////            double tol = computeTolerance(y, yTmp);
////            if (error <= tol) {
////                double[] newState = yTmp.clone();
////                solution.add(newState); // Добавляем новую точку решения
////                System.arraycopy(newState, 0, y, 0, y.length);
////                t += h;
////                lastDeriv = Arrays.copyOfRange(yDotK, 11 * y.length, 12 * y.length);
////                firstStep = false;
////            }
////
////            double factor = Math.min(5.0, Math.max(0.2, 0.9 * Math.pow(error / tol, -1.0 / 8.0)));
////            h = Math.max(minStep, Math.min(maxStep, Math.abs(h * factor))) * Math.signum(h);
////        }
////        
////        return solution;
////    
////    }
////
////    private double step(RightCalculator calculator, Object parm,
////                       double t, double[] y, double h,
////                       double[] yNew, double[] yDotK) {
////        final int n = y.length;
////        final int stageStride = n;
////        
////        // Стадия 0 (FSAL)
////        if (firstStep) {
////            double[] yDot0 = new double[n];
////            if (!calculator.compute(t, y, yDot0, parm)) {
////                return Double.NaN; // Флаг ошибки
////            }
////            System.arraycopy(yDot0, 0, yDotK, 0, n);
////        } else {
////            System.arraycopy(lastDeriv, 0, yDotK, 0, n);
////        }
////
////        // Стадии 1-11
////        double[] yTmp = new double[n];
////        for (int i = 1; i < N_STAGES; i++) {
////            System.arraycopy(y, 0, yTmp, 0, n);
////            for (int j = 0; j < i; j++) {
////                double aij = A[i][j];
////                if (aij != 0.0) {
////                    int offset = j * stageStride;
////                    for (int k = 0; k < n; k++) {
////                        yTmp[k] += h * aij * yDotK[offset + k];
////                    }
////                }
////            }
////            
////            int offset = i * stageStride;
////            double[] yDotTemp = new double[n];
////            if (!calculator.compute(t + C[i] * h, yTmp, yDotTemp, parm)) {
////                return Double.NaN; // Флаг ошибки
////            }
////            System.arraycopy(yDotTemp, 0, yDotK, offset, n);
////        }
////
////        // Вычисление нового состояния
////        System.arraycopy(y, 0, yNew, 0, n);
////        for (int j = 0; j < N_STAGES; j++) {
////            double bj = B8[j];
////            if (bj != 0.0) {
////                int offset = j * stageStride;
////                for (int k = 0; k < n; k++) {
////                    yNew[k] += h * bj * yDotK[offset + k];
////                }
////            }
////        }
////
////        // Оценка ошибки
////        double error1 = 0.0;
////        double error2 = 0.0;
////        for (int k = 0; k < n; k++) {
////            double errSum1 = 0.0;
////            double errSum2 = 0.0;
////            for (int j = 0; j < N_STAGES; j++) {
////                double yDot = yDotK[j * stageStride + k];
////                errSum1 += E1[j] * yDot;
////                errSum2 += E2[j] * yDot;
////            }
////            double tol = getToleranceComponent(k, y[k], yNew[k]);
////            double ratio1 = errSum1 / tol;
////            double ratio2 = errSum2 / tol;
////            error1 += ratio1 * ratio1;
////            error2 += ratio2 * ratio2;
////        }
////
////        double den = error1 + 0.01 * error2;
////        return Math.abs(h) * error1 / Math.sqrt(n * Math.max(1e-15, den));
////    }
////
////    private double computeTolerance(double[] y, double[] yNew) {
////        double error = 0.0;
////        for (int i = 0; i < y.length; i++) {
////            double tol = getToleranceComponent(i, y[i], yNew[i]);
////            error += 1.0 / (tol * tol);
////        }
////        return Math.sqrt(error / y.length);
////    }
////
////    private double getToleranceComponent(int index, double yk, double yk1) {
////        double atol = (vecAbsoluteTolerance != null) ? 
////            vecAbsoluteTolerance[index] : scalAbsoluteTolerance;
////        double rtol = (vecRelativeTolerance != null) ? 
////            vecRelativeTolerance[index] : scalRelativeTolerance;
////        return atol + rtol * Math.max(Math.abs(yk), Math.abs(yk1));
////    }
////}
////
////
///////*
////// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
////// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
////// */
//////package ru.rkapp.methods;
//////
//////import java.util.Arrays;
//////
//////public class AdaptiveDormandPrince853Integrator {
//////    private final double minStep;
//////    private final double maxStep;
//////    private final double scalAbsoluteTolerance;
//////    private final double scalRelativeTolerance;
//////    private final double[] vecAbsoluteTolerance;
//////    private final double[] vecRelativeTolerance;
//////    private static final double sqrt6 = Math.sqrt(6.0);
//////    private static final int N_STAGES = 12;
//////    private boolean firstStep = true;
//////    private double[] lastDeriv;
//////
//////    // Коэффициенты c_i
//////    private static final double[] C = {
//////        (12.0 - 2.0 * sqrt6) / 135.0,
//////        (6.0 - sqrt6) / 45.0,
//////        (6.0 - sqrt6) / 30.0,
//////        (6.0 + sqrt6) / 30.0,
//////        1.0/3.0,
//////        1.0/4.0,
//////        4.0/13.0,
//////        127.0/195.0,
//////        3.0/5.0,
//////        6.0/7.0,
//////        1.0,
//////        1.0
//////    };
//////
//////    // Нижняя треугольная матрица A
//////    private static final double[][] A = {
//////        {},
//////        {(6.0 - sqrt6) / 180.0},
//////        {(6.0 - sqrt6) / 120.0, (6.0 - sqrt6) / 40.0},
//////        {(462.0 + 107.0 * sqrt6) / 3000.0, 0.0, (-402.0 - 197.0 * sqrt6) / 1000.0},
//////        {1.0/27.0, 0.0, 0.0, (16.0 + sqrt6) / 108.0},
//////        {19.0/512.0, 0.0, 0.0, (118.0 + 23.0 * sqrt6) / 1024.0, -9.0/512.0},
//////        {13772.0/371293.0, 0.0, 0.0, (51544.0 + 4784.0 * sqrt6) / 371293.0, -5688.0/371293.0, 3072.0/371293.0},
//////        {58656157643.0/93983540625.0, 0.0, 0.0, (-1324889724104.0 - 318801444819.0 * sqrt6) / 626556937500.0, 
//////         96044563816.0/3480871875.0, 5682451879168.0/281950621875.0, -165125654.0/3796875.0},
//////        {8909899.0/18653125.0, 0.0, 0.0, (-4521408.0 - 1137963.0 * sqrt6) / 2937500.0, 
//////         96663078.0/4553125.0, 2107245056.0/137915625.0, -4913652016.0/147609375.0, -78894270.0/3880452869.0},
//////        {-20401265806.0/21769653311.0, 0.0, 0.0, (354216.0 + 94326.0 * sqrt6) / 112847.0,
//////         -43306765128.0/5313852383.0, -20866708358144.0/1126708119789.0, 14886003438020.0/654632330667.0,
//////         35290686222309375.0/14152473387134411.0, -1477884375.0/485066827.0},
//////        {39815761.0/17514443.0, 0.0, 0.0, (-3457480.0 - 960905.0 * sqrt6) / 551636.0,
//////         -844554132.0/47026969.0, 8444996352.0/302158619.0, -2509602342.0/877790785.0,
//////         -28388795297996250.0/3199510091356783.0, 226716250.0/18341897.0, 1371316744.0/2131383595.0},
//////        {104257.0/1920240.0, 0.0, 0.0, 0.0, 0.0, 3399327.0/763840.0, 
//////         66578432.0/35198415.0, -1674902723.0/288716400.0, 54980371265625.0/176692375811392.0, 
//////         -734375.0/4826304.0, 171414593.0/851261400.0}
//////    };
//////
//////    // Веса для основного порядка (8)
//////    private static final double[] B8 = {
//////        104257.0/1920240.0,
//////        0.0,
//////        0.0,
//////        0.0,
//////        0.0,
//////        3399327.0/763840.0,
//////        66578432.0/35198415.0,
//////        -1674902723.0/288716400.0,
//////        54980371265625.0/176692375811392.0,
//////        -734375.0/4826304.0,
//////        171414593.0/851261400.0,
//////        137909.0/3084480.0
//////    };
//////
//////    // Веса для оценки ошибки (E1)
//////    private static final double[] E1 = new double[12];
//////    static {
//////        Arrays.fill(E1, 0.0);
//////        E1[0] =  116092271.0 / 8848465920.0;
//////        E1[5] =  -1871647.0 / 1527680.0;
//////        E1[6] = -69799717.0 / 140793660.0;
//////        E1[7] =  1230164450203.0 / 739113984000.0;
//////        E1[8] = -1980813971228885.0 / 5654156025964544.0;
//////        E1[9] =   464500805.0 / 1389975552.0;
//////        E1[10] = 1606764981773.0 / 19613062656000.0;
//////        E1[11] =     -137909.0 / 6168960.0;
//////    }
//////
//////    // Веса для оценки ошибки (E2)
//////    private static final double[] E2 = new double[12];
//////    static {
//////        Arrays.fill(E2, 0.0);
//////        E2[0] =  -364463.0 / 1920240.0;
//////        E2[5] =  3399327.0 / 763840.0;
//////        E2[6] = 66578432.0 / 35198415.0;
//////        E2[7] = -1674902723.0 / 288716400.0;
//////        E2[8] = -74684743568175.0 / 176692375811392.0;
//////        E2[9] =    -734375.0 / 4826304.0;
//////        E2[10] = 171414593.0 / 851261400.0;
//////        E2[11] =     69869.0 / 3084480.0;
//////    }
//////
//////    public AdaptiveDormandPrince853Integrator(double minStep, double maxStep,
//////                                             double scalAbsoluteTolerance,
//////                                             double scalRelativeTolerance) {
//////        this.minStep = Math.abs(minStep);
//////        this.maxStep = Math.abs(maxStep);
//////        this.scalAbsoluteTolerance = scalAbsoluteTolerance;
//////        this.scalRelativeTolerance = scalRelativeTolerance;
//////        this.vecAbsoluteTolerance = null;
//////        this.vecRelativeTolerance = null;
//////    }
//////
//////    public AdaptiveDormandPrince853Integrator(double minStep, double maxStep,
//////                                             double[] vecAbsoluteTolerance,
//////                                             double[] vecRelativeTolerance) {
//////        this.minStep = Math.abs(minStep);
//////        this.maxStep = Math.abs(maxStep);
//////        this.scalAbsoluteTolerance = 0;
//////        this.scalRelativeTolerance = 0;
//////        this.vecAbsoluteTolerance = vecAbsoluteTolerance.clone();
//////        this.vecRelativeTolerance = vecRelativeTolerance.clone();
//////    }
//////
//////    public void integrate(FirstOrderDifferentialEquations equations,
//////                         double t0, double[] y0,
//////                         double tEnd, double[] y) {
//////        double h = Math.copySign(maxStep, tEnd - t0);
//////        double t = t0;
//////        System.arraycopy(y0, 0, y, 0, y0.length);
//////        lastDeriv = null;
//////        firstStep = true;
//////
//////        while (!Double.isNaN(h) && Math.abs(t - tEnd) > 1e-12) {
//////            if (Math.abs(h) < minStep) {
//////                h = Math.copySign(minStep, h);
//////            }
//////            if (Math.abs(t + h - tEnd) < 1e-12) {
//////                h = tEnd - t;
//////            } else if (Math.abs(t + h) > Math.abs(tEnd)) {
//////                h = tEnd - t;
//////            }
//////
//////            double[] yTmp = new double[y.length];
//////            double[] yDotK = new double[N_STAGES * y.length];
//////            double error = step(equations, t, y, h, yTmp, yDotK);
//////
//////            double tol = computeTolerance(y, yTmp);
//////            if (error <= tol) {
//////                System.arraycopy(yTmp, 0, y, 0, y.length);
//////                t += h;
//////                lastDeriv = Arrays.copyOfRange(yDotK, 11 * y.length, 12 * y.length);
//////                firstStep = false;
//////            }
//////
//////            double factor = 0.9 * Math.pow(error / tol, -1.0 / 8.0);
//////            factor = Math.min(5.0, Math.max(0.2, factor));
//////            h *= factor;
//////            h = Math.max(minStep, Math.min(maxStep, Math.abs(h))) * Math.signum(h);
//////        }
//////    }
//////
//////    private double step(FirstOrderDifferentialEquations equations,
//////                       double t, double[] y, double h,
//////                       double[] yNew, double[] yDotK) {
//////        final int n = y.length;
//////        final int stageStride = n;
//////        
//////        // Стадия 0 (FSAL)
//////        if (firstStep) {
//////            double[] yDot0 = new double[n];
//////            equations.computeDerivatives(t, y, yDot0);
//////            System.arraycopy(yDot0, 0, yDotK, 0, n);
//////        } else {
//////            System.arraycopy(lastDeriv, 0, yDotK, 0, n);
//////        }
//////
//////        // Стадии 1-11
//////        double[] yTmp = new double[n];
//////        for (int i = 1; i < N_STAGES; i++) {
//////            System.arraycopy(y, 0, yTmp, 0, n);
//////            for (int j = 0; j < i; j++) {
//////                double aij = A[i][j];
//////                if (aij != 0.0) {
//////                    int offset = j * stageStride;
//////                    for (int k = 0; k < n; k++) {
//////                        yTmp[k] += h * aij * yDotK[offset + k];
//////                    }
//////                }
//////            }
//////            int offset = i * stageStride;
//////            equations.computeDerivatives(t + C[i] * h, yTmp, 0, yDotK, offset);
//////        }
//////
//////        // Вычисление нового состояния
//////        System.arraycopy(y, 0, yNew, 0, n);
//////        for (int j = 0; j < N_STAGES; j++) {
//////            double bj = B8[j];
//////            if (bj != 0.0) {
//////                int offset = j * stageStride;
//////                for (int k = 0; k < n; k++) {
//////                    yNew[k] += h * bj * yDotK[offset + k];
//////                }
//////            }
//////        }
//////
//////        // Оценка ошибки
//////        double error1 = 0.0;
//////        double error2 = 0.0;
//////        for (int k = 0; k < n; k++) {
//////            double errSum1 = 0.0;
//////            double errSum2 = 0.0;
//////            for (int j = 0; j < N_STAGES; j++) {
//////                double yDot = yDotK[j * stageStride + k];
//////                errSum1 += E1[j] * yDot;
//////                errSum2 += E2[j] * yDot;
//////            }
//////            double tol = getToleranceComponent(k, y[k], yNew[k]);
//////            double ratio1 = errSum1 / tol;
//////            double ratio2 = errSum2 / tol;
//////            error1 += ratio1 * ratio1;
//////            error2 += ratio2 * ratio2;
//////        }
//////
//////        double den = error1 + 0.01 * error2;
//////        return Math.abs(h) * error1 / Math.sqrt(n * Math.max(1e-15, den));
//////    }
//////
//////    private double computeTolerance(double[] y, double[] yNew) {
//////        double error = 0.0;
//////        for (int i = 0; i < y.length; i++) {
//////            double tol = getToleranceComponent(i, y[i], yNew[i]);
//////            error += 1.0 / (tol * tol);
//////        }
//////        return Math.sqrt(error / y.length);
//////    }
//////
//////    private double getToleranceComponent(int index, double yk, double yk1) {
//////        double atol = (vecAbsoluteTolerance != null) ? 
//////            vecAbsoluteTolerance[index] : scalAbsoluteTolerance;
//////        double rtol = (vecRelativeTolerance != null) ? 
//////            vecRelativeTolerance[index] : scalRelativeTolerance;
//////        return atol + rtol * Math.max(Math.abs(yk), Math.abs(yk1));
//////    }
//////
//////    public interface FirstOrderDifferentialEquations {
//////        void computeDerivatives(double t, double[] y, double[] yDot);
//////    }
//////}