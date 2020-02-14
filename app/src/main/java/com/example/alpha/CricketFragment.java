package com.example.alpha;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static com.example.alpha.string_functions.isStarted;
import static com.example.alpha.string_functions.split_Score;

public class CricketFragment extends Fragment {
    ProgressBar progressBar;
    ArrayList<Cricket_live_scores> cls=new ArrayList<>();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.main_page,container,false);
        progressBar=view.findViewById(R.id.progress);
        data_fetcher("https://cricapi.com/api/cricket/?apikey=Bd8wF5XUVGRFmScoOnpJ5aEh93d2",progressBar);

        return view;
    }
    RecyclerView recyclerView;

    TreeMap<Integer, String[]> map = new TreeMap<>();

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        LinearLayoutManager RecyclerViewLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(RecyclerViewLayoutManager);
        LinearLayoutManager HorizontalLayout = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(HorizontalLayout);
        SnapHelper linearSnapHelper = new PagerSnapHelper();
        linearSnapHelper.attachToRecyclerView(recyclerView);

    }

    public static String search(String txt, String[] pat) {
        for (String x : pat) {
            int M = x.length();
            int N = txt.length();
            for (int i = 0; i <= N - M; i++) {
                int j;
                for (j = 0; j < M; j++)
                    if (txt.charAt(i + j) != x.charAt(j))
                        break;

                if (j == M)
                    return x;
            }
        }
        return null;
    }



    private void data_fetcher(final String url, final View view) {

        class data_fetch extends AsyncTask<Void, Void, String> {
            String s;
            String[] unique_id = new String[100];
            String[] mydata = new String[100];
            JSONArray live_score_array;

            protected void onPreExecute() {
                super.onPreExecute();
                view.setVisibility(View.VISIBLE);
            }

            @Override
            protected String doInBackground(Void... voids) {
                if (cls.size() == 0) {
                    try {
                        URL url_score = new URL(url);
                        HttpURLConnection urlConnection = (HttpURLConnection) url_score.openConnection();
                        System.out.println(cls.size() + "=size");
                        try {
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                            StringBuilder stringBuilder = new StringBuilder();
                            String line;
                            while ((line = bufferedReader.readLine()) != null) {
                                stringBuilder.append(line).append("\n");
                            }
                            bufferedReader.close();
//
                            s = stringBuilder.toString();
                        } finally {
                            urlConnection.disconnect();
                        }
                    } catch (Exception e) {
                        Log.e("ERROR", e.getMessage(), e);
                        return null;
                    }
                    {


                        String[] check = {"India", "Australia", "England", "Pakistan", "South Africa", "New Zealand", "West Indies",
                                "Sri Lanka", "Bangladesh", "Zimbabve", "Afghanistan", "Ireland"};
                        Map<String, Integer> preference_Order = new HashMap<>();
                        preference_Order.put("India", 1);
                        preference_Order.put("Australia", 2);
                        preference_Order.put("England", 3);
                        preference_Order.put("Pakistan", 4);
                        preference_Order.put("New Zealand", 5);
                        preference_Order.put("South Africa", 6);
                        preference_Order.put("West Indies", 7);
                        preference_Order.put("Sri Lanka", 8);
                        preference_Order.put("Bangladesh", 9);
                        preference_Order.put("Zimbabwe", 10);
                        preference_Order.put("Afghanistan", 11);
                        preference_Order.put("Ireland", 12);


                        try {
                            JSONObject reader = new JSONObject(s);
                            live_score_array = reader.getJSONArray("data");


                            for (int i = 0; i < live_score_array.length(); i++) {
                                JSONObject jsonObject = live_score_array.getJSONObject(i);
                                mydata[i] = jsonObject.getString("description");
                                unique_id[i] = jsonObject.getString("unique_id");


                            }

                            for (int i = 0; i < live_score_array.length(); i++) {
                                String api = "https://cricapi.com/api/cricketScore/?apikey=Bd8wF5XUVGRFmScoOnpJ5aEh93d2&unique_id=" + unique_id[i];
                                URL url_new = new URL(api);
                                HttpURLConnection httpURLConnection = (HttpURLConnection) url_new.openConnection();
                                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                                String details;
                                StringBuilder stringBuilder = new StringBuilder();
                                while ((details = bufferedReader.readLine()) != null) {
                                    stringBuilder.append(details);
                                }
                                JSONObject object = new JSONObject(stringBuilder.toString());
                                String[] teams = split_Score(mydata[i]);
                                String[] teams_final = new String[3];
                                if (search(teams[0], check) != null && search(teams[1], check) != null) {
                                    teams[0] = teams[0].replace("&amp;", "&");
                                    teams[1] = teams[1].replace("&amp;", "&");
                                    teams_final[0] = teams[0];
                                    teams_final[1] = teams[1];
                                    teams_final[2] = object.getString("stat");
                                    String t1 = search(teams[0], check);
                                    String t2 = search(teams[1], check);
                                    map.put(Math.min(preference_Order.get(t1), preference_Order.get(t2)), teams_final);
                                }

                            }
                            int count = 0;
                            for (Map.Entry m : map.entrySet()) {
                                if (count >= 5)
                                    break;
                                String[] arr = (String[]) m.getValue();
//                            if (isStarted(arr) == 1) {
                                cls.add(new Cricket_live_scores(arr[0], arr[1], arr[2]));
                                count++;
//                            }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
                    return null;
                }

            @Override
            protected void onPostExecute(String s) {
                Custom_adapter myadapter = new Custom_adapter(cls);
                recyclerView.setAdapter(myadapter);
                view.setVisibility(View.GONE);
            }
        }
        data_fetch data_fetch = new data_fetch();
        data_fetch.execute();
    }

}
