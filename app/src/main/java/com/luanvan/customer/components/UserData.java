package com.luanvan.customer.components;

import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;

public class UserData {
    public static int getConsumerId(String token){
        String TOKEN_PREFIX = "Bearer ";
        JWT jwt = new JWT(token.replace(TOKEN_PREFIX,""));
        Claim claim = jwt.getClaim("userId");
        return claim.asInt();
    }
    public static String getConsumerUsername(String token){
        String TOKEN_PREFIX = "Bearer ";
        JWT jwt = new JWT(token.replace(TOKEN_PREFIX,""));
        return jwt.getSubject();
    }
}
