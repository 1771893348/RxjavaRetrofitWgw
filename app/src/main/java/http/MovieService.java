package http;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by liukun on 16/3/9.
 */
public interface MovieService {

    @GET("top250")
    Call<String> getTopMovie(@Query("start") int start, @Query("count") int count);
}
