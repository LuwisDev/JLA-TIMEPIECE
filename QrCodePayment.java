package jlaluxurywatch;

public class QrCodePayment extends Payment {

    @Override
    public String getName() {
        return "QR Code";
    }

    @Override
    public boolean isPaidOnline() {
        return true;
    }

    @Override
    public String getReferenceFieldLabel() {
        return "QR payment reference no.:";
    }

    @Override
    public String getReceiptLabel() {
        return "QR RefNo:    ";
    }

    @Override
    public String getCustomerInstruction() {
        return "Scan the store's QR code to pay, then type the reference number from your payment receipt.";
    }

    @Override
    public String getCashierHint(String formattedAmount, String customerReference) {
        return "Check the QR Code payment of " + formattedAmount + ". Customer's reference no.: " + customerReference;
    }

    @Override
    public String getQrImagePath() {
        return "images/qr.png";
    }
}
