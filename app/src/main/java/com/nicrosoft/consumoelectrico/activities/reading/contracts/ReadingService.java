package com.nicrosoft.consumoelectrico.activities.reading.contracts;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nicrosoft.consumoelectrico.realm.Lectura;
import com.nicrosoft.consumoelectrico.realm.Periodo;

import java.util.Date;

/**
 * Created by Eder Xavier Rojas on 15/01/2017.
 */

public interface ReadingService {

    @NonNull
    Boolean readingForDateExist(Date date, Periodo periodo);
    boolean saveReading(Lectura lectura, boolean finishPeriod, String medidor_id);

    boolean isReadingOverRange(float reading, Date date, Periodo periodo);

    Periodo getActivePeriod(String medidor_id);
    Lectura getLastReading(Periodo periodo, boolean old_readings_if_more_than_a_period);

}
