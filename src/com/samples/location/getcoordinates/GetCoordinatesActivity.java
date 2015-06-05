package com.samples.location.getcoordinates;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.location.*;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GetCoordinatesActivity extends Activity implements View.OnClickListener {  //этот класс слушает onClick

	private LocationManager manager;        //доступ к услугам местоположения
	private TextView text;
    private EditText phone;
    private EditText eText;
    private SmsManager smsmanager;
    private PendingIntent intent;
    Location loc;                           //объект местоположения
    String comment;

	private LocationListener locListener = new LocationListener() {

		public void onLocationChanged(Location argLocation) {   //при изменении координат переписывает их
			printLocation(argLocation);
		}

		@Override
		public void onProviderDisabled(String arg0) {       //если выключен GPS,
            findViewById(R.id.button).setEnabled(false);    //т.е. провайдер не доступен
            findViewById(R.id.editText).setEnabled(false);
            findViewById(R.id.buttonSMS).setEnabled(false);
            findViewById(R.id.editPhone).setEnabled(false);
			printLocation(null);
		}

		@Override
		public void onProviderEnabled(String arg0) {        //если включен
            manager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            //регистрация слушателя для постоянного прослушивания изменений
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
            loc = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            printLocation(loc);
            findViewById(R.id.button).setEnabled(true);
            findViewById(R.id.editText).setEnabled(true);
            findViewById(R.id.buttonSMS).setEnabled(true);
            findViewById(R.id.editPhone).setEnabled(true);
        }

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {

        }
	};

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		text = (TextView)findViewById(R.id.text);
		
		manager = (LocationManager)getSystemService(
				  Context.LOCATION_SERVICE);
        //регистрация созданного ранее объекта LocationListener
		manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
		loc = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        printLocation(loc);

        final Button button1 = (Button)findViewById(R.id.button);
        button1.setOnClickListener(this);   //обработчик - текущий объект

        GpsStatus.Listener lGPS = new GpsStatus.Listener() {
            public void onGpsStatusChanged(int event) {
                if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
                    GpsStatus status = manager.getGpsStatus(null);
                    Iterable<GpsSatellite> sats = status.getSatellites();
                    TextView tv = (TextView)findViewById(R.id.textView2);
                    Integer quan=0;
                    Iterator it=sats.iterator();
                    while (it.hasNext()) {
                        it.next();
                        quan++;
                        tv.setText("Количество спутников: "+quan.toString());
                    }
                }
            }
        };
        manager.addGpsStatusListener(lGPS);
	}

    private void printLocation(Location loc) {
		if (loc != null)
		{
		    text.setText("Широта:\t" + loc.getLatitude() + "\nДолгота:\t" + loc.getLongitude() +
	                "\nСкорость: " + loc.getSpeed()+ "м/с (" +
                    loc.getSpeed()*3.6 + " км/ч)");
		}
		else {
			text.setText("Координаты недоступны! Включите GPS-приемник устройства");
		}
	}

    @Override       //отправка координат по нажатию кнопки
    public void onClick(View v) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(
                "http://212.192.134.20/u2014-35-2-korobkov-denis.kubsu-dev.ru/script.php"
        );
        try {
            // Add data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("Longitude", loc.getLongitude() + ""));   //как строка
            nameValuePairs.add(new BasicNameValuePair("Latitude", loc.getLatitude() + ""));
            eText=(EditText)findViewById(R.id.editText);
            nameValuePairs.add(new BasicNameValuePair("Comment", eText.getText().toString() + ""));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "utf-8"));
            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            //создание всплывающего сообщения
            Toast.makeText(this,"Данные успешно отправлены!",Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            //все ошибки, происходящие во время выполнения приложения
            e.printStackTrace();    //печать искл-я в стандартный вывод ошибок
            Toast.makeText(this,"Не удалось связаться с сервером. Проверьте подключение к сети",Toast.LENGTH_LONG).show();
        }
    }

    public void sendSMS(View v){
        //добавление всплывающего окна
        smsmanager=SmsManager.getDefault();     //обязательно нужно объявить!!!
        phone = (EditText)findViewById(R.id.editPhone);
        String dest=phone.getText().toString(),
               sms="Мое местоположение: "+loc.getLatitude()+" "+loc.getLongitude();
        try {
            if (PhoneNumberUtils.isWellFormedSmsAddress(dest)) {
                intent = PendingIntent.getActivity(this, 0, new Intent(this, GetCoordinatesActivity.class), 0);
                smsmanager.sendTextMessage(dest, null, sms, intent, null);
                Toast.makeText(GetCoordinatesActivity.this, "SMS sent", Toast.LENGTH_LONG).show();
            }
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(GetCoordinatesActivity.this);
                builder.setTitle("Ошибка!")
                        .setMessage("Что-то не так с номером получателя...")
                        .setIcon(R.drawable.warn)
                        .setCancelable(false)
                        .setNegativeButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
        catch (Exception e) {
            Toast.makeText(GetCoordinatesActivity.this, e.toString(),Toast.LENGTH_LONG).show();
        }
    }

    public void about_info(MenuItem item){
        AlertDialog.Builder builder = new AlertDialog.Builder(GetCoordinatesActivity.this);
        builder.setTitle("О программе")
                .setMessage("Первая программа, написанная для Android.\nОтдельное спасибо Моисееву Денису " +
                        "за неоценимую помощь в написании.\n© Денис Коробков, 2014")
                .setIcon(R.drawable.info)
                .setCancelable(false)
                .setNegativeButton("Закрыть",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {         //появление меню у активити
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
}