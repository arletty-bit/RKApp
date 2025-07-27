package ru.rkapp.Ballistics;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.rkapp.RightCalculator;

/**
 * Полная реализация моделей возмущающих сил из V2.c
 * Включает:
 * - Гравитационное поле с гармониками
 * - Атмосферную модель
 * - Переносные ускорения от вращения Земли
 * - Преобразования между ГСК и ИСК
 */
public class SpacecraftForcesCalculator implements RightCalculator {
    private static final Logger LOG = LogManager.getLogger(SpacecraftForcesCalculator.class);
    
    // Константы из V2.c
    private static final double Re = 6378.136; // Радиус Земли, км
    private static final double mu = 398600.44; // Гравитационный параметр Земли, км^3/с^2
    private static final double omEarth = 7.292115E-5; // Угловая скорость вращения Земли, рад/с
    private static final double flat = 1.0/298.257; // Сжатие Земли
    
        // Эпоха J2000 в модифицированных юлианских днях (MJD)
    private static final double MJD_J2000 = 51544.5;
    
        // Константы для преобразования даты
    private static final int[][] MONTH_DAYS = {
        {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31}, // Не високосный
        {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31}  // Високосный
    };
    
     // Время в секундах от эпохи J2000
    private double secondsSinceJ2000 = 0.0;
    
    // Гармоники гравитационного поля (C и S коэффициенты)
    private static final double[][] C = {
        {-1.082627e-03, -2.414000e-10, 1.574572e-06},
        {2.532509e-06, 2.192795e-06, 3.090173e-07, 1.005581e-07},
        {1.617608e-06, -5.088084e-07, 7.842862e-08, 5.921927e-08, -3.983904e-09},
        {2.278587e-07, -5.369446e-08, 1.055901e-07, -1.492638e-08, -2.297899e-09, 4.305516e-10},
        {-5.430918e-07, -6.018834e-08, 6.016117e-09, 1.184089e-09, -3.263133e-10, -2.155734e-10, 2.201492e-12},
        {3.511802e-07, 2.051571e-07, 3.283774e-08, 3.527325e-09, -5.852631e-10, 5.894186e-13, -2.490917e-11, 2.618492e-14},
        {2.000138e-07, 1.545761e-08, 6.581511e-09, -1.939188e-10, -3.188100e-10, -4.622370e-12, -1.839716e-12, 3.433027e-13, -1.582356e-13},
        {1.185910e-07, 9.242485e-08, 1.559103e-09, -1.218482e-09, -7.009250e-12, -1.665717e-12, 8.287277e-13, -2.253113e-13, 6.146758e-14, -3.680146e-15},
        {2.499947e-07, 5.109058e-08, -5.569401e-09, -4.102929e-11, -4.954720e-11, -3.079418e-12, -2.592400e-13, 6.990761e-15, 4.666506e-15, 2.327112e-15, 4.178813e-16},
        {-2.412758e-07, 9.425682e-09, 9.423767e-10, -1.423762e-10, -1.684627e-11, 1.485202e-12, -6.080011e-15, 1.991118e-15, -2.808707e-16, -1.931958e-16, -4.972620e-17, 9.453480e-18},
        {1.897726e-07, -3.115794e-08, 6.329864e-10, 1.465905e-10, -2.121690e-11, 8.211853e-13, 7.983568e-15, -4.085453e-15, -5.673872e-16, 1.011502e-16, -1.886234e-18, 5.040677e-19, -1.740105e-20},
        {2.085696e-07, -2.899374e-08, 2.265965e-09, -6.869797e-11, -3.497644e-13, 1.066509e-12, -5.319475e-14, 3.619424e-16, -1.107624e-16, 2.662279e-17, 4.535095e-18, -5.913334e-19, -5.944658e-20, -2.262992e-20},
        {-1.079476e-07, -1.003908e-08, -1.338232e-09, 9.541567e-11, 2.997156e-13, 3.863116e-13, -1.913797e-14, 2.807701e-15, -2.118718e-16, 1.681101e-17, 1.843772e-18, 7.271561e-20, 4.841846e-21, 2.404124e-21, -7.175132e-22},
        {2.014168e-08, 5.933984e-09, -7.231132e-10, 1.117500e-10, -6.036660e-12, 1.271144e-13, 2.215139e-14, 2.834375e-15, -1.124594e-16, 3.466073e-18, 2.196550e-19, -2.147601e-21, -5.783243e-21, -5.907583e-22, 1.361363e-23, -9.622663e-24},
        {-2.053255e-08, 1.365686e-08, -6.463121e-10, -6.335163e-11, 4.634112e-12, -1.008690e-13, 6.651415e-15, -2.336654e-16, -4.506173e-17, -3.345570e-18, -1.297775e-19, 1.641810e-20, 1.458026e-21, 9.143158e-23, -1.314353e-23, -1.297677e-24, -5.912951e-25}
    };
    
