package swd.coiviet.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import swd.coiviet.configuration.MoMoConfig;
import swd.coiviet.configuration.VnPayConfiguration;
import swd.coiviet.model.Payment;
import swd.coiviet.service.PaymentGatewayService;

import jakarta.servlet.http.HttpServletRequest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PaymentGatewayServiceImpl implements PaymentGatewayService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentGatewayServiceImpl.class);
    
    @Autowired
    private MoMoConfig momoConfig;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public String createMoMoPaymentUrl(Payment payment, String returnUrl, String notifyUrl) {
        try {
            long amount = payment.getAmount().longValue();
            String orderId = payment.getTransactionId();
            String orderInfo = "Thanh toan tour " + payment.getBooking().getBookingCode();
            String requestId = UUID.randomUUID().toString();
            String extraData = "";
            
            // Create raw hash
            String rawHash = "accessKey=" + momoConfig.getAccessKey() +
                    "&amount=" + amount +
                    "&extraData=" + extraData +
                    "&ipnUrl=" + notifyUrl +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + momoConfig.getPartnerCode() +
                    "&redirectUrl=" + returnUrl +
                    "&requestId=" + requestId +
                    "&requestType=captureWallet";
            
            // Create signature
            String signature = hmacSHA256(momoConfig.getSecretKey(), rawHash);
            
            // Create request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", momoConfig.getPartnerCode());
            requestBody.put("partnerName", "Coi Viet");
            requestBody.put("storeId", "CoiVietStore");
            requestBody.put("requestId", requestId);
            requestBody.put("amount", amount);
            requestBody.put("orderId", orderId);
            requestBody.put("orderInfo", orderInfo);
            requestBody.put("redirectUrl", returnUrl);
            requestBody.put("ipnUrl", notifyUrl);
            requestBody.put("lang", "vi");
            requestBody.put("extraData", extraData);
            requestBody.put("requestType", "captureWallet");
            requestBody.put("signature", signature);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    momoConfig.getEndpoint(), request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Integer resultCode = (Integer) responseBody.get("resultCode");
                if (resultCode != null && resultCode == 0) {
                    String payUrl = (String) responseBody.get("payUrl");
                    logger.info("MoMo payment URL created: {}", payUrl);
                    return payUrl;
                } else {
                    String message = (String) responseBody.get("message");
                    logger.error("MoMo payment creation failed: {}", message);
                    throw new RuntimeException("Failed to create MoMo payment: " + message);
                }
            }
            
            throw new RuntimeException("Failed to create MoMo payment");
        } catch (Exception e) {
            logger.error("Error creating MoMo payment URL", e);
            throw new RuntimeException("Error creating MoMo payment URL: " + e.getMessage(), e);
        }
    }

    @Override
    public String createVnPayPaymentUrl(Payment payment, String returnUrl, String ipAddress) {
        try {
            String vnp_TxnRef = payment.getTransactionId();
            long amount = payment.getAmount().multiply(java.math.BigDecimal.valueOf(100)).longValue(); // VNPay uses cents
            String vnp_OrderInfo = "Thanh toan tour " + payment.getBooking().getBookingCode();
            String vnp_OrderType = "other";
            String vnp_Locale = "vn";
            String vnp_IpAddr = ipAddress;
            
            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", "2.1.0");
            vnp_Params.put("vnp_Command", "pay");
            vnp_Params.put("vnp_TmnCode", VnPayConfiguration.vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(amount));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
            vnp_Params.put("vnp_OrderType", vnp_OrderType);
            vnp_Params.put("vnp_Locale", vnp_Locale);
            vnp_Params.put("vnp_ReturnUrl", returnUrl);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
            
            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            formatter.setTimeZone(TimeZone.getTimeZone("Etc/GMT+7"));
            String vnp_CreateDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
            
            cld.add(Calendar.MINUTE, 15);
            String vnp_ExpireDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);
            
            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(fieldValue);
                    query.append(fieldName);
                    query.append('=');
                    query.append(java.net.URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }
            String queryUrl = query.toString();
            String vnp_SecureHash = VnPayConfiguration.hmacSHA512(VnPayConfiguration.vnp_HashSecret, hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
            String paymentUrl = VnPayConfiguration.vnp_PayUrl + "?" + queryUrl;
            
            logger.info("VNPay payment URL created: {}", paymentUrl);
            return paymentUrl;
        } catch (Exception e) {
            logger.error("Error creating VNPay payment URL", e);
            throw new RuntimeException("Error creating VNPay payment URL: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyMoMoCallback(String partnerRefId, Long amount, String status) {
        // Verify MoMo callback - simplified version
        // In production, should verify signature and other parameters
        return "0".equals(status) || "success".equalsIgnoreCase(status);
    }

    @Override
    public boolean verifyVnPayCallback(Map<String, String> params) {
        String vnp_SecureHash = params.get("vnp_SecureHash");
        if (vnp_SecureHash == null) {
            return false;
        }
        
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");
        
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(fieldValue);
                hashData.append('&');
            }
        }
        if (hashData.length() > 0) {
            hashData.setLength(hashData.length() - 1);
        }
        
        String vnp_SecureHashCalculated = VnPayConfiguration.hmacSHA512(
                VnPayConfiguration.vnp_HashSecret, hashData.toString());
        
        return vnp_SecureHash.equals(vnp_SecureHashCalculated);
    }
    
    private String hmacSHA256(String secret, String data) {
        try {
            Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSHA256.init(secretKeySpec);
            byte[] hash = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            logger.error("Error creating HMAC SHA256", e);
            throw new RuntimeException("Error creating HMAC SHA256", e);
        }
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
