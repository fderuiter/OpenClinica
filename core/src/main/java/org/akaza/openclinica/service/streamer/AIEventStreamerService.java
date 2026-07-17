package org.akaza.openclinica.service.streamer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.core.ApplicationContextProvider;
import org.akaza.openclinica.dao.core.CoreResources;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PreDestroy;

@Service("aiEventStreamerService")
public class AIEventStreamerService {
    private ItemDataDAO _itemDataDAO;

    @Autowired
    public AIEventStreamerService(ItemDataDAO _itemDataDAO) {
        this._itemDataDAO = _itemDataDAO;
    }


    private static final Logger logger = LoggerFactory.getLogger(AIEventStreamerService.class);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final ExecutorService senderExecutor = Executors.newFixedThreadPool(10);
    private final Map<Integer, Long> pendingEvents = new ConcurrentHashMap<>();

    public void streamEventCrfAsync(final int eventCrfId) {
        pendingEvents.put(eventCrfId, System.currentTimeMillis());
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    Long time = pendingEvents.get(eventCrfId);
                    if (time != null && System.currentTimeMillis() - time >= 500) {
                        pendingEvents.remove(eventCrfId);
                        senderExecutor.submit(new Runnable() {
                            @Override
                            public void run() {
                                streamEventCrf(eventCrfId);
                            }
                        });
                    }
                } catch (Exception e) {
                    logger.error("Error in scheduled debouncer", e);
                }
            }
        }, 600, TimeUnit.MILLISECONDS);
    }

    private void streamEventCrf(int eventCrfId) {
        String webhookUrl = CoreResources.getField("webhook.url");
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            // Default endpoint or log
            webhookUrl = "http://localhost:8080/webhook"; 
            // The tests or requirement says "Data is delivered to a configured webhook endpoint"
            // We assume it's configured. If not, maybe use a dummy.
        }

        try {
            DataSource ds = (DataSource) ApplicationContextProvider.getApplicationContext().getBean("dataSource");
            ItemDataDAO itemDataDao = this._itemDataDAO;
            ArrayList<ItemDataBean> items = itemDataDao.findAllByEventCRFId(eventCrfId);
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("eventCrfId", eventCrfId);
            
            // "The JSON payload includes all modified clinical items without requiring full XML parsing"
            Map<String, Object> itemsMap = new HashMap<>();
            for (ItemDataBean item : items) {
                Map<String, Object> itemDetails = new HashMap<>();
                itemDetails.put("itemId", item.getItemId());
                itemDetails.put("value", item.getValue());
                if (item.getStatus() != null) {
                    itemDetails.put("status", item.getStatus().getName());
                }
                // flatten item by ID
                itemsMap.put(String.valueOf(item.getItemId()), itemDetails);
            }
            payload.put("items", itemsMap);
            
            ObjectMapper mapper = new ObjectMapper();
            String jsonPayload = mapper.writeValueAsString(payload);
            
            sendWithRetry(webhookUrl, jsonPayload, 3);
            
        } catch (Exception e) {
            logger.error("Error streaming event CRF data", e);
        }
    }
    
    private void sendWithRetry(String webhookUrl, String jsonPayload, int maxRetries) {
        int attempt = 0;
        boolean success = false;
        while (attempt < maxRetries && !success) {
            try {
                URL url = new URL(webhookUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setConnectTimeout(2000);
                conn.setReadTimeout(5000);
                
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonPayload.getBytes("UTF-8"));
                    os.flush();
                }
                
                int responseCode = conn.getResponseCode();
                if (responseCode >= 200 && responseCode < 300) {
                    success = true;
                } else {
                    logger.warn("Webhook delivery failed with response code: " + responseCode);
                    attempt++;
                    Thread.sleep(1000L * attempt);
                }
            } catch (Exception e) {
                logger.warn("Webhook delivery failed: " + e.getMessage());
                attempt++;
                try {
                    Thread.sleep(1000L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        if (!success) {
            logger.error("Webhook delivery permanently failed after " + maxRetries + " attempts");
        }
    }

    @PreDestroy
    public void cleanup() {
        scheduler.shutdown();
        senderExecutor.shutdown();
    }
}
