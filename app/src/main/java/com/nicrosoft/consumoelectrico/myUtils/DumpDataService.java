package com.nicrosoft.consumoelectrico.myUtils;

import android.util.Log;
import android.widget.Toast;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class DumpDataService extends JobService {
    public static final String JOB_TAG = "DumpDataService";
    CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    public boolean onStartJob(JobParameters job) {
        // Answers the question: "Is there still work going on?"
        String finalName = "com_nicrosoft_consumoelectrico_BACKUP";
        String defPath = RestoreHelper.getInternalStoragePath(getApplicationContext());
        mDisposable.add(
        Single.create((SingleOnSubscribe<Boolean>) e -> e.onSuccess(CSVHelper.saveAllToCSV(defPath, finalName, getApplicationContext())))
                .subscribeOn(Schedulers.io()) //Run on IO Thread
                .observeOn(AndroidSchedulers.mainThread()) //Run on UI Thread
                .doFinally(() -> {
                    if(!mDisposable.isDisposed())
                        mDisposable.dispose();
                    jobFinished(job, false);
                })
                .subscribe(dumped -> {
                    /*if(dumped)
                        Log.e("EDER", "DATA DUMPED");
                    else
                        Log.e("EDER", "DATA NOT DUMPED");
                        */

                },throwable -> {
                    Log.e("EDER", "ERROR DUMPING DATA");
                })
        );
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        // si el trabajo falla retornar true para reintentar
        return true;
    }
}
