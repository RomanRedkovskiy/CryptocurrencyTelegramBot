package com.example.hackathon2024.serialization;

import com.example.hackathon2024.model.CryptoCurrency;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashSet;
import java.util.Set;

public class JsonSerializer {

    public HashSet<CryptoCurrency> deserialize(String data) {
        return new Gson().fromJson(data, new TypeToken<Set<CryptoCurrency>>() {}.getType());
    }
}
