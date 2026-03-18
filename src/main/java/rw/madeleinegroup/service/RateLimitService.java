package rw.madeleinegroup.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<Long, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createBucket() {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(20, Refill.intervally(20, Duration.ofHours(1))))
            .build();
    }

    public boolean tryConsume(Long userId) {
        Bucket bucket = buckets.computeIfAbsent(userId, id -> createBucket());
        return bucket.tryConsume(1);
    }

    public long getRemainingTokens(Long userId) {
        Bucket bucket = buckets.computeIfAbsent(userId, id -> createBucket());
        return bucket.getAvailableTokens();
    }
}
