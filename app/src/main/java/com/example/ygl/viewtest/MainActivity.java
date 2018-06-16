package com.example.ygl.viewtest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.spb)
    SlenderProgressBar mSpb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        updateSPB();
    }

    private void updateSPB() {
        Timer timer=new Timer();
        TimerTask timerTask=new TimerTask() {
            int i=0;
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        i+=10;
                        mSpb.drawRect(i);
                    }
                });
            }
        };
        timer.schedule(timerTask,1000,1000);
    }

    @OnClick(R.id.spb)
    public void onViewClicked() {
    }
}
