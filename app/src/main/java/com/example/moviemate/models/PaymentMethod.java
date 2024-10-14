package com.example.moviemate.models;

public class PaymentMethod {
    public static enum TYPE {
        ZALO_PAY,
        MOMO,
        QR_CODE
    };

    private final TYPE type;
    private final String name;
    private final int imageResource;

    public PaymentMethod(TYPE type, String name, int imageResource) {
        this.type = type;
        this.name = name;
        this.imageResource = imageResource;
    }

    public TYPE getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getImageResource() {
        return imageResource;
    }
}
