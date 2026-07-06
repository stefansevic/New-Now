# Priprema 5 (priprema za K2):

Spaja vise tema: proximity senzor, kamera (cuvanje slike u cache direktorijum), Retrofit (GET zahtev), Room baza, lokacija (lat/long), rad sa CheckBox i ImageButton elementima.


## Preporuceni redosled rada
1. Projekat Kolokvijum2 (glavna aktivnost MainActivity), pa biblioteke u build.gradle i Sync.
2. AndroidManifest (INTERNET i dozvole za lokaciju).
3. activity_main.xml (dva checkboxa, image button, dva textview-a).
4. Room baza: Continent, ContinentDao, AppDatabase.
5. ApiService (Retrofit GET).
6. MainActivity: senzor, kamera, mreza, baza, lokacija.


## KORAK 1: build.gradle (Module: app)

Otvori build.gradle koji ima dependencies blok. Unutar dependencies dodaj ove linije:

    implementation 'com.google.code.gson:gson:2.8.7'
    implementation 'com.squareup.retrofit2:retrofit:2.3.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.3.0'
    implementation 'com.google.android.gms:play-services-location:21.0.1'
    implementation 'androidx.room:room-runtime:2.4.2'
    annotationProcessor 'androidx.room:room-compiler:2.4.2'

Klikni Sync Now.

Napomena: room-runtime i room-compiler MORAJU biti iste verzije. Ako Android Studio negde ubaci liniju za androidx.room3 (novi eksperimentalni Room), izbaci je, jer se mesa sa ovim i onda ne kompajlira.


## KORAK 2: AndroidManifest.xml

Iznad application taga:

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

Napomena: CAMERA dozvolu NE dodajemo. Koristimo sistemsku kameru (ACTION_IMAGE_CAPTURE) koja je ne trazi. Ako je deklarises a ne trazis je u kodu, aplikacija puca na kameri.


## KORAK 3: activity_main.xml (zameni ceo sadrzaj)

Dva textview-a, image button i dva checkboxa:

    <?xml version="1.0" encoding="utf-8"?>
    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:padding="16dp">

        <TextView
            android:id="@+id/tvSensorValue"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Proximity:"
            android:textSize="16sp" />

        <TextView
            android:id="@+id/tvDisplayResult"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Rezultat:"
            android:textSize="16sp"
            android:paddingTop="8dp" />

        <ImageButton
            android:id="@+id/ibCamera"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="12dp"
            android:src="@android:drawable/ic_menu_camera" />

        <CheckBox
            android:id="@+id/cbFetch"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="12dp"
            android:text="Dobavi kontinente" />

        <CheckBox
            android:id="@+id/cbToggle"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="12dp"
            android:text="Prikazi / lokacija" />

    </LinearLayout>


## KORAK 4: Room baza (tri klase)

### Continent (model i entitet)

Desni klik na paket, New, Java Class, ime Continent. Polja se zovu isto kao kljucevi u JSON-u (bitno za Gson):

    package com.example.kolokvijum2;

    import androidx.room.Entity;
    import androidx.room.PrimaryKey;

    @Entity(tableName = "continents")
    public class Continent {
        @PrimaryKey(autoGenerate = true)
        public int id;
        public String name;
        public long population;
        public int countries;

        public Continent(String name, long population, int countries) {
            this.name = name;
            this.population = population;
            this.countries = countries;
        }
    }

Vazno: svi importi su iz androidx.room (NE androidx.room3). Ovo je bila glavna greska zbog koje se ranije nije kompajliralo.

### ContinentDao (operacije nad bazom)

New, Java Class, tip Interface, ime ContinentDao:

    package com.example.kolokvijum2;

    import androidx.room.Dao;
    import androidx.room.Insert;
    import androidx.room.Query;

    import java.util.List;

    @Dao
    public interface ContinentDao {
        @Insert
        void insertAll(List<Continent> continents);

        @Query("SELECT * FROM continents")
        List<Continent> getAll();

        @Query("DELETE FROM continents")
        void deleteAll();
    }

### AppDatabase (sama baza)

New, Java Class, ime AppDatabase:

    package com.example.kolokvijum2;

    import androidx.room.Database;
    import androidx.room.RoomDatabase;

    @Database(entities = {Continent.class}, version = 1)
    public abstract class AppDatabase extends RoomDatabase {
        public abstract ContinentDao continentDao();
    }


## KORAK 5: ApiService (Retrofit GET)

New, Java Class, tip Interface, ime ApiService. U @GET ide SAMO putanja, ne ceo URL:

    package com.example.kolokvijum2;

    import java.util.List;

    import retrofit2.Call;
    import retrofit2.http.GET;

    public interface ApiService {
        @GET("continents")
        Call<List<Continent>> getContinents();
    }


## KORAK 6: MainActivity.java (cela klasa)

Ovde se sve spaja. Svaki blok je obelezen kom zadatku pripada:

    package com.example.kolokvijum2;

    import android.Manifest;
    import android.content.Intent;
    import android.content.pm.PackageManager;
    import android.graphics.Bitmap;
    import android.hardware.Sensor;
    import android.hardware.SensorEvent;
    import android.hardware.SensorEventListener;
    import android.hardware.SensorManager;
    import android.os.Bundle;
    import android.provider.MediaStore;
    import android.widget.CheckBox;
    import android.widget.ImageButton;
    import android.widget.TextView;
    import android.widget.Toast;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.app.ActivityCompat;
    import androidx.core.content.ContextCompat;
    import androidx.room.Room;
    import com.google.android.gms.location.FusedLocationProviderClient;
    import com.google.android.gms.location.LocationServices;

    import java.io.File;
    import java.io.FileOutputStream;
    import java.io.IOException;
    import java.util.ArrayList;
    import java.util.List;

    import retrofit2.Call;
    import retrofit2.Callback;
    import retrofit2.Response;
    import retrofit2.Retrofit;
    import retrofit2.converter.gson.GsonConverterFactory;

    public class MainActivity extends AppCompatActivity implements SensorEventListener {

        private CheckBox cbFetch, cbToggle;
        private ImageButton ibCamera;
        private TextView tvSensorValue, tvDisplayResult;

        private SensorManager sensorManager;
        private Sensor proximitySensor;
        private float currentProximityValue;

        private FusedLocationProviderClient fusedLocationClient;

        private AppDatabase db;
        private static final int REQUEST_IMAGE_CAPTURE = 1;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            cbFetch = findViewById(R.id.cbFetch);
            cbToggle = findViewById(R.id.cbToggle);
            ibCamera = findViewById(R.id.ibCamera);
            tvSensorValue = findViewById(R.id.tvSensorValue);
            tvDisplayResult = findViewById(R.id.tvDisplayResult);

            // Room baza
            db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "kolokvijum_db")
                    .allowMainThreadQueries().build();

            // ZADATAK 3: proximity senzor
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

            // ZADATAK 7: klijent za lokaciju + trazenje dozvole
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            // ZADATAK 4: klik na image button pokrece kameru
            ibCamera.setOnClickListener(v -> {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            });

            // ZADATAK 6: prvi checkbox dobavlja i filtrira kontinente
            cbFetch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    fetchDataFromApi();
                }
            });

            // ZADATAK 7: drugi checkbox (cekiran vs odcekiran)
            cbToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateSecondTextView();
            });
        }

        // ZADATAK 6: mreza + filter + upis u bazu
        private void fetchDataFromApi() {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://dummy-json.free.beeceptor.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            ApiService api = retrofit.create(ApiService.class);
            api.getContinents().enqueue(new Callback<List<Continent>>() {
                @Override
                public void onResponse(Call<List<Continent>> call, Response<List<Continent>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Continent> filtered = new ArrayList<>();
                        for (Continent c : response.body()) {
                            if (c.population > 10000) filtered.add(c);
                        }
                        db.continentDao().deleteAll();
                        db.continentDao().insertAll(filtered);
                        Toast.makeText(MainActivity.this, "Sacuvano " + filtered.size() + " kontinenata", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<List<Continent>> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Greska: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // ZADATAK 4: kamera, cuvanje u cache direktorijum, putanja u toast
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                try {
                    File cacheDir = getCacheDir();
                    File tempFile = File.createTempFile("slika_", ".jpg", cacheDir);
                    FileOutputStream out = new FileOutputStream(tempFile);
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                    Toast.makeText(this, "Putanja: " + tempFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // ZADATAK 3: ocitavanje proximity senzora + prag + toast
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                currentProximityValue = event.values[0];
                tvSensorValue.setText("Proximity: " + currentProximityValue);

                float prag = 5;
                if (currentProximityValue < prag) {
                    Toast.makeText(this, "Blizu!", Toast.LENGTH_SHORT).show();
                }
            }
        }

        // ZADATAK 7: cekiran -> treci kontinent iz baze; odcekiran -> lokacija
        private void updateSecondTextView() {
            if (cbToggle.isChecked()) {
                List<Continent> lista = db.continentDao().getAll();
                if (lista.size() >= 3) {
                    tvDisplayResult.setText("Drzava u 3. kontinentu: " + lista.get(2).countries);
                } else {
                    tvDisplayResult.setText("Nema dovoljno podataka u bazi.");
                }
            } else {
                prikaziLokaciju();
            }
        }

        // ZADATAK 7: lokacija (lat/long)
        private void prikaziLokaciju() {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            tvDisplayResult.setText("Lat: " + location.getLatitude()
                                    + " Long: " + location.getLongitude());
                        } else {
                            tvDisplayResult.setText("Lokacija nije dostupna");
                        }
                    });
        }

        // senzor se ukljucuje kad je app aktivna, iskljucuje u pozadini (baterija)
        @Override
        protected void onResume() {
            super.onResume();
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        @Override
        protected void onPause() {
            super.onPause();
            sensorManager.unregisterListener(this);
        }

        // deo SensorEventListener interfejsa, mora da postoji ali nam ne treba
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    }


## Kako sve radi zajedno (rezime po zadacima)

Zadatak 3 (proximity): u onResume registrujemo senzor, u onPause ga odjavljujemo. U onSensorChanged citamo event.values[0], upisemo u prvi TextView, i ako je vrednost ispod praga (5) javimo Toast "Blizu!".

Zadatak 4 (kamera u cache): klik na image button pokrece sistemsku kameru. Kad se slika potvrdi, u onActivityResult uzmemo sliku, napravimo fajl u cache direktorijumu (getCacheDir), sacuvamo sliku i ispisemo putanju u Toast.

Zadatak 5 (model i Retrofit): Continent je model za JSON i entitet za bazu. ApiService definise GET zahtev getContinents(). ClientUtils logika je u fetchDataFromApi (Retrofit.Builder).

Zadatak 6 (prvi checkbox): fetchDataFromApi povuce sve kontinente sa servera, u petlji zadrzi samo one sa populacijom preko praga, obrise staru bazu (deleteAll da nema duplikata) i upise filtrirane (insertAll).

Zadatak 7 (drugi checkbox): kad je cekiran, procita sve iz baze i prikaze broj drzava treceg kontinenta (lista.get(2), jer se broji od nule). Kad je odcekiran, prikaze lokaciju (lat/long preko FusedLocationProviderClient).


## Ceste greske (nauceno na kolokvijumu)

Room importi: SVI moraju iz androidx.room. Ako je makar jedan iz androidx.room3 (Android Studio to ume sam da ubaci pri auto-importu), Room ne prepoznaje entitet i ne kompajlira se. Isto vazi za suvisnu liniju implementation libs.room3.common.jvm u build.gradle, izbaci je.

Retrofit URL: baseUrl mora da se zavrsava sa /, a u @GET ide samo putanja (npr. "continents"), ne ceo link. Link koji dobijes tipa app.beeceptor.com/mock-server/ime je stranica za podesavanje, ne endpoint. Pravi endpoint je https://ime.free.beeceptor.com/. Proveri tako sto ga otvoris u browseru: ako vidis JSON, tacan je.

Ime polja u modelu mora biti isto kao kljuc u JSON-u (npr. countries). Ako se razlikuje, Gson ga ne mapira i vrednost je 0.

Promena strukture baze: ako preimenujes ili dodas polje u entitetu, a baza vec postoji, app puca (schema mismatch). Resenje: deinstaliraj app sa emulatora pa pokreni ponovo (nova baza se napravi).

Kamera: sistemska kamera (ACTION_IMAGE_CAPTURE) ne trazi CAMERA dozvolu. Ako je deklarises u manifestu a ne trazis je u kodu, app puca. Najlakse je ne deklarisati je.

Lokacija: treba dozvola i u manifestu i u kodu (runtime). Na emulatoru getLastLocation vrati null dok ne postavis lokaciju: tri tacke pored emulatora, Extended controls, Location, unesi koordinate, Set Location.

Prag populacije: u kodu je 10000. Ako zadatak trazi drugi broj (npr. 1000), promeni samo taj broj u if uslovu.

Napomena: na vezbama je radjen cist SQLite (SQLiteOpenHelper, ContentProvider). Ovde je koriscen Room. Oba su ispravna ako se kompajliraju, ali ako zelis da budes na sigurno kao sa vezbi, moze i SQLite verzija.
