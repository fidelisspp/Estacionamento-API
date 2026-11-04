package com.joaovitor.estacionamento_api.apikey;

import com.joaovitor.estacionamento_api.entity.ApiKey;
import com.joaovitor.estacionamento_api.entity.Plano;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Bucket> baldes = new ConcurrentHashMap<>();

    public ConsumptionProbe consumir(ApiKey apiKey) {
        String identificador = apiKey.getChave() + ":" + apiKey.getPlano().name();
        Bucket balde = baldes.computeIfAbsent(identificador, id -> criarBalde(apiKey.getPlano()));
        return balde.tryConsumeAndReturnRemaining(1);
    }

    private Bucket criarBalde(Plano plano) {
        int limite = plano.getLimitePorMinuto();
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(limite).refillGreedy(limite, Duration.ofMinutes(1)))
                .build();
    }
}
