#include <iostream>
#include <math.h>
#include <immintrin.h>
#include <omp.h>
#include <jni.h>
#include <ctime>
#include "org_wso2_siddhi_extension_var_models_montecarlo_MonteCarloNativeSimulation.h"

jdouble NormalCDFInverse(jdouble p);

JNIEXPORT jdoubleArray JNICALL Java_org_wso2_siddhi_extension_var_models_montecarlo_MonteCarloNativeSimulation_simulate
(JNIEnv *env, jobject thisObj,jdouble mean, jdouble std, jdouble timeSlice, jdouble currentPrice,jint numberOfTrials,jint calculationsPerDay) {

    __m256d stochasticFactorTemp = _mm256_set1_pd(std * sqrt(timeSlice));
    __m256d drift = _mm256_set1_pd((mean - (std * std / 2)) * timeSlice);


    jdouble *finalValues = new jdouble[numberOfTrials];
    jdoubleArray tempArray= env->NewDoubleArray(numberOfTrials);
    unsigned int seed = (unsigned int) time(0);


#pragma omp parallel for
    for (jint i = 0; i < numberOfTrials; i += 4) {
        __m256d tempStockValues = _mm256_set1_pd(currentPrice);
        for (jint j = 0; j < calculationsPerDay; ++j) {
            __m256d randomDistVals = _mm256_set_pd(NormalCDFInverse((jdouble) rand_r(&seed) / RAND_MAX),
                                                   NormalCDFInverse((jdouble) rand_r(&seed) / RAND_MAX),
                                                   NormalCDFInverse((jdouble) rand_r(&seed) / RAND_MAX),
                                                   NormalCDFInverse((jdouble) rand_r(&seed) / RAND_MAX));


            __m256d stochasticFactor = _mm256_mul_pd(stochasticFactorTemp, randomDistVals);
            __m256d brownianMotionFactor = _mm256_add_pd(drift, stochasticFactor);

            jdouble *temp = (jdouble *) &brownianMotionFactor;
            brownianMotionFactor = _mm256_set_pd(exp(temp[0]), exp(temp[1]), exp(temp[2]), exp(temp[3]));
            tempStockValues = _mm256_mul_pd(tempStockValues, brownianMotionFactor);

            if (j == calculationsPerDay-1) {
                temp = (jdouble *) &tempStockValues;
                        finalValues[i] = temp[0];
                        finalValues[i + 1] = temp[1];
                        finalValues[i + 2] = temp[2];
                        finalValues[i + 3] = temp[3];
            }
        }
    }

    env->SetDoubleArrayRegion(tempArray, 0 , numberOfTrials, finalValues);
    delete[] finalValues;
    return tempArray;
}

jdouble NormalCDFInverse(jdouble p) {
    if (p <= 0.0 || p >= 1.0) {
        return 0.0;
    }

    jdouble c[] = {2.515517, 0.802853, 0.010328};
    jdouble d[] = {1.432788, 0.189269, 0.001308};
    jdouble t;

    if (p < 0.5) {
        t = sqrt(-2.0 * log(p));
        return -(t - ((c[2] * t + c[1]) * t + c[0]) / (((d[2] * t + d[1]) * t + d[0]) * t + 1.0));
    } else {
        t = sqrt(-2.0 * log(1 - p));
        return t - ((c[2] * t + c[1]) * t + c[0]) /(((d[2] * t + d[1]) * t + d[0]) * t + 1.0);
    }
}


