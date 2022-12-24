package ma.tarik.democqrs.commonApi.events;

public class AccountCreditedEvent extends BaseEvent<String>{
    private double creditAmount;
    private String currency;

    public AccountCreditedEvent(String id, double creditAmount, String currency) {
        super(id);
        this.creditAmount = creditAmount;
        this.currency = currency;
    }

    public double getCreditAmount() {
        return creditAmount;
    }

    public String getCurrency() {
        return currency;
    }

}
