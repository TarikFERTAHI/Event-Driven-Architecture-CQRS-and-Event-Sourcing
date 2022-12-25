<h1> Event Driven Architecture CQRS and Event Sourcing </h1>
Créer une application qui permet de gérer des comptes respectant les patterns CQRS et Event Sourcing avec les Framework AXON et Spring Boot.
<p align="center">
  <img src="https://github.com/Amina-contact/Architectures-Micro-Services/blob/master/Devoir%205/pictures/1.JPG" class="center">
</p>
<h3>Dependencies : </h3>
<pre class="notranslate"><code>
   - Spring web
   - Spring data JPA
   - MySQL Driver
   - Lombok
   - Axon Framwork
</code></pre>
<h3>Application.properties : </h3>
<pre class="notranslate"><code>
spring.application.name=compte-service
spring.datasource.url=jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/bank?createDatabaseIfNotExist=true
spring.datasource.username=${MYSQL_USER:root}
spring.datasource.password=${MYSQL_PASSWORD:}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MariaDBDialect
server.port=8082 
</code></pre>
<h3>Commands & Events : </h3>
<li>BaseCommand</strong>:</li>
<pre class="notranslate"><code>
public abstract class BaseCommand<T> {
    @TargetAggregateIdentifier
    @Getter private T id;
    public BaseCommand(T id) {
        this.id = id;
    }
} 
</code></pre>
<li>CreateAccountCommand</strong>:</li>
<pre class="notranslate"><code>
public class CreateAccountCommand extends BaseCommand<String>{
    private double initialBalance;
    private String currency;
    public CreateAccountCommand(String id, double accountBalance, String currency) {
        super(id);
        this.initialBalance = accountBalance;
        this.currency = currency;
    }
    public double getInitialBalance() {
        return initialBalance;
    }
    public String getCurrency() {
        return currency;
    }
}
</code></pre>
<li>DebitAccountCommand</strong>:</li>
<pre class="notranslate"><code>
public class DebitAccountCommand extends BaseCommand<String>{
    @Getter private double debitAmount;
    @Getter private String currency;
    public DebitAccountCommand(String id, double debitAmount, String currency) {
        super(id);
        this.debitAmount = debitAmount;
        this.currency = currency;
    }
} 
</code></pre>
<li>CreditAccountCommand</strong>:</li>
<pre class="notranslate"><code>
public class CreditAccountCommand extends BaseCommand<String>{
    @Getter private double creditAmount;
    @Getter private String currency;
    public CreditAccountCommand(String id, double creditAmount, String currency) {
        super(id);
        this.creditAmount = creditAmount;
        this.currency = currency;
    }
}
</code></pre>
<h3>Commands Controllers : </h3>
<li>AccountCommandController</strong>:</li>
<pre class="notranslate"><code>
public class AccountCommandController {
    private CommandGateway commandGateway;
    private EventStore eventStore;
    @PostMapping("/create")
    public CompletableFuture<String> createAccount(@RequestBody CreateAccountRequestDTO request){
        CompletableFuture<String> commandResponse = commandGateway.send(new CreateAccountCommand(
                UUID.randomUUID().toString(),
                request.getInitialBalance(),
                request.getCurrency()
        ));
        return commandResponse;
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> exceptionHandler(Exception exception){
        ResponseEntity<String> responseEntity = new ResponseEntity<>(
                exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR
        );
        return responseEntity;
    }
    @GetMapping("/eventStore/{accountId}")
    public Stream eventStore(@PathVariable String accountId){
        return eventStore.readEvents(accountId).asStream();
    }
    @PutMapping("/credit")
    public CompletableFuture<String> creditAccount(@RequestBody CreditAccountRequestDTO creditAccountRequestDTO){
        CompletableFuture<String> creditAccountCommandResponse = commandGateway.send(new CreditAccountCommand(
                creditAccountRequestDTO.getAccountId(),
                creditAccountRequestDTO.getAmount(),
                creditAccountRequestDTO.getCurrency()
        ));
        return creditAccountCommandResponse;
    }
    @PutMapping("/debit")
    public CompletableFuture<String> debitAccount(@RequestBody DebitAccountRequestDTO debitAccountRequestDTO){
        CompletableFuture<String> debitAccountCommandResponse = commandGateway.send(new DebitAccountCommand(
                debitAccountRequestDTO.getAccountId(),
                debitAccountRequestDTO.getAmount(),
                debitAccountRequestDTO.getCurrency()
        ));
        return debitAccountCommandResponse;
    }
}
</code></pre>
<li>DTOS</strong>:</li>
<li>CreateAccountRequestDTO</strong>:</li>
<pre class="notranslate"><code>
@Data @NoArgsConstructor @AllArgsConstructor
public class CreateAccountRequestDTO {
    private double initialBalance;
    private String currency;
} 
</code></pre>
<li>CreditAccountRequestDTO</strong>:</li>
<pre class="notranslate"><code>
@Data
public class CreditAccountRequestDTO {
    private String accountId;
    private double amount;
    private String currency;
}
</code></pre>
<li>DebitAccountRequestDTO</strong>:</li>
<pre class="notranslate"><code>
@Data
public class DebitAccountRequestDTO {
    private String accountId;
    private double amount;
    private String currency;
}
</code></pre>
<h3>Testion create account : </h3>
<li>create account</strong>:</li>
<p align="center">
  <img src="https://github.com/Amina-contact/Architectures-Micro-Services/blob/master/Devoir%205/pictures/Test1.JPG">
</p>
<li>After exception</strong>:</li>
<p align="center">
  <img src="https://github.com/Amina-contact/Architectures-Micro-Services/blob/master/Devoir%205/pictures/Test1AfterException.JPG">
</p>
<li>Add an exceptionHandler method to AccountCommandController</strong>:</li>
<pre class="notranslate"><code>
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> exceptionHandler(Exception exception){
        ResponseEntity<String> responseEntity = new ResponseEntity<>(
                exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR
        );
        return responseEntity;
    }  
</code></pre>
<h3>Events : </h3>
<li>BaseEvent</strong>:</li>
<pre class="notranslate"><code>
public abstract class BaseEvent<T> {
    @Getter private T id;
    public BaseEvent(T id) {
        this.id = id;
    }
}  
</code></pre>
<li>AccountActivatedEvent</strong>:</li>
<pre class="notranslate"><code>
public class AccountActivatedEvent extends BaseEvent<String>{
    @Getter
    private AccountStatus accountStatus;
    public AccountActivatedEvent(String id, AccountStatus accountStatus) {
        super(id);
        this.accountStatus = accountStatus;
    }
}  
</code></pre>
<li>AccountCreatedEvent</strong>:</li>
<pre class="notranslate"><code>
public class AccountCreatedEvent extends BaseEvent<String>{
    @Getter
    private double initialBalance;
    @Getter
    private String currency;
    @Getter
    private AccountStatus accountStatus;
    public AccountCreatedEvent(String id, double initialBalance, String currency, AccountStatus accountStatus) {
        super(id);
        this.initialBalance = initialBalance;
        this.currency = currency;
        this.accountStatus = accountStatus;
    }
} 
</code></pre>
<li>AccountCreditedEvent</strong>:</li>
<pre class="notranslate"><code>
public class AccountCreditedEvent extends BaseEvent<String>{
    @Getter
    private double amount;
    @Getter
    private String currency;
    @Getter
    private Date operationDate;
    public AccountCreditedEvent(String s, double amount, String currency, Date operationDate) {
        super(s);
        this.amount = amount;
        this.currency = currency;
        this.operationDate = operationDate;
    }
}
</code></pre>
<li>AccountDebitedEvent</strong>:</li>
<pre class="notranslate"><code>
public class AccountDebitedEvent extends BaseEvent<String>{
    @Getter
    private double amount;
    @Getter
    private String currency;
    @Getter
    private Date operationDate;
    public AccountDebitedEvent(String s, double amount, String currency, Date operationDate) {
        super(s);
        this.amount = amount;
        this.currency = currency;
        this.operationDate = operationDate;
    }
}
</code></pre>
<h3>Aggregates : </h3>
<li>AccountAggregate</strong>:</li>
<pre class="notranslate"><code>
@Aggregate
public class AccountAggregate {
    @AggregateIdentifier
    private String accountId;
    private double balance;
    private String currency;
    private AccountStatus status;
    public AccountAggregate(){
        // Required by Axon
    }
    @CommandHandler // Subscribe to Command Bus, and listen to the CreateAccountCommand events
    public AccountAggregate(CreateAccountCommand createAccountCommand){
        // Business logic Every new account well have a new aggregate
        if(createAccountCommand.getInitialBalance() < 0) throw new RuntimeException(" Balance Negative");
        AggregateLifecycle.apply(new AccountCreatedEvent(
                // Command to event
                createAccountCommand.getId(),
                createAccountCommand.getInitialBalance(),
                createAccountCommand.getCurrency(),
                AccountStatus.ACTIVATED
        ));
    }
    @EventSourcingHandler
    public void on(AccountCreatedEvent accountCreatedEvent){
        this.accountId = accountCreatedEvent.getId();
        this.balance = accountCreatedEvent.getInitialBalance();
        this.currency = accountCreatedEvent.getCurrency();
        this.status = AccountStatus.CREATED;
        AggregateLifecycle.apply(new AccountActivatedEvent(accountCreatedEvent.getId(), AccountStatus.ACTIVATED));
    }
    @EventSourcingHandler
    public void on(AccountActivatedEvent accountActivatedEvent){
        this.status = accountActivatedEvent.getAccountStatus();
    }
    @CommandHandler
    public void handle(CreditAccountCommand creditAccountCommand){
        if(creditAccountCommand.getCreditAmount() <= 100) throw new InsufficientCreditAmount("Credit Amount lower than 100");
        AggregateLifecycle.apply(new AccountCreditedEvent(
                creditAccountCommand.getId(),
                creditAccountCommand.getCreditAmount(),
                creditAccountCommand.getCurrency(),
                new Date()
        ));
    }
    @EventSourcingHandler
    public void on(AccountCreditedEvent accountCreditedEvent){
        this.balance += accountCreditedEvent.getAmount();
    }
    @CommandHandler
    public void handler(DebitAccountCommand debitAccountCommand){
        if(debitAccountCommand.getDebitAmount() < 0) throw new InsufficientCreditAmount("Amount can't be negative");
        if(this.balance < debitAccountCommand.getDebitAmount()) throw new InsufficientBalanceToDebitException("Balance not sufficient");
        AggregateLifecycle.apply(new AccountDebitedEvent(
                debitAccountCommand.getId(),
                debitAccountCommand.getDebitAmount(),
                debitAccountCommand.getCurrency(),
                new Date()
        ));
    }
    @EventSourcingHandler
    public void on(AccountDebitedEvent accountDebitedEvent){
        this.balance -= accountDebitedEvent.getAmount();
    }
} 
</code></pre>
<li>Testing create account</strong>:</li>
<p align="center">
  <img src="https://github.com/Amina-contact/Architectures-Micro-Services/blob/master/Devoir%205/pictures/Test1AfterEventHandler.JPG">
</p>
<li>See domain_event_entry in the DataBase</strong>:</li>
<p align="center">
  <img src="https://github.com/Amina-contact/Architectures-Micro-Services/blob/master/Devoir%205/pictures/Test2.JPG">
</p>
<p align="center">
  <img src="https://github.com/Amina-contact/Architectures-Micro-Services/blob/master/Devoir%205/pictures/Tes21.JPG">
</p>
<li>See the event content</strong>:</li>
<p align="center">
  <img src="https://github.com/Amina-contact/Architectures-Micro-Services/blob/master/Devoir%205/pictures/Test23.JPG">
</p>
<li>Consult the eventStore</strong>:</li>
<p align="center">
  <img src="https://github.com/Amina-contact/Architectures-Micro-Services/blob/master/Devoir%205/pictures/Test24AfterSream.JPG">
</p>
<li>Testing Credit</strong>:</li>
<p align="center">
  <img src="https://github.com/Amina-contact/Architectures-Micro-Services/blob/master/Devoir%205/pictures/TestCredit.JPG">
</p>
<li>Testing Debit</strong>:</li>
<p align="center">
  <img src="https://github.com/Amina-contact/Architectures-Micro-Services/blob/master/Devoir%205/pictures/TestDebit.JPG">
</p>
<p align="center">
  <img src="https://github.com/Amina-contact/Architectures-Micro-Services/blob/master/Devoir%205/pictures/TestDebit2.JPG">
</p>
<h3>Query : </h3>
<li>Entities</strong>:</li>
<li>Entity Account</strong>:</li>
<pre class="notranslate"><code>
public class Account {
    @Id
    private String id;
    private String currency;
    private double balance;
    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @OneToMany(mappedBy = "account")
    private Collection<Operation> operations;
}
</code></pre>
<li>Entity Account</strong>:</li>
<pre class="notranslate"><code>
public class Operation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(TemporalType.DATE)
    private Date date;
    private double amount;
    @Enumerated(EnumType.STRING)
    private OperationType type;
    @ManyToOne
    private Account account;
}
</code></pre>
<li>Repositories</strong>:</li>
<li>AccountRepository</strong>:</li>
<pre class="notranslate"><code>
public interface AccountRepository extends JpaRepository<Account,String> {
}
</code></pre>
<li>OperationRepository</strong>:</li>
<pre class="notranslate"><code>
public interface OperationRepository extends JpaRepository<Operation,Long> {
}
</code></pre>
<li>Services</strong>:</li>
<li>AccountServiceHandler</strong>:</li>
<pre class="notranslate"><code>
public class AccountServiceHandler {
    private AccountRepository accountRepository;
    private OperationRepository operationRepository;
    @EventHandler
    public void on(AccountCreatedEvent accountCreatedEvent){
        log.info("AccountCreatedEvent ");
        Account account =  Account
                .builder()
                .id(accountCreatedEvent.getId())
                .balance(accountCreatedEvent.getInitialBalance())
                .accountStatus(accountCreatedEvent.getAccountStatus())
                .currency(accountCreatedEvent.getCurrency())
                .build();
        accountRepository.save(account);
    }
    @EventHandler
    public void on(AccountActivatedEvent accountActivatedEvent){
        log.info(" AccountActivatedEvent ");
        Account account = accountRepository.findById(accountActivatedEvent.getId()).get();
        account.setAccountStatus(accountActivatedEvent.getAccountStatus());
        accountRepository.save(account);
    }
    @EventHandler
    public void on(AccountDebitedEvent accountDebitedEvent){
        log.info("AccountDebitedEvent ");
        Account account = accountRepository.findById(accountDebitedEvent.getId()).get();
        Operation operation = Operation.builder()
                .amount(accountDebitedEvent.getAmount())
                .date(new Date())
                .type(OperationType.DEBIT)
                .account(account)
                .build();
        operationRepository.save(operation);
        account.setBalance(account.getBalance() - accountDebitedEvent.getAmount());
        accountRepository.save(account);
    }
    @EventHandler
    public void on(AccountCreditedEvent accountCreditedEvent){
        log.info("AccountCreditedEvent ");
        Account account = accountRepository.findById(accountCreditedEvent.getId()).get();
        Operation operation = Operation.builder()
                .amount(accountCreditedEvent.getAmount())
                .date(new Date())
                .type(OperationType.DEBIT)
                .account(account)
                .build();
        operationRepository.save(operation);
        account.setBalance(account.getBalance() + accountCreditedEvent.getAmount());
        accountRepository.save(account);
    }
    @QueryHandler
    public List<Account> on(GetAllAccountsQuery getAllAccountQuery){
        return accountRepository.findAll();
    }

