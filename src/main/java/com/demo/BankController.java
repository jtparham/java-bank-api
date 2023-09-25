package com.demo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.Bank.BankAccount;
import com.demo.Bank.BankAccountRepository;
import com.demo.Bank.CreateAccountRequest;
import com.demo.Customer.Customer;
import com.demo.Customer.CustomerRepository;
import com.demo.Transaction.Transaction;
import com.demo.Transaction.TransactionRepository;
import com.demo.Transaction.TransactionRequest;

@RestController
@RequestMapping("/api")
class BankController {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    BankAccountRepository bankAccountRepository;

    @Autowired
    TransactionRepository transactionRepository;

    final String BAD_CUSTOMER_ID = "Invalid customer id";
    final String BAD_SENDER_ID = "Invalid sender id";
    final String BAD_RECEIVER_ID = "Invalid receiver id";
    final String BAD_OWNER_SENDER = "Sending bank account does not belong to sending customer";
    final String BAD_OWNER_RECEIVER = "Receiving bank account does not belong to receiving customer";
    final String NO_TRANSACTIONS_FOUND = "No transactions found";
    final String BAD_TRANSACTION_ID = "There was an error retreiving the transactions";
    final String NO_BALANCES_FOUND = "No balances found";
    final String INSUFFICIENT_FUNDS = "Insufficient funds";
    final String BAD_INITIAL_DEPOSIT = "Initial deposit be a number and equal or greater than zero";
    final String NO_ACCOUNT_FOUND = "No account found with that id";
    final String ACCOUNT_CREATED = "Account successfully created";
    final String TRANSFER_COMPLETED = "Transfer successful";
    final String HISTORY_FETCHED = "Returned all transactions";
    final String BAD_SENDER_ACCOUNT_ID = "Sender does not have an account with that id";
    final String BAD_RECEIVER_ACCOUNT_ID = "Receiver does not have an account with that id";

