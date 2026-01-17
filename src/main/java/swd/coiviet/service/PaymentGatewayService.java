package swd.coiviet.service;

import swd.coiviet.model.Payment;

public interface PaymentGatewayService {
    /**
     * Create payment URL for MoMo
     */
    String createMoMoPaymentUrl(Payment payment, String returnUrl, String notifyUrl);
    
    /**
     * Create payment URL for VNPay
     */
    String createVnPayPaymentUrl(Payment payment, String returnUrl, String ipAddress);
    
    /**
     * Verify MoMo payment callback
     */
    boolean verifyMoMoCallback(String partnerRefId, Long amount, String status);
    
    /**
     * Verify VNPay payment callback
     */
    boolean verifyVnPayCallback(java.util.Map<String, String> params);
}