    @QueryHandler
    public Account on(GetAccountByIdQuery getAccountQuery){
        return accountRepository.findById(getAccountQuery.getId()).get();
    }
}
</code></pre>
<li>Controllers</strong>:</li>
<li>AccountQueryController</strong>:</li>
<pre class="notranslate">
public class AccountQueryController {
    private QueryGateway queryGateway;
    @GetMapping("/allAccounts")
    public List<Account> accountsList(){
        List<Account> response = queryGateway.query(new GetAllAccountsQuery(), ResponseTypes.multipleInstancesOf(Account.class)).join();
        return  response;
    }
    @GetMapping("/byId/{id}")
    public Account getAccount(@PathVariable String id){
        return queryGateway.query(new GetAccountByIdQuery(id), ResponseTypes.instanceOf(Account.class)).join();
    }
}
</code></pre>
<li>Testing Controller To get all Accounts</strong>:</li><br>
<pre class="notranslate">
[
    {
        "id": "0fdc2ea1-ce65-464b-858e-d22aa415dac2",
        "currency": "MAD",
        "balance": 9000.0,
        "accountStatus": "ACTIVATED"
    },
    {
        "id": "43ce2b7e-176f-462b-aecd-9656a0c6b979",
        "currency": "MAD",
        "balance": 67890.0,
        "accountStatus": "ACTIVATED"
    },
    {
        "id": "f2346a27-18cd-4b66-aaff-6b7c9c217913",
        "currency": "MAD",
        "balance": 467000.0,
        "accountStatus": "ACTIVATED"
    }
]
</code></pre><br>
<li>Testing Controller To get  AccountBy Id</strong>:</li><br>
<pre class="notranslate">
{
    "id": "b2567005-7346-430c-a83a-e7fe4353e372",
    "currency": "MAD",
    "balance": 9000.0,
    "accountStatus": "ACTIVATED"
}
</code></pre><br>