    private static final double[][] S = {
        {0.000000e+00, 1.543100e-09, -9.038663e-07},
        {0.000000e+00, 2.679913e-07, -2.114060e-07, 1.972029e-07},
        {0.000000e+00, -4.495183e-07, 1.481469e-07, -1.200594e-08, 6.525622e-09},
        {0.000000e+00, -8.071402e-08, -5.233139e-08, -7.101529e-09, 3.875484e-10, -1.648291e-09},
        {0.000000e+00, 2.124541e-08, -4.652940e-08, 1.887106e-10, -1.784736e-09, -4.327862e-10, -5.529349e-11},
        {0.000000e+00, 6.932533e-08, 9.281954e-09, -3.061286e-09, -2.635883e-10, 6.372263e-12, 1.053317e-11, 4.467248e-13},
        {0.000000e+00, 4.046208e-08, 5.356569e-09, -8.700160e-10, 9.114646e-11, 1.614656e-11, 8.630223e-12, 3.815509e-13, 1.535894e-13},
        {0.000000e+00, 1.415496e-08, -2.226693e-09, -5.631418e-10, 1.716785e-11, -5.557411e-12, 2.939957e-12, -1.846417e-13, -1.013460e-15, 7.439440e-15},
        {0.000000e+00, -8.090713e-08, -3.083412e-09, -8.981878e-10, -4.631894e-11, -3.123481e-12, -5.515369e-13, -2.552964e-15, -1.052142e-14, -6.979569e-16, -9.926636e-17},
        {0.000000e+00, -1.658314e-08, -5.093843e-09, -6.858268e-10, -2.675870e-11, 1.980929e-12, 1.344238e-13, -3.726783e-14, 1.169505e-15, 2.589413e-16, -1.732831e-17, -1.398715e-17},
        {0.000000e+00, -2.339218e-08, 1.402947e-09, 9.050585e-11, 8.901785e-13, 2.018067e-13, 9.380445e-14, 7.948671e-15, 3.706467e-16, 6.188477e-17, 9.249478e-18, -2.746314e-19, -1.022157e-19},
        {0.000000e+00, 2.163403e-08, -2.538041e-09, 3.007388e-10, -2.956462e-12, 1.217185e-12, -8.943584e-15, -9.593076e-16, -1.103966e-16, 4.864412e-17, -4.147983e-18, -6.320627e-20, 1.648767e-19, 2.498929e-20},
        {0.000000e+00, 1.478301e-08, -1.198967e-10, 5.074209e-11, -3.803194e-12, -2.224904e-13, 2.688011e-15, -3.292102e-16, -9.139545e-17, 1.488120e-17, -5.464044e-20, -1.856212e-19, -1.698698e-20, 3.287442e-21, -6.690608e-23},
        {0.000000e+00, 4.102195e-09, -1.020846e-09, 3.362769e-11, 1.077659e-12, 8.666462e-14, -2.490520e-14, 2.971132e-16, 7.671713e-17, 1.011607e-17, 3.302119e-19, 3.573356e-20, 2.985821e-21, -6.897422e-23, -6.357548e-23, -2.588365e-24},
        {0.000000e+00, 1.675193e-08, 7.879720e-10, -4.279819e-11, 5.269790e-12, -1.256583e-14, -1.573029e-14, -2.713925e-16, 1.025863e-17, -5.791262e-18, 1.338411e-19, -2.836551e-21, 4.459567e-22, 2.609620e-24, -2.768834e-23, -2.912985e-24, 5.448819e-26}
    };
    
