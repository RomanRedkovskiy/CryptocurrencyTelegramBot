package com.example.hackathon2024.components;

import com.example.hackathon2024.CryptoCurrencyBot;
import com.example.hackathon2024.model.CryptoCurrency;
import com.example.hackathon2024.model.User;
import com.example.hackathon2024.repository.CryptoRepository;
import com.example.hackathon2024.repository.UserRepository;
import com.example.hackathon2024.serialization.JsonSerializer;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class Scheduler {

    private static final String URL = "https://api.mexc.com/api/v3/ticker/price";

    private final CryptoRepository cryptoRepository;
    private final UserRepository userRepository;

    private final CryptoCurrencyBot cryptoCurrencyBot;


    @Autowired
    public Scheduler(CryptoCurrencyBot cryptoCurrencyBot, CryptoRepository cryptoRepository,
                     UserRepository userRepository) {
        this.cryptoCurrencyBot = cryptoCurrencyBot;
        this.cryptoRepository = cryptoRepository;
        this.userRepository = userRepository;
    }

    @Getter
    @Setter
    private static Set<CryptoCurrency> fetchedCrypto;


    @Scheduled(fixedRate = 20000)
    public void schedulerMethod() {
        fetchData();

        Set<CryptoCurrency> databaseCrypto = cryptoRepository.findAll();

        Iterable<User> users = userRepository.findAll();
        for (User user : users) {
            String alertData = findPriceDifference(fetchedCrypto, databaseCrypto, user.getPercentage());
            fetchedCrypto.forEach(crypto ->
                    cryptoRepository.insertOrUpdateCryptoData(crypto.getSymbol(), crypto.getPrice())
            );

            cryptoCurrencyBot.sendAlert(user.getChat_id(), alertData);
        }
    }

    private void fetchData() {
        RestTemplate restTemplate = new RestTemplate();
        JsonSerializer jsonSerializer = new JsonSerializer();
        setFetchedCrypto(jsonSerializer.deserialize(restTemplate.getForObject(URL, String.class)));
    }

    public static String findPriceDifference(Set<CryptoCurrency> fetchedCrypto,
                                             Set<CryptoCurrency> databaseCrypto,
                                             double percentage) {
        StringBuilder result = new StringBuilder();

        // Create a map to store currency symbols and prices from database set
        Map<String, Double> priceMap = new HashMap<>();
        for (CryptoCurrency databaseCryptoRow : databaseCrypto) {
            priceMap.put(databaseCryptoRow.getSymbol(), databaseCryptoRow.getPrice());
        }

        for (CryptoCurrency fetchedCryptoRow : fetchedCrypto) {
            String symbol = fetchedCryptoRow.getSymbol();
            if (priceMap.containsKey(symbol)) {
                double fetchedPrice = fetchedCryptoRow.getPrice();
                double databasePrice = priceMap.get(symbol);

                // Calculate percentage difference
                double percentageDifference = Math.abs((fetchedPrice - databasePrice) / fetchedPrice) * 100;

                if (percentageDifference > percentage) {
                    result.append("Price for ")
                            .append(symbol)
                            .append(" was changed from ")
                            .append(databasePrice)
                            .append(" to ")
                            .append(fetchedPrice)
                            .append(".\n");
                }
            }
        }
        return result.toString();
    }
}