package klab.mastodon_api_test;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String HOST_MASTODON = "ojii-chan-mimamori.site";
    private final String ACCESS_TOKEN = "0a19fd3518413fc79ac375467893ddc5a39f86c0c177015b2e656d2e38ce0bd8";
    private final String CLIENT_ID = "3d93a67f16b0ddaa7327e1408c593d15a7a8ece03b864e85a035ce043e7442bf";
    private final String CLIENT_SECRET = "8b91a9d41eaa025a863b28a01405b3ef874323e922a5c9fc3a836736294a4d1a";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button userbutton = (Button) this.findViewById(R.id.userbutton);
        userbutton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                new AsyncTask<String, Void, String>() { //ボタンを押した時に非同期処理を開始します

                    @Override
                    protected String doInBackground(String... params) {
                        final StringBuilder result = new StringBuilder();
                        Uri.Builder uriBuilder = new Uri.Builder(); //Uri.Builderで要素を入力していく
                        uriBuilder.scheme("https");
                        uriBuilder.authority(HOST_MASTODON);
                        uriBuilder.path("/api/v1/accounts/verify_credentials");
                        final String uriStr = uriBuilder.build().toString(); //URIを作成して文字列に

                        try {
                            URL url = new URL(uriStr); //文字列からURLに変換
                            HttpURLConnection con = null; //HTTP接続の設定を入力していく
                            con = (HttpURLConnection) url.openConnection();
                            con.setRequestMethod("GET");
                            con.addRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
                            con.setDoInput(true);
                            con.connect(); //HTTP接続

                            final InputStream in = con.getInputStream(); //情報を受け取り表示するための形式に↓
                            final InputStreamReader inReader = new InputStreamReader(in);
                            final BufferedReader bufReader = new BufferedReader(inReader);
                            String line = null;
                            while((line = bufReader.readLine()) != null) {
                                result.append(line);
                            }
                            bufReader.close();
                            inReader.close();
                            in.close();
                        }

                        catch(Exception e) { //エラーの時に呼び出される
                            Log.e("button", e.getMessage());
                        }

                        return result.toString(); //onPostExecuteへreturn
                    }

                    @Override
                    protected void onPostExecute(String result) { //doInBackgroundが終わると呼び出される
                        TextView textView = (TextView) findViewById(R.id.textView);
                        textView.setText(result); //テキストビューに出力
                    }
                }.execute();
            }
        });

        Button postbutton = (Button) this.findViewById(R.id.postbutton);
        postbutton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                EditText editText = (EditText) findViewById(R.id.editText);
                final String tootText = editText.getText().toString(); //本文

                Spinner spinner = (Spinner) findViewById(R.id.spinner);
                final String tootvsblty = spinner.getSelectedItem().toString(); //公開設定

                new AsyncTask<String, Void, String>() {

                    @Override
                    protected String doInBackground(String... params) {
                        final StringBuffer result = new StringBuffer();
                        Uri.Builder uriBuilder = new Uri.Builder();
                        uriBuilder.scheme("https");
                        uriBuilder.authority(HOST_MASTODON);
                        uriBuilder.path("/api/v1/statuses");
                        final String uriStr = uriBuilder.build().toString();

                        try {
                            URL url = new URL(uriStr);
                            HttpURLConnection con = null;
                            con = (HttpURLConnection) url.openConnection();
                            con.setRequestMethod("POST");
                            con.addRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
                            con.addRequestProperty("Content-Type", "application/json; charset=UTF-8");
                            //データの形式を指定
                            con.setDoOutput(true); //defaultではfalseなので必ずtrueにすること
                            con.connect();


                            final String input =
                                    "{\"status\":\"" +tootText+ "\",\"visibility\":\"" +tootvsblty+ "\"}";
                            //無理やりJSONデータ作成
                            OutputStream os = con.getOutputStream();
                            os.write(input.getBytes()); //文字列をバイトに変換
                            os.flush(); //データ送信
                            os.close();

                            final int status = con.getResponseCode(); //レスポンスコードを受け取る
                            if(status == HttpURLConnection.HTTP_OK) { //通信成功
                                final InputStream in = con.getInputStream();
                                String encoding = con.getContentEncoding();
                                if(null == encoding){
                                    encoding = "UTF-8";
                                }
                                final InputStreamReader inReader = new InputStreamReader(in, encoding);
                                final BufferedReader bufReader = new BufferedReader(inReader);
                                String line = null;
                                while((line = bufReader.readLine()) != null) {
                                    result.append(line);
                                }
                                bufReader.close();
                                inReader.close();
                                in.close();
                            }

                            else { //通信失敗
                                System.out.println(status);
                            }
                        }

                        catch(Exception e) { //エラーの時
                            Log.e("button", e.getMessage());
                        }

                        System.out.println("result=" + result.toString());
                        return result.toString();
                    }

                    @Override
                    protected void onPostExecute(String result) {
                        TextView textView = (TextView) findViewById(R.id.textView);
                        textView.setText(result); //返ってきた情報をテキストビューで一応見れるように
                    }
                }.execute();
                editText.setText(""); //投稿した後に入力フォームを空にする
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ここから通信
                //EditText editText = (EditText) findViewById(R.id.editText);
                final String tootText = "助けてください！！！！";//editText.getText().toString(); //本文

                Spinner spinner = (Spinner) findViewById(R.id.spinner);
                final String tootvsblty = spinner.getSelectedItem().toString(); //公開設定

                new AsyncTask<String, Void, String>() {

                    @Override
                    protected String doInBackground(String... params) {
                        final StringBuffer result = new StringBuffer();
                        Uri.Builder uriBuilder = new Uri.Builder();
                        uriBuilder.scheme("https");
                        uriBuilder.authority(HOST_MASTODON);
                        uriBuilder.path("/api/v1/statuses");
                        final String uriStr = uriBuilder.build().toString();

                        try {
                            URL url = new URL(uriStr);
                            HttpURLConnection con = null;
                            con = (HttpURLConnection) url.openConnection();
                            con.setRequestMethod("POST");
                            con.addRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
                            con.addRequestProperty("Content-Type", "application/json; charset=UTF-8");
                            //データの形式を指定
                            con.setDoOutput(true); //defaultではfalseなので必ずtrueにすること
                            con.connect();


                            final String input =
                                    "{\"status\":\"" + tootText + "\",\"visibility\":\"" + tootvsblty + "\"}";
                            //無理やりJSONデータ作成
                            OutputStream os = con.getOutputStream();
                            os.write(input.getBytes()); //文字列をバイトに変換
                            os.flush(); //データ送信
                            os.close();

                            final int status = con.getResponseCode(); //レスポンスコードを受け取る
                            if (status == HttpURLConnection.HTTP_OK) { //通信成功
                                final InputStream in = con.getInputStream();
                                String encoding = con.getContentEncoding();
                                if (null == encoding) {
                                    encoding = "UTF-8";
                                }
                                final InputStreamReader inReader = new InputStreamReader(in, encoding);
                                final BufferedReader bufReader = new BufferedReader(inReader);
                                String line = null;
                                while ((line = bufReader.readLine()) != null) {
                                    result.append(line);
                                }
                                bufReader.close();
                                inReader.close();
                                in.close();
                            } else { //通信失敗
                                System.out.println(status);
                            }
                        } catch (Exception e) { //エラーの時
                            //Log.e("button", e.getMessage());
                        }

                        System.out.println("result=" + result.toString());
                        return result.toString();
                    }
                }.execute();

                //通信ここまで
                Snackbar.make(view, "あなたはSOSを発信しました", Snackbar.LENGTH_LONG)
                        .setAction("取り消す", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
