package com.ms.inventory.authenticationModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface AuthenticationService {
    @GET
    Call<AuthResponse>getAuthResponse(@Url String endUrl);
}
