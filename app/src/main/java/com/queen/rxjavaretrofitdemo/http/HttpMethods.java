package com.queen.rxjavaretrofitdemo.http;

import com.queen.rxjavaretrofitdemo.entity.HttpResult;
import com.queen.rxjavaretrofitdemo.entity.Subject;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.HTTP;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by liukun on 16/3/9.
 */
public class HttpMethods {

    public static final String BASE_URL = "https://api.douban.com/v2/movie/";

    private static final int DEFAULT_TIMEOUT = 5;

    private Retrofit retrofit;
    private MovieService movieService;

    //构造方法私有
    private HttpMethods() {
        //手动创建一个OkHttpClient并设置超时时间
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

        retrofit = new Retrofit.Builder()
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();

        movieService = retrofit.create(MovieService.class);
    }

    //在访问HttpMethods时创建单例
    private static class SingletonHolder{
        private static final HttpMethods INSTANCE = new HttpMethods();
    }

    //获取单例
    public static HttpMethods getInstance(){
        return SingletonHolder.INSTANCE;
    }

    /**
     * 用于获取豆瓣电影Top250的数据
     * @param subscriber  由调用者传过来的观察者对象
     * @param start 起始位置
     * @param count 获取长度
     */
    public void getTopMovie(Subscriber<List<Subject>> subscriber, int start, final int count){
//        movieService.getTopMovie(start, count)
//                .flatMap(new Func1<HttpResult<List<Subject>>, Observable<List<Subject>>>() {
//                    @Override
//                    public Observable<List<Subject>> call(HttpResult<List<Subject>> httpResult) {
//                        return flatResult(httpResult);
//                    }
//                })
//                .subscribeOn(Schedulers.io())
//                .unsubscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(subscriber);

        movieService.getTopMovie(start, count)
                .compose(new HttpObservableTransformer<List<Subject>>())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    private class HttpObservableTransformer<T> implements Observable.Transformer<HttpResult<T>, T> {

        @Override
        public Observable<T> call(Observable<HttpResult<T>> httpObservable) {

            return httpObservable.map(new Func1<HttpResult<T>, T>() {
                @Override
                public T call(HttpResult<T> httpResult) {
                    if (httpResult.getCount() == 0) {
                        throw new ApiException(100);
                    }
                    return httpResult.getSubjects();
                }
            })
            .subscribeOn(Schedulers.io())
            .unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
        }
    }

    /**
     * 用来统一处理Http的ResultCode
     * @param result   Http请求返回的数据，用过HttpResult进行了封装
     * @param <T>   Subscriber真正需要的数据类型，也就是Data部分的数据类型
     * @return
     */
    static <T> Observable<T> flatResult(final HttpResult<T> result) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {

                if (result.getCount() == 0) {
                    subscriber.onError(new ApiException(ApiException.USER_NOT_EXIST));
                } else{
                    subscriber.onNext(result.getSubjects());
                }

                subscriber.onCompleted();
            }
        });
    }
}
