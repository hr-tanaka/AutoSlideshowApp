package jp.techacademy.hiroaki.tanaka.autoslideshowapp

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.database.Cursor
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100
    private var mTimer: Timer? = null
    private var mCursor: Cursor? = null

    private var mHandler = Handler()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }

        start_button.setOnClickListener {
            if (mTimer == null){
                start_button.text = "停止"
                next_button.setEnabled(false)
                return_button.setEnabled(false)

                mTimer = Timer()
                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        mHandler.post {
                            getContentsNext()
                        }
                    }
                }, 2000, 2000) // 最初に始動させるまで100ミリ秒、ループの間隔を100ミリ秒 に設定

            }
            else {
                if (mTimer != null){
                    mTimer!!.cancel()
                    mTimer = null
                }
                start_button.text = "再生"
                next_button.setEnabled(true)
                return_button.setEnabled(true)
            }
        }

        next_button.setOnClickListener {
            getContentsNext()
        }

        return_button.setOnClickListener {
            getContentsPrevious()
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        if (mCursor!= null) {
            mCursor?.close()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }else{
                    showAlertDialog()
                }
        }
    }

    private fun getContentsInfo() {
        if (mCursor== null) {
            // 画像の情報を取得する
            val resolver = contentResolver
            mCursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目（null = 全項目）
                null, // フィルタ条件（null = フィルタなし）
                null, // フィルタ用パラメータ
                null // ソート (nullソートなし）
            )
            if (mCursor!!.moveToFirst()) {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                getContents()
            }
        }
    }

    private fun getContentsNext() {
        if (!mCursor!!.moveToNext()) {
            mCursor!!.moveToFirst()
        }
        getContents()
    }
    private fun getContentsPrevious() {
        if (!mCursor!!.moveToPrevious()) {
            mCursor!!.moveToLast()
        }
        getContents()
    }

    private fun getContents() {
        // indexからIDを取得し、そのIDから画像のURIを取得する
        val fieldIndex = mCursor?.getColumnIndex(MediaStore.Images.Media._ID)
        val id = mCursor?.getLong(fieldIndex!!)
        val imageUri =
            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id!!)
        Log.d("ANDROID", "URI : " + imageUri.toString())
        imageView.setImageURI(imageUri)
    }

    private fun showAlertDialog() {
        // AlertDialog.Builderクラスを使ってAlertDialogの準備をする
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("終了します")

        // 肯定ボタンに表示される文字列、押したときのリスナーを設定する
        alertDialogBuilder.setPositiveButton("OK"){dialog, which ->
            Log.d("UI_PARTS", "肯定ボタン")
            finish()
        }
        // AlertDialogを作成して表示する
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }




}