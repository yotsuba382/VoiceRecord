package com.example.ryotafukuzawa.voicerecord;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private MediaRecorder mediarecorder;
    static final String filePath = "/storage/emulated/0/Audio/sample.wav";
    private File file = new File(filePath);

    private TextView textView;

    private FileInputStream in = null; //file読み込み用
    //private byte[] byteData = new byte[4096];
    //private short[] shortData = new short[2048];
    //private int bufSize;

    AudioRecord audioRecord; //録音用のオーディオレコードクラス
    final static int SAMPLING_RATE = 44100; //オーディオレコード用サンプリング周波数
    private int bufSize;//オーディオレコード用バッファのサイズ
    private short[] shortData; //オーディオレコード用バッファ
    private MyWaveFile wav1 = new MyWaveFile();

    int nnumberofFilters = 24;
    int nlifteringCoefficient = 22;
    boolean oisLifteringEnabled = true;
    boolean oisZeroThCepstralCoefficientCalculated = false;
    int nnumberOfMFCCParameters = 12; //without considering 0-th
    double dsamplingFrequency = 44100.0;
    int nFFTLength = 512;

    private double[] dparameters;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        //button 作成
        Button button1 = (Button) findViewById(R.id.button);
        Button button2 = (Button) findViewById(R.id.button2);
        Button cal = (Button) findViewById(R.id.button3);

        TextView text = (TextView)findViewById(R.id.textView2);

        //initAudioRecord();


        //start を押した処理
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startMediaRecord();

            }
        });

        //stopの処理
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecord();
            }
        });

        cal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(MainActivity.this, "MFCC Cal", Toast.LENGTH_SHORT).show();

                //waveのサイズ取得
                byte[] byteData = new byte[(int) file.length()];

                //wave load
                try {
                    in = new FileInputStream(file);
                    in.read(byteData);
                    in.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (oisZeroThCepstralCoefficientCalculated) {
                    //take in account the zero-th MFCC
                    nnumberOfMFCCParameters = nnumberOfMFCCParameters + 1;
                } //delete

                MFCC mfcc = new MFCC(nnumberOfMFCCParameters,
                        dsamplingFrequency,
                        nnumberofFilters,
                        nFFTLength,
                        oisLifteringEnabled,
                        nlifteringCoefficient,
                        oisZeroThCepstralCoefficientCalculated);

                int i;
                //ByteBuffer buf = ByteBuffer.wrap(byteData);

                //Byte -> Double
                double[] array = new double[((byteData.length) / 50)];
                for (i = 0; i < ((byteData.length) / 50); i++) {
                    Double od = new Double(byteData[i]);
                    array[i] = od.doubleValue();
                }

                //double[] dValue = new double[(byteData.length)/50];
                //for ( i = 0 ; i < ((byteData.length)/50); i++) dValue[i] = buf.getDouble();

                //check
                /*double[] x = new double[65];
                x[10] = x[30] = 10.0;
                double[] dparameters = mfcc.getParameters(x);*/

                double[] dparameters = mfcc.getParameters(array);

                TextView et1 = (TextView)findViewById(R.id.textView2);
                for ( i = 0; i < 12 ; i++) {
                    et1.append(String.valueOf(dparameters[i]) + " \n");
                }
            }
        });



    }

    private void initAudioRecord() {
        wav1.createFile(filePath);
        // AudioRecordオブジェクトを作成
        bufSize = android.media.AudioRecord.getMinBufferSize(SAMPLING_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLING_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufSize);

        shortData = new short[bufSize / 2];

        // コールバックを指定
        audioRecord.setRecordPositionUpdateListener(new AudioRecord.OnRecordPositionUpdateListener() {
            // フレームごとの処理
            @Override
            public void onPeriodicNotification(AudioRecord recorder) {
                // TODO Auto-generated method stub
                audioRecord.read(shortData, 0, bufSize / 2); // 読み込む
                wav1.addBigEndianData(shortData); // ファイルに書き出す
            }

            @Override
            public void onMarkerReached(AudioRecord recorder) {
                // TODO Auto-generated method stub

            }
        });
        // コールバックが呼ばれる間隔を指定
        audioRecord.setPositionNotificationPeriod(bufSize / 2);
    }

    private void startMediaRecord() {
        Toast.makeText(MainActivity.this, "Now Recording", Toast.LENGTH_SHORT).show();
        try {

            if (file.exists()) {
                //ファイルが存在する場合は削除する
                file.delete();
            }

            file = null;

            mediarecorder = new MediaRecorder();
            //マイクからの音声を録音する
            mediarecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //ファイルへの出力フォーマット DEFAULTにするとwavが扱えるはず
            mediarecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            //音声のエンコーダーも合わせてdefaultにする
            mediarecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            //ファイルの保存先を指定
            mediarecorder.setOutputFile(filePath);

            //録音の準備をする
            mediarecorder.prepare();
            //録音開始
            mediarecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    //停止
    private void stopRecord() {
        if (mediarecorder == null) {
            Toast.makeText(MainActivity.this, "mediarecorder = null", Toast.LENGTH_SHORT).show();
        } else {
            try {
                //録音停止
                Toast.makeText(MainActivity.this, "Stop Record", Toast.LENGTH_SHORT).show();
                mediarecorder.stop();
                mediarecorder.reset();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //byteデータ列をshortのオーディオサンプル列に変換
    void byte2short(short data[], byte bdata[]) {
        int i;
        for (i = 0; i < bdata.length / 2; i++) {
            // リトルエンディアン
            data[i] = (short) ((short) bdata[2 * i] + (short) bdata[2 * i + 1] * 256);
        }
        ;
    }

}