    /**
     * Fetch the transaction history for a given customer id. If a customer id that
     * doesnt exist is handed in, returns an error that the customer does not exist.
     * If the list is empty, return a notification that the list is empty. Else,
     * return list of transactions.
     * 
     * @param id
     * @return
     */
    @GetMapping("/getCustomerTransactionDetails/{id}")
    public ResponseEntity<List<String>> returnTransferHistory(@PathVariable Long id) {
        List<String> res = new ArrayList<String>();
        try {

            if (checkValidCustomer(id)) {
                res.add(BAD_CUSTOMER_ID);
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
            }

            List<String> transactionDetails = new ArrayList<>();

            List<Transaction> transactions = transactionRepository.findAllByCustomerId(id);
            for (Transaction transaction : transactions) {
                transactionDetails.add(transaction.getTransactionDetails());
            }

            if (transactionDetails.isEmpty()) {
                res.add(NO_TRANSACTIONS_FOUND);
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(transactionDetails, HttpStatus.OK);
        } catch (Exception e) {
            res.add(e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all balances for a customer. If no valid customer, return an error that
     * customer doesn't exist. Else, return list.
     * 
     * @param id
     * @return
     */
    @GetMapping("/getCustomerBalances/{id}")
    public ResponseEntity<List<String>> getCustomerBalancesById(@PathVariable Long id) {
        List<String> res = new ArrayList<String>();
        try {
            if (checkValidCustomer(id)) {
                res.add(BAD_CUSTOMER_ID);
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
            }

            List<BigDecimal> balanceList = bankAccountRepository.getBalances(id);
            for (BigDecimal balance : balanceList) {
                res.add(balance.toString());
            }

            if (balanceList.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            res.add(e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
     * Create a new bank account for a customer, with an initial deposit amount. A
     * single customer may have multiple bank accounts. If a customer does not
     * exist, return an error. If the deposit is less than zero, return an error
     * that
     * the deposit must be equal to or greater than zero.
     */
    @PostMapping("/createBankAccount")
    public ResponseEntity<List<String>> createNewBankAccountForCustomer(@RequestBody CreateAccountRequest request) {
        List<String> res = new ArrayList<String>();
        try {
            if (checkValidCustomer(request.getCustomer_id())) {
                res.add(BAD_CUSTOMER_ID);
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
            }

            if (request.getInitialDeposit().compareTo(BigDecimal.ZERO) < 0) {
                res.add(BAD_INITIAL_DEPOSIT);
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
            }

            BankAccount account = new BankAccount(request.getCustomer_id(), request.getInitialDeposit());

            bankAccountRepository.save(account);

            res.add(ACCOUNT_CREATED);
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            res.add(e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
     * Transfer amounts between any two accounts, including those owned by
     * different customers.
     * NOTE: transaction details is a string generated in the function.
     * "FROM {sender_name} {amount}TO {reciever_name}"
     * 
     */
    @PutMapping("/transfer")
    public ResponseEntity<List<String>> transferBetweenAccountsAndRecordTransaction(
            @RequestBody TransactionRequest transactionRequest) {
        List<String> res = new ArrayList<String>();
        try {
            Optional<BankAccount> optionalSenderBankAccount = bankAccountRepository
                    .findById(transactionRequest.getSender_account_id());

            Optional<BankAccount> optionalReceiverBankAccount = bankAccountRepository
                    .findById(transactionRequest.getReceiver_account_id());

            if (checkValidCustomer(transactionRequest.getReceiving_customer_id())) {
                res.add(BAD_RECEIVER_ID);
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
            }
            if (checkValidCustomer(transactionRequest.getSending_customer_id())) {
                res.add(BAD_SENDER_ID);
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
            }

            Customer receivingCustomer = customerRepository.findById(transactionRequest.getReceiving_customer_id())
                    .orElse(null);
            Customer sendingCustomer = customerRepository.findById(transactionRequest.getSending_customer_id())
                    .orElse(null);

            if (optionalSenderBankAccount.isEmpty()) {
                res.add(BAD_SENDER_ACCOUNT_ID);
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
            }

            if (optionalReceiverBankAccount.isEmpty()) {
                res.add(BAD_RECEIVER_ACCOUNT_ID);
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
            }

            BankAccount senderBankAccount = optionalSenderBankAccount.get();
            BankAccount receiverBankAccount = optionalReceiverBankAccount.get();

            if (senderBankAccount.getBalance().compareTo(transactionRequest.getAmount()) < 0) {
                res.add(INSUFFICIENT_FUNDS);
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
            }

            if (senderBankAccount.getCustomer_id() != sendingCustomer.getId()) {
                res.add(BAD_OWNER_SENDER);
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
            }

            if (receiverBankAccount.getCustomer_id() != receivingCustomer.getId()) {
                res.add(BAD_OWNER_RECEIVER);
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
            }
            senderBankAccount.setBalance(senderBankAccount.getBalance().subtract(transactionRequest.getAmount()));
            receiverBankAccount.setBalance(receiverBankAccount.getBalance().add(transactionRequest.getAmount()));

            bankAccountRepository.save(senderBankAccount);
            bankAccountRepository.save(receiverBankAccount);

            Transaction transaction = new Transaction();
            transaction.setSender_account_id(transactionRequest.getSender_account_id());
            transaction.setReceiver_account_id(transactionRequest.getReceiver_account_id());
            transaction.setAmount(transactionRequest.getAmount());

            String receiverName = receivingCustomer.getName();
            String senderName = sendingCustomer.getName();

            transaction.setTransactionDetails(
                    "FROM " + senderName + " " + transactionRequest.getAmount() + " TO " + receiverName);
            transactionRepository.save(transaction);

            res.add(TRANSFER_COMPLETED);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.add(e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }

    }

    public boolean checkValidCustomer(Long id) {
        Optional<Customer> targetCustomer = customerRepository.findById(id);

        return targetCustomer.isEmpty();
    }

}