    // Баллистический коэффициент (может быть установлен извне)
    private double ballisticCoefficient = 0.0413;
    
    // Параметры для расчета звездного времени
    private int day = 1, month = 1, year = 2000;
    
    /**
     * Устанавливает баллистический коэффициент.
     * @param bc новое значение баллистического коэффициента
     */
    public void setBallisticCoefficient(double bc) {
        this.ballisticCoefficient = bc;
    }
    
    /**
     * Устанавливает текущую дату для расчета звездного времени.
     */
    public void setCurrentDate(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }
    
    
       /**
     * Устанавливает текущую дату и время для расчета.
     * 
     * @param year год (полный, например 2023)
     * @param month месяц (1-12)
     * @param day день (1-31)
     * @param hour час (0-23)
     * @param minute минута (0-59)
     * @param second секунда (0-59)
     */
    public void setDateTime(int year, int month, int day, int hour, int minute, double second) {
        double mjd = dateToMJD(year, month, day, hour, minute, second);
        this.secondsSinceJ2000 = (mjd - MJD_J2000) * 86400.0;
    }
    
    /**
     * Преобразует дату и время в модифицированную юлианскую дату (MJD).
     * 
     * @param year год
     * @param month месяц (1-12)
     * @param day день (1-31)
     * @param hour час (0-23)
     * @param minute минута (0-59)
     * @param second секунда (0-59)
     * @return модифицированная юлианская дата
     */
    private double dateToMJD(int year, int month, int day, int hour, int minute, double second) {
        // Проверка на високосный год
        int leap = ((year % 4 == 0) && ((year % 100 != 0) || (year % 400 == 0))) ? 1 : 0;
        
        // Расчет номера дня в году
        int dayOfYear = day;
        for (int i = 1; i < month; i++) {
            dayOfYear += MONTH_DAYS[leap][i - 1];
        }
        
        // Расчет юлианской даты
        int a = (14 - month) / 12;
        int y = year + 4800 - a;
        int m = month + 12 * a - 3;
        int jdn = day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045;
        
        // Модифицированная юлианская дата
        double mjd = jdn - 2400000.5;
        
        // Добавляем время суток
        double timeFraction = (hour + minute / 60.0 + second / 3600.0) / 24.0;
        return mjd + timeFraction;
    }
    
    /**
     * Возвращает время в секундах с эпохи J2000.
     */
    public double getSecondsSinceJ2000() {
        return secondsSinceJ2000;
    }
    
    
    /**
     * Вычисляет правые части уравнений движения КА.
     */
    @Override
    public boolean compute(double t, double[] y, double[] f, Object parm) {
        if (y.length != 6 || f.length != 6) {
            LOG.error("Некорректные размеры массивов y или f");
            return false;
        }
        
        try {
            // Положение и скорость
            double x = y[0], yPos = y[1], z = y[2];
            double vx = y[3], vy = y[4], vz = y[5];
            
            // Вычисление ускорений
            double[] accG = calculateGravityAcceleration(x, yPos, z);
            double[] accE = calculateEarthRotationAcceleration(x, yPos, vx, vy);
            double[] accAero = calculateAerodynamicAcceleration(x, yPos, z, vx, vy, vz);
            
            // Суммарное ускорение
            double ax = accG[0] + accE[0] + accAero[0];
            double ay = accG[1] + accE[1] + accAero[1];
            double az = accG[2] + accE[2] + accAero[2];
            
            // Запись производных
            f[0] = vx; // dx/dt = vx
            f[1] = vy; // dy/dt = vy
            f[2] = vz; // dz/dt = vz
            f[3] = ax; // dvx/dt = ax
            f[4] = ay; // dvy/dt = ay
            f[5] = az; // dvz/dt = az
            
            return true;
        } catch (Exception e) {
            LOG.error("Ошибка при вычислении правых частей: ", e);
            return false;
        }
    }
    
