package ma.tarik.democqrs.commonApi.events;

import ma.tarik.democqrs.commonApi.enums.AccountStatus;

public class AccountActivatedEvent extends BaseEvent<String>{
    private AccountStatus accountStatus;

    public AccountActivatedEvent(String id, AccountStatus accountStatus) {
        super(id);
        this.accountStatus = accountStatus;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }
}
