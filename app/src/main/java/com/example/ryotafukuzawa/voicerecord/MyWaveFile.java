package com.example.ryotafukuzawa.voicerecord;

/**
 * Created by yotsu on 2017/08/01.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import static com.example.ryotafukuzawa.voicerecord.MainActivity.SAMPLING_RATE;

public class MyWaveFile {
    private final int FILESIZE_SEEK = 4;
    private final int DATASIZE_SEEK = 40;

    private RandomAccessFile raf; //リアルタイム処理なのでランダムアクセスファイルクラスを使用する
    private File recFile; //録音後の書き込み、読み込みようファイル
    private String fileName = "/storage/emulated/0/Audio/sample.wav";//録音ファイルのパス
    private byte[] RIFF = {'R','I','F','F'}; //wavファイルリフチャンクに書き込むチャンクID用
    private int fileSize = 36;
    private byte[] WAVE = {'W','A','V','E'}; //WAV形式でRIFFフォーマットを使用する
    private byte[] fmt = {'f','m','t',' '}; //fmtチャンク　スペースも必要
    private int fmtSize = 16; //fmtチャンクのバイト数
    private byte[] fmtID = {1, 0}; // フォーマットID リニアPCMの場合01 00 2byte
    private short chCount = 1; //チャネルカウント モノラルなので1 ステレオなら2にする
    private int bytePerSec = SAMPLING_RATE * (fmtSize / 8) * chCount; //データ速度
    private short blockSize = (short) ((fmtSize / 8) * chCount); //ブロックサイズ (Byte/サンプリングレート * チャンネル数)
    private short bitPerSample = 16; //サンプルあたりのビット数 WAVでは8bitか16ビットが選べる
    private byte[] data = {'d', 'a', 't', 'a'}; //dataチャンク
    private int dataSize = 0; //波形データのバイト数


    public	void	createFile(String	fName){
        fileName	=	fName;
        //	ファイルを作成
        recFile	=	new	File(fileName);
        if(recFile.exists()){
            recFile.delete();
        }
        try	{
            recFile.createNewFile();
        }	catch	(IOException	e)	{
            //	TODO	Auto-generated	catch	block
            e.printStackTrace();
        }

        try	{
            raf	=	new	RandomAccessFile(recFile,	"rw");
        }	catch	(FileNotFoundException	e)	{
            //	TODO	Auto-generated	catch	block
            e.printStackTrace();
        }

        //wavのヘッダを書き込み
        try	{
            raf.seek(0);
            raf.write(RIFF);
            raf.write(littleEndianInteger(fileSize));
            raf.write(WAVE);
            raf.write(fmt);
            raf.write(littleEndianInteger(fmtSize));
            raf.write(fmtID);
            raf.write(littleEndianShort(chCount));
            raf.write(littleEndianInteger(SAMPLING_RATE)); //サンプリング周波数
            raf.write(littleEndianInteger(bytePerSec));
            raf.write(littleEndianShort(blockSize));
            raf.write(littleEndianShort(bitPerSample));
            raf.write(data);
            raf.write(littleEndianInteger(dataSize));

        }	catch	(IOException	e)	{
            //	TODO	Auto-generated	catch	block
            e.printStackTrace();
        }

    }


    private byte[] littleEndianInteger(int i){
        byte[] buffer = new byte[4];
        buffer[0] = (byte) i;
        buffer[1] = (byte) (i >> 8);
        buffer[2] = (byte) (i >> 16);
        buffer[3] = (byte) (i >> 24);

        return buffer;

    }

    // PCMデータを追記するメソッド
    public void addBigEndianData(short[] shortData){

        // ファイルにデータを追記
        try {
            raf.seek(raf.length());
            raf.write(littleEndianShorts(shortData));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // ファイルサイズを更新
        updateFileSize();

        // データサイズを更新
        updateDataSize();

    }

    // ファイルサイズを更新
    private void updateFileSize(){

        fileSize = (int) (recFile.length() - 8);
        byte[] fileSizeBytes = littleEndianInteger(fileSize);
        try {
            raf.seek(FILESIZE_SEEK);
            raf.write(fileSizeBytes);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    // データサイズを更新
    private void updateDataSize(){

        dataSize = (int) (recFile.length() - 44);
        byte[] dataSizeBytes = littleEndianInteger(dataSize);
        try {
            raf.seek(DATASIZE_SEEK);
            raf.write(dataSizeBytes);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    // short型変数をリトルエンディアンのbyte配列に変更
    private byte[] littleEndianShort(short s){

        byte[] buffer = new byte[2];

        buffer[0] = (byte) s;
        buffer[1] = (byte) (s >> 8);

        return buffer;

    }

    // short型配列をリトルエンディアンのbyte配列に変更
    private byte[] littleEndianShorts(short[] s){

        byte[] buffer = new byte[s.length * 2];
        int i;

        for(i = 0; i < s.length; i++){
            buffer[2 * i] = (byte) s[i];
            buffer[2 * i + 1] = (byte) (s[i] >> 8);
        }
        return buffer;
    }


    // ファイルを閉じる
    public void close(){
        try {
            raf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