    /**
     * Вычисляет гравитационное ускорение с учетом гармоник.
     */
    private double[] calculateGravityAcceleration(double x, double y, double z) {
        double[] acc = new double[3];
        double R = Math.sqrt(x*x + y*y + z*z);
        double locR1 = Math.sqrt(x*x + y*y);
        
        double sPhi, cPhi, tPhi, sLam, cLam;
        if (locR1 > 1E-40) {
            sPhi = z / R;
            cPhi = locR1 / R;
            tPhi = sPhi / cPhi;
            sLam = y / locR1;
            cLam = x / locR1;
        } else {
            sPhi = (z < 0) ? -1.0 : 1.0;
            cPhi = 0;
            tPhi = 0;
            sLam = 0;
            cLam = 1;
        }
        
        // Полиномы Лежандра и коэффициенты
        double[][] P = new double[17][17];
        double[] fSml = new double[17];
        double[] fCml = new double[17];
        double[][] A = new double[17][17];
        double[][] B = new double[17][17];
        
        P[0][0] = 1.0;
        P[1][0] = sPhi;
        P[1][1] = cPhi;
        fSml[1] = sLam;
        fCml[1] = cLam;
        
        for (int m = 2; m <= 16; m++) {
            fSml[m] = sLam * fCml[m-1] + cLam * fSml[m-1];
            fCml[m] = cLam * fCml[m-1] - sLam * fSml[m-1];
        }
        
        for (int n = 2; n <= 16; n++) {
            for (int m = 0; m <= n; m++) {
                A[n][m] = C[n-2][m] * fCml[m] + S[n-2][m] * fSml[m];
                B[n][m] = S[n-2][m] * fCml[m] - C[n-2][m] * fSml[m];
                
                if (n > m) {
                    P[n][m] = ((2*n-1)*sPhi*P[n-1][m] - (n+m-1)*P[n-2][m]) / (double)(n-m);
                } else {
                    P[n][n] = (2*n-1)*P[n-1][n-1]*cPhi;
                }
            }
        }
        
        double ReR = Re / R;
        double koefstep1 = ReR;
        double koefstep2 = mu / (R * R);
        double[] q = new double[3];
        
        for (int n = 2; n <= 16; n++) {
            double qr = 0.0, qm = 0.0, ql = 0.0;
            
            for (int m = 0; m <= n; m++) {
                qr += A[n][m] * P[n][m];
                if ((m+1) > n) {
                    qm += A[n][m] * (0 - m * tPhi * P[n][m]);
                } else {
                    qm += A[n][m] * (P[n][m+1] - m * tPhi * P[n][m]);
                }
                ql += m * B[n][m] * P[n][m];
            }
            
            koefstep1 *= ReR;
            q[0] += qr * (n+1) * koefstep2 * koefstep1;
            q[1] += qm * koefstep2 * koefstep1;
            q[2] += ql * koefstep2 * koefstep1 / cPhi;
        }
        
        // Матрица преобразования
        double[][] N = new double[3][3];
        double locR3 = R * R * R;
        
        if ((sPhi != 1 && sPhi != -1) && (locR1 > 1E-40)) {
            N[0][0] = -x / R;
            N[0][1] = -x * z / (R * locR1);
            N[0][2] = -y / locR1;
            N[1][0] = -y / R;
            N[1][1] = -y * z / (R * locR1);
            N[1][2] = x / locR1;
            N[2][0] = -z / R;
            N[2][1] = locR1 / R;
            N[2][2] = 0.0;
        } else {
            N[0][0] = 0.0;
            N[0][1] = 0.0;
            N[0][2] = 0.0;
            N[1][0] = 0.0;
            N[1][1] = 0.0;
            N[1][2] = 0.0;
            N[2][0] = -z / R;
            N[2][1] = 0.0;
            N[2][2] = 0.0;
        }
        
        // Итоговое ускорение
        acc[0] = -mu * x / locR3 + N[0][0]*q[0] + N[0][1]*q[1] + N[0][2]*q[2];
        acc[1] = -mu * y / locR3 + N[1][0]*q[0] + N[1][1]*q[1] + N[1][2]*q[2];
        acc[2] = -mu * z / locR3 + N[2][0]*q[0] + N[2][1]*q[1] + N[2][2]*q[2];
        
        return acc;
    }
    
    /**
     * Вычисляет переносное ускорение из-за вращения Земли.
     */
    private double[] calculateEarthRotationAcceleration(double x, double y, double vx, double vy) {
        double ax = omEarth * omEarth * x + 2 * omEarth * vy;
        double ay = omEarth * omEarth * y - 2 * omEarth * vx;
        double az = 0.0;
        
        return new double[]{ax, ay, az};
    }
    
    /**
     * Вычисляет аэродинамическое ускорение.
     */
    private double[] calculateAerodynamicAcceleration(double x, double y, double z, 
                                                     double vx, double vy, double vz) {
        double r = Math.sqrt(x*x + y*y + z*z);
        double sPhi = z / r;
        double altitude = r - Re * (1 - flat * sPhi * sPhi);
        
        // Плотность атмосферы
        double density = calculateAtmosphericDensity(altitude);
        
        // Скорость относительно атмосферы
        double vRelX = vx + omEarth * y;
        double vRelY = vy - omEarth * x;
        double vRelZ = vz;
        double vRel = Math.sqrt(vRelX*vRelX + vRelY*vRelY + vRelZ*vRelZ);
        
        // Аэродинамическое ускорение
        double factor = -ballisticCoefficient * density * vRel * 1e-6 / 9.80665;
        double ax = factor * vRelX;
        double ay = factor * vRelY;
        double az = factor * vRelZ;
        
        return new double[]{ax, ay, az};
    }
    
    /**
     * Модель плотности атмосферы (точная реализация из V2.c).
     */
    private double calculateAtmosphericDensity(double altitude) {
        // Высота в км
        double h = altitude;
        
        // Табличные данные из V2.c
        double[] L = {0.0, 20.0, 60.0, 100.0, 150.0, 300.0, 600.0, 900.0};
        double[] ro = {
            1.2280, 0.90130E-1, 0.31043E-3, 0.53675E-6, 
            0.20078E-8, 0.18651E-10, 0.11273E-12, 0.56916E-14
        };
        double[] A = {
            0.90764E-1, 0.16739, 0.12378, 0.17527, 
            0.45825E-1, 0.19885E-1, 0.14474E-1, 0.39247E-2
        };
        double[] B = {
            -0.20452E-2, 0.62669E-3, -0.86999E-3, 0.12870E-2,
            0.10167E-3, 0.97266E-5, 0.15127E-4, 0.0
        };
        
        // Определение интервала
        int i = 0;
        if (h >= 0.0 && h < 20.0) i = 0;
        else if (h < 60.0) i = 1;
        else if (h < 100.0) i = 2;
        else if (h < 150.0) i = 3;
        else if (h < 300.0) i = 4;
        else if (h < 600.0) i = 5;
        else if (h < 900.0) i = 6;
        else return 0.0;
        
        // Расчет плотности
        double argexp = -A[i] * (h - L[i]) + B[i] * Math.pow(h - L[i], 2);
        return ro[i] * 1.0e9 * Math.exp(argexp);
    }
    
    /**
     * Преобразует координаты из ИСК в ГСК (аналог функции gsc из V2.c).
     */
public void convertISKtoGSK(double tRelative, double[] iskState, double[] gskState) {
    double tAbsolute = secondsSinceJ2000 + tRelative;
    double[] starTime = calculateStarTime(tAbsolute);
        double cosS = starTime[0];
        double sinS = starTime[1];
        
        // Преобразование координат
        gskState[0] = cosS * iskState[0] + sinS * iskState[1];
        gskState[1] = -sinS * iskState[0] + cosS * iskState[1];
        gskState[2] = iskState[2];
        
        // Преобразование скоростей
        gskState[3] = cosS * iskState[3] + sinS * iskState[4] + omEarth * gskState[1];
        gskState[4] = -sinS * iskState[3] + cosS * iskState[4] - omEarth * gskState[0];
        gskState[5] = iskState[5];
    }
    
