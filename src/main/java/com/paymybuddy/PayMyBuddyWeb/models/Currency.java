package com.paymybuddy.paymybuddyweb.models;

import com.paymybuddy.paymybuddyweb.interfaces.service.HTTPRequestServiceInterface;
import com.paymybuddy.paymybuddyweb.services.HTTPRequestService;
import org.json.JSONObject;

import java.io.IOException;

/**
 * @author MorganCpn
 */
public class Currency {
    private String code;
    private Double rateBasedUSD;

    /**
     * Constructor
     * @param code
     * @throws IOException
     */
    public Currency(String code) throws IOException {
        this.code = code.toUpperCase();
        this.getInfoCurrency(code.toUpperCase());
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) throws IOException {
        this.code = code.toUpperCase();
        this.getInfoCurrency(code.toUpperCase());
    }

    public Double getRateBasedUSD() {
        return rateBasedUSD;
    }

    public void setRateBasedUSD(Double rateBasedUSD) {
        this.rateBasedUSD = rateBasedUSD;
    }

    /**
     * Get Currency rate from External API
     * @param code
     * @throws IOException
     */
    private void getInfoCurrency(String code) throws IOException {
        HTTPRequestServiceInterface httpRequestService = new HTTPRequestService();
        JSONObject data = httpRequestService.getReq("https://api.exchangeratesapi.io/latest?base=USD", null);
        Integer status = data.getInt("status");
        if (status < 299) {
            JSONObject rates = (JSONObject) ((JSONObject) data.get("content")).get("rates");
            if (!rates.isNull(code)) this.setRateBasedUSD(rates.getDouble(code));
            else {
                this.code = null;
                this.rateBasedUSD = null;
            }
        }
    }

    /**
     * Get currency symbol
     * @return
     */
    public String getSymbol(){
        if (this.getCode().equals("USD")) return "$";
        else if (this.getCode().equals("EUR")) return "€";
        else if (this.getCode().equals("JPY")) return "¥";
        else if (this.getCode().equals("GBP")) return "£";
        else return null;
    }
}
