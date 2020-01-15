package com.example.numberpi;


import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    boolean finished;
    long startTime,lastTime;
    CountTask task = new CountTask();
    TextView outPi,timePassed;
    int numberOfDigits;
    StringBuilder pi;
    EditText numbers;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        outPi = findViewById(R.id.pi);
        numbers = findViewById(R.id.numbers);
        timePassed = findViewById(R.id.time);
        progressBar = findViewById(R.id.process);
    }

    class CountTask extends AsyncTask<Void, Long, Void> {

        @Override
        protected void onProgressUpdate(Long... values) {
            super.onProgressUpdate(values);
            long seconds,minutes,hours,days;
            seconds=values[0]%60;
            minutes=(values[0]/60)%60;
            hours=(values[0]/3600)%24;
            days = values[0]/86400;
            timePassed.setText("Примерно осталось: "+(days!=0?days+" дн ":"")+(hours!=0?hours+" ч ":"")+(minutes!=0&&days==0?minutes+" мин ":"")+(seconds!=0&&hours==0?seconds+" сек ":""));
        }

        @Override
        protected Void doInBackground(Void... voids) {

            startTime = System.currentTimeMillis();
            finished=false;
            pi = new StringBuilder(numberOfDigits);
            int unchecked=0,count=0;
            int[] remainders= new int[10*numberOfDigits/3];
            for (int i = 0; i < remainders.length; i++) {
                remainders[i]=2;
            }
            for (int i = 0; i < numberOfDigits; i++) {
                int carriedOver = 0, sum = 0;
                for (int j = remainders.length - 1; j >= 0; j--) {
                    remainders[j]*=10;
                    sum=remainders[j]+carriedOver;
                    int quotient = sum / (j * 2 + 1);
                    remainders[j] = sum % (j * 2 + 1);
                    carriedOver = quotient * j;
                }
                remainders[0] = sum % 10;
                int q = sum / 10;
                if (q == 9) {
                    unchecked++;
                } else if (q == 10) {
                    q = 0;
                    for (int k = 1; k <= unchecked; k++) {
                        int replaced = Integer.parseInt(pi.substring(i - k, i - k + 1));
                        if (replaced == 9) {
                            replaced = 0;
                        } else {
                            replaced++;
                        }
                        pi.deleteCharAt(i - k);
                        pi.insert(i - k, replaced);
                    }
                    unchecked = 1;
                } else {
                    unchecked = 1;
                }
                pi.append(q);
                count++;
                if (isCancelled())
                    return null;
                if (System.currentTimeMillis()-lastTime>5000 && System.currentTimeMillis()-startTime>3000) {
                    lastTime=System.currentTimeMillis();
                    publishProgress(((System.currentTimeMillis()-startTime)*numberOfDigits/count-(System.currentTimeMillis()-startTime))/1000);
                }
            }
            finished=true;
            return null;
        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
            if (pi != null)
                finih();

        }

        @Override
        protected void onPostExecute(Void aVoid) {
          finih();
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                    try {
                        int entered = Integer.parseInt(numbers.getText().toString()) + 1;
                        if (entered<=8_000_001&&(!finished || numberOfDigits != entered)) {
                            numberOfDigits = entered;
                            progressBar.setVisibility(ProgressBar.VISIBLE);
                            timePassed.setText("");
                            switch (task.getStatus()) {
                                case RUNNING:
                                    Toast.makeText(this, "В процессе...", Toast.LENGTH_SHORT).show();
                                    break;
                                case FINISHED:
                                    task = new CountTask();
                                case PENDING:
                                    task.execute();
                            }
                        }
                        else timePassed.setText("Некорректный ввод");
                    } catch (Exception e) {
                        timePassed.setText("Некорректный ввод");
                    }
                break;
            case R.id.stop:
                task.cancel(true);
                break;
        }
    }
    public void finih(){
        if (pi.length()>1)
            pi.insert(1,".");
        if (pi.length()==1)
            pi.replace(0,1,"3");
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        long millis,seconds,minutes,passedTime=System.currentTimeMillis()-startTime;
        millis=passedTime%1000;
        seconds=(passedTime/1000)%60;
        minutes=passedTime/60000;
        timePassed.setText("Завершено за: "+(minutes!=0?minutes+" мин ":"")+(seconds!=0?seconds+" сек ":"")+(millis!=0?millis+" мс ":""));
        outPi.setText(pi.toString());
    }
}