package com.queen.rxjavaretrofitdemo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import com.queen.rxjavaretrofitdemo.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import http.MovieService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.click_me_BN)
    Button clickMeBN;
    @Bind(R.id.result_TV)
    TextView resultTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.click_me_BN)
    public void onClick() {
        getMovie();
    }

    //进行网络请求
    private void getMovie(){
        String baseUrl = "https://api.douban.com/v2/movie/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .build();

        MovieService movieService = retrofit.create(MovieService.class);
        Call<String> call = movieService.getTopMovie(0, 10);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                resultTV.setText(response.body());
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                resultTV.setText(t.getMessage());
            }
        });

    }
}
