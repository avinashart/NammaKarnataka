package smartAmigos.com.nammakarnataka;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import smartAmigos.com.nammakarnataka.adapter.generic_adapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class trekkingFragment extends Fragment {

    private List<generic_adapter> trekking_adapterList = new ArrayList<>();


    static SimpleDraweeView draweeView;
    private InterstitialAd interstitial;
    View view;
    Context context;
    MaterialRefreshLayout materialRefreshLayout;
    static int serverVersion, localVersion;
    ListView list;
    TextView t;

    public trekkingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_trekking, container, false);
        context = getActivity().getApplicationContext();

        t = (TextView) view.findViewById(R.id.pp1);
        Typeface myFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Kaushan.otf");
        t.setTypeface(myFont);

        materialRefreshLayout = (MaterialRefreshLayout) view.findViewById(R.id.refresh);
        list = (ListView) view.findViewById(R.id.trekkingList);

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        //Call ads
        AdRequest adRequest = new AdRequest.Builder().build();

        // Prepare the Interstitial Ad
        interstitial = new InterstitialAd(context);
        // Insert the Ad Unit ID
        interstitial.setAdUnitId(getString(R.string.admob_interstitial_id));

        interstitial.loadAd(adRequest);
        // Prepare an Interstitial Ad Listener
        interstitial.setAdListener(new AdListener() {
            public void onAdLoaded() {
                // Call displayInterstitial() function
                if (interstitial.isLoaded()&&Math.random()>0.7) {
                    interstitial.show();
                }
            }
        });
        //Finish calling ads

        Fresco.initialize(getActivity());
        awesomeAlgorithm();

        if (!loadJsonFile()) {
            if (isNetworkConnected()) {
                Toast.makeText(getActivity(), "please wait for a moment!", Toast.LENGTH_SHORT).show();
                SharedPreferences preferences = getActivity().getSharedPreferences("trekking_version", Context.MODE_PRIVATE);
                localVersion = preferences.getInt("version", 0);
                new TrekkingVersion().execute("http://nammakarnataka.net23.net/trekking/trekking_version.json");
            } else {
                Toast.makeText(getActivity(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
            }
        }



        materialRefreshLayout.setMaterialRefreshListener(new MaterialRefreshListener() {
            @Override
            public void onRefresh(final MaterialRefreshLayout materialRefreshLayout) {

                if (isNetworkConnected()) {
                    SharedPreferences preferences = getActivity().getSharedPreferences("trekking_version", Context.MODE_PRIVATE);
                    localVersion = preferences.getInt("version", 0);
                    new TrekkingVersion().execute("http://nammakarnataka.net23.net/trekking/trekking_version.json");
                } else {
                    Toast.makeText(getActivity(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
                    materialRefreshLayout.finishRefresh();
                }
            }

        });
        return view;
    }



    private void awesomeAlgorithm() {

        SharedPreferences preferences = getActivity().getSharedPreferences("golmal", Context.MODE_PRIVATE);
        int b = preferences.getInt("trekking",0);
        if(b == 4){
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("trekking",1);
            editor.commit();

            if(isNetworkConnected())
            new TrekkingVersion().execute("http://nammakarnataka.net23.net/trekking/trekking_version.json");

        }else{
            if(b%3 == 0){
                Toast.makeText(getActivity(), "Swipe down to refresh Contents!", Toast.LENGTH_SHORT).show();
            }
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("trekking",++b);
            editor.commit();
        }
    }



    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }


    private void saveJsonFile(String data) {
        FileOutputStream stream = null;
        try {
            File path = new File("/data/data/smartAmigos.com.nammakarnataka/trekking.json");
            stream = new FileOutputStream(path);
            stream.write(data.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stream != null)
                    stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private boolean loadJsonFile() {
        trekking_adapterList.clear();
        String ret = null;
        BufferedReader reader = null;
        File file = new File("/data/data/smartAmigos.com.nammakarnataka/trekking.json");
        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                reader = new BufferedReader(new InputStreamReader(fis));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                ret = builder.toString();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null)
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }


            try {
                JSONObject parent = new JSONObject(ret);
                JSONArray items = parent.getJSONArray("list");
                for (int i = 0; i < items.length(); i++) {
                    JSONObject child = items.getJSONObject(i);
                    JSONArray images = child.getJSONArray("image");
                    String[] imagesArray = new String[25];
                    for (int j = 0; j < images.length(); j++) {
                        imagesArray[j] = images.getString(j);
                    }
                    trekking_adapterList.add(new generic_adapter(imagesArray, child.getString("name"), child.getString("description"), child.getString("district"), child.getString("bestSeason"), child.getString("additionalInformation"), child.getString("nearByPlaces"), child.getDouble("latitude"), child.getDouble("longitude")));

                }
                displayList(items);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }

        return false;
    }


    public class TrekkingVersion extends AsyncTask<String, String, String> {
        HttpURLConnection connection;
        BufferedReader reader;

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                return builder.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject parent = new JSONObject(s);
                JSONObject news_version = parent.getJSONObject("trekking_version");

                serverVersion = news_version.getInt("version");


            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (localVersion != serverVersion) {
                new TrekkingFile().execute("http://nammakarnataka.net23.net/trekking/trekking.json");
            } else {
                Toast.makeText(getActivity(), "Trekking List is up to date!", Toast.LENGTH_SHORT).show();
                materialRefreshLayout.finishRefresh();
            }

        }
    }



    private void displayList(final JSONArray par) {
        ArrayAdapter<generic_adapter> adapter = new myTrekkingListAdapterClass();

        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                try {
                    JSONObject child = par.getJSONObject(position);
                    Fragment fragment = new placeDisplayFragment(child, "Trekking");
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.content_main, fragment);
                    ft.addToBackStack(null);
                    ft.commit();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public class myTrekkingListAdapterClass extends ArrayAdapter<generic_adapter> {

        myTrekkingListAdapterClass() {
            super(context, R.layout.hillstations_item, trekking_adapterList);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                itemView = inflater.inflate(R.layout.hillstations_item, parent, false);

            }
            generic_adapter current = trekking_adapterList.get(position);

            //Code to download image from url and paste.
            Uri uri = Uri.parse(current.getImage()[0]);
            draweeView = (SimpleDraweeView) itemView.findViewById(R.id.item_hillstationsImage);
            draweeView.setImageURI(uri);
            //Code ends here.
            TextView t_name = (TextView) itemView.findViewById(R.id.item_hillstationsTitle);
            t_name.setText(current.getTitle());

            TextView t_dist = (TextView) itemView.findViewById(R.id.item_hillstationsDistrict);
            t_dist.setText(current.getDistrict());

            return itemView;
        }
    }




    public class TrekkingFile extends AsyncTask<String, String, String> {

        HttpURLConnection connection;
        BufferedReader reader;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                String str = builder.toString();
                saveJsonFile(str);
                return str;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            trekking_adapterList.clear();

            SharedPreferences preferences = getActivity().getSharedPreferences("trekking_version", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("version", serverVersion);
            editor.apply();

            Toast.makeText(getActivity(), "Trekking List updated!", Toast.LENGTH_SHORT).show();
            try {
                JSONObject parent = new JSONObject(s);
                JSONArray items = parent.getJSONArray("list");
                for (int i=0;i<items.length();i++){
                    JSONObject child = items.getJSONObject(i);
                    JSONArray images = child.getJSONArray("image");
                    String [] imagesArray = new String[25];
                    for(int j=0;j<images.length();j++){
                        imagesArray[j] = images.getString(j);
                    }
                    trekking_adapterList.add(new generic_adapter(imagesArray, child.getString("name"), child.getString("description"), child.getString("district"), child.getString("bestSeason"),child.getString("additionalInformation"),child.getString("nearByPlaces"),child.getDouble("latitude"), child.getDouble("longitude")));

                }
                materialRefreshLayout.finishRefresh();
                displayList(items);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }



}