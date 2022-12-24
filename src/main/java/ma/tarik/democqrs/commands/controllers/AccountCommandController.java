package ma.tarik.democqrs.commands.controllers;

import ma.tarik.democqrs.commonApi.commands.CreateAccountCommand;
import ma.tarik.democqrs.commonApi.dto.CreateAccountRequestDTO;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@RestController
@RequestMapping(path = "/api/v1/accountCommands")
public class AccountCommandController {
    private CommandGateway cg;
    private EventStore es;

   public AccountCommandController(CommandGateway cg,EventStore es) {
        this.cg = cg;
        this.es = es;
    }

    @PostMapping(path = "/createAccount")
    public CompletableFuture<String> createAccount(@RequestBody CreateAccountRequestDTO request) {
        CompletableFuture<String> cr = cg.send(new CreateAccountCommand(UUID.randomUUID().toString(),
                request.getInitialBalance(), request.getCurrency()));
        return cr;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        ResponseEntity<String> entity = new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        return entity;
    }
    @GetMapping("/eventStore/{accountId}")
    public Stream eventStore(@PathVariable String accountId) {
        return es.readEvents(accountId).asStream();
    }
}
