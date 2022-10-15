package com.example.authentificationservice.utils;

public class JWTUtils {
    public static String SECRET = "secret";

    public static String PREFIX = "Bearer ";

    public static String ROLES = "roles";

    public static String REFRESH_TOKEN_URL = "/api/token/refresh";
    public static Long ACCESS_TOKEN = Long.valueOf(60 * 24 * 30 * 60 * 1000);
    public static Long REFRESH_TOKEN = Long.valueOf(60 * 24 * 30 * 12 * 60 * 1000);

    public static String USERNAME = "email";

    public static String PASSWORD = "password";



}