    /**
     * Преобразует координаты из ГСК в ИСК (аналог функции isc из V2.c).
     */
  public void convertGSKtoISK(double tRelative, double[] gskState, double[] iskState) {
        // Расчет звездного времени
           double tAbsolute = secondsSinceJ2000 + tRelative;
    double[] starTime = calculateStarTime(tAbsolute);
        double cosS = starTime[0];
        double sinS = starTime[1];
        
        // Преобразование координат
        iskState[0] = cosS * gskState[0] - sinS * gskState[1];
        iskState[1] = sinS * gskState[0] + cosS * gskState[1];
        iskState[2] = gskState[2];
        
        // Преобразование скоростей
        iskState[3] = cosS * gskState[3] - sinS * gskState[4] - omEarth * iskState[1];
        iskState[4] = sinS * gskState[3] + cosS * gskState[4] + omEarth * iskState[0];
        iskState[5] = gskState[5];
    }
    
//    /**
//     * Расчет звездного времени (аналог AstroTime_2000 из V2.c).
//     */
//    private double[] calculateStarTime(double t) {
//        double dMU = 0.002737811906;
//        double time = 43200.0;
//        double dS = 0.0;
//        
//        // Расчет модифицированной юлианской даты
//        double dateTime = calcIntervalDateTimeBase(t, day, month, year);
//        double Tm = dateTime - Math.floor(dateTime);
//        
//        if (Tm >= 0.125) {
//            dS = 2 * Math.PI * (Tm - 0.125) * (1 + dMU);
//        } else {
//            dS = 2 * Math.PI * (Tm + 0.875) * (1 + dMU);
//        }
//        
//        // Расчет звездного времени
//        int den2000 = 1, mes2000 = 1, god2000 = 2000;
//        double T = (Math.floor(dateTime - 0.125) - calcIntervalDateTimeBase(time, den2000, mes2000, god2000)) / 36525.0;
//        
//        double Om = 450160.280 - 6962890.539 * T + 7.455 * Math.pow(T, 2) + 0.008 * Math.pow(T, 3);
//        double dPsi = -17.1996 * Math.sin(Om / 3600.0 / 180.0 * Math.PI);
//        
//        double So = (24110.54841 + 8640184.812866 * T + 0.093104 * Math.pow(T, 2) 
//                  - 0.0000062 * Math.pow(T, 3) + 0.061165 * dPsi) / 86400.0 * 2 * Math.PI;
//        
//        So = So - Math.floor(So / (2 * Math.PI)) * 2 * Math.PI;
//        double S = So + dS;
//        S = S - Math.floor(S / (2 * Math.PI)) * 2 * Math.PI;
//        
//        return new double[]{Math.cos(S), Math.sin(S)};
//    }
  
    
      /**   * Обновленный метод для расчета звездного времени с учетом эпохи J2000.
     */
    private double[] calculateStarTime(double t) {
        // Общее время с учетом эпохи J2000
        double totalSeconds = secondsSinceJ2000 + t;
        double daysSinceJ2000 = totalSeconds / 86400.0;
        
        // Расчет звездного времени
        double T = daysSinceJ2000 / 36525.0;
        double gmst = 24110.54841 + 8640184.812866 * T + 0.093104 * T*T - 0.0000062 * T*T*T;
        
        // Нормализация
        gmst = Math.toRadians(gmst / 3600.0 % 360.0);
        
        return new double[]{Math.cos(gmst), Math.sin(gmst)};
    }
    
    
    /**
     * Расчет интервала времени (аналог CalcIntervalDateTimeBase из V2.c).
     */
    private double calcIntervalDateTimeBase(double datetime, int den, int mes, int god) {
        int[][] monthDays = {
            {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31}, // Не високосный
            {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31}  // Високосный
        };
        
        double dateDelta = 693594.0; // Число дней между 1/1/0001 и 12/31/1899
        
        int leap = ((god % 4 == 0) && ((god % 100 != 0) || (god % 400 == 0))) ? 1 : 0;
        
        // Расчет номера дня в году
        int dayOfYear = den;
        for (int i = 1; i < mes; i++) {
            dayOfYear += monthDays[leap][i - 1];
        }
        
        // Расчет полного числа дней
        int j = god - 1;
        double data = j * 365 + j / 4 - j / 100 + j / 400 + dayOfYear - dateDelta;
        
        // Добавление времени суток
        double time = datetime / (24.0 * 60.0 * 60.0);
        return data + time;
    }
}