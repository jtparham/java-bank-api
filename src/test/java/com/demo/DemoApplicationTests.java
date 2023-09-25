package com.demo;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import com.demo.Bank.BankAccount;
import com.demo.Bank.BankAccountRepository;
import com.demo.Bank.CreateAccountRequest;
import com.demo.Customer.CustomerRepository;
import com.demo.Transaction.TransactionRepository;
import com.demo.Transaction.TransactionRequest;

//This sql file contains pre-loaded transactions, accounts, and customers.
@Sql(scripts = "classpath:/data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoApplicationTests {

	@Autowired
	TestRestTemplate restTemplate;

	@Autowired
	CustomerRepository customerRepository;

	@Autowired
	BankAccountRepository bankAccountRepository;

	@Autowired
	TransactionRepository transactionRepository;

	// Confirm all balances a given account
	@Test
	public void testGetAllBalances() {
		// make sure the repos loaded.
		assertNotNull(customerRepository);
		assertNotNull(bankAccountRepository);
		assertNotNull(transactionRepository);

		Long customerId = Long.valueOf(1);
		// Getting all balances for Arisha

		ResponseEntity<String> balanceResponse = restTemplate
				.getForEntity("/api/getCustomerBalances/" + customerId, String.class);
		assertEquals(HttpStatus.OK, balanceResponse.getStatusCode());
		assertNotNull(balanceResponse.getBody());
		String formattedBalance = balanceResponse.getBody();

		// request.getBody truncates a space. adding it back
		formattedBalance = formattedBalance.replace(",", ", ");
		assertEquals("[\"520.00\", \"5520.00\"]", formattedBalance);

	}

	// This creates a new bank account for Branden Gibson
	@Test
	public void testCreateBankAccountWithInitialDeposit() {
		// make sure the repos loaded.
		assertNotNull(customerRepository);
		assertNotNull(bankAccountRepository);
		assertNotNull(transactionRepository);

		// Getting all balances for Branden should only return one account with a value
		// of 800
		List<BigDecimal> startingList = new ArrayList<BigDecimal>();
		startingList.add(new BigDecimal("800.00"));
		assertEquals(startingList, bankAccountRepository.getBalances(Long.valueOf(2)));

		CreateAccountRequest request = new CreateAccountRequest(Long.valueOf(2), BigDecimal.valueOf(3434334));

		ResponseEntity<String> response = restTemplate.postForEntity("/api/createBankAccount", request, String.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("[\"Account successfully created\"]", response.getBody());

		List<BigDecimal> expectedResult = new ArrayList<BigDecimal>();
		expectedResult.add(new BigDecimal("800.00"));
		expectedResult.add(new BigDecimal("3434334.00"));

		assertEquals(expectedResult, bankAccountRepository.getBalances(Long.valueOf(2)));
	}

	// Checks if a customer exists when getting transactionDetails
	@Test
	public void testBadCustomerIdForTransactionDetails() {
		// make sure the repos loaded.
		assertNotNull(customerRepository);
		assertNotNull(bankAccountRepository);
		assertNotNull(transactionRepository);

		// this is an invalid account number
		Long customerId = Long.valueOf(999);

		// hit the endpoint
		ResponseEntity<String> response = restTemplate
				.getForEntity("/api/getCustomerTransactionDetails/" + customerId, String.class);

		// expect an error message
		assertEquals("[\"Invalid customer id\"]", response.getBody().toString());
	}

	// Confirms that no details are returned for a valid customer
	@Test
	public void testNoTransactionDetailsForValidCustomer() {
		// make sure the repos loaded.
		assertNotNull(customerRepository);
		assertNotNull(bankAccountRepository);
		assertNotNull(transactionRepository);

		// Account for Judah has no transactions at start
		Long customerId = Long.valueOf(5);

		ResponseEntity<String> response = restTemplate
				.getForEntity("/api/getCustomerTransactionDetails/" + customerId, String.class);

		assertEquals("[\"No transactions found\"]", response.getBody().toString());
	}

	// confirms that no account is created for invalid customer id
	@Test
	public void testCreateBankAccountForInvalidCustomer() {
		// make sure the repos loaded.
		assertNotNull(customerRepository);
		assertNotNull(bankAccountRepository);
		assertNotNull(transactionRepository);

		CreateAccountRequest request = new CreateAccountRequest(Long.valueOf(999), BigDecimal.valueOf(3434334));
		ResponseEntity<String> response = restTemplate.postForEntity("/api/createBankAccount", request, String.class);
		assertEquals("[\"Invalid customer id\"]", response.getBody().toString());
	}

	// confirms that no account is created for invalid deposit
	@Test
	public void testCreateBankAccountForInvalidDeposit() {
		// make sure the repos loaded.
		assertNotNull(customerRepository);
		assertNotNull(bankAccountRepository);
		assertNotNull(transactionRepository);

		CreateAccountRequest request = new CreateAccountRequest(Long.valueOf(1), BigDecimal.valueOf(-1));
		ResponseEntity<String> response = restTemplate.postForEntity("/api/createBankAccount", request, String.class);
		assertEquals("[\"Initial deposit be a number and equal or greater than zero\"]", response.getBody().toString());
	}

	// confirms that no transfer occurs for invalid senderId
	@Test
	public void testTransferToInvalidSenderId() {
		// make sure the repos loaded.
		assertNotNull(customerRepository);
		assertNotNull(bankAccountRepository);
		assertNotNull(transactionRepository);

		// Judah Parham receives 20 from Georgina
		Long invalidSenderId = Long.valueOf(999);
		Long sendingBankAccountId = Long.valueOf(5);
		Long receiverId = Long.valueOf(5);
		Long receivingBankAccountId = Long.valueOf(6);
		BigDecimal amount = BigDecimal.valueOf(20);

		TransactionRequest request = new TransactionRequest(sendingBankAccountId, invalidSenderId,
				receivingBankAccountId, receiverId, amount);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<TransactionRequest> requestEntity = new HttpEntity<>(request,
				headers);

		ResponseEntity<String> transferResponse = restTemplate.exchange("/api/transfer",
				HttpMethod.PUT, requestEntity,
				String.class);

		assertEquals("[\"Invalid sender id\"]", transferResponse.getBody().toString());
	}

	// confirms that no account is created for invalid senderId
	@Test
	public void testTransferToInvalidReceiverId() {
		// make sure the repos loaded.
		assertNotNull(customerRepository);
		assertNotNull(bankAccountRepository);
		assertNotNull(transactionRepository);

		// Judah Parham receives 20 from Georgina
		Long senderId = Long.valueOf(4);
		Long sendingBankAccountId = Long.valueOf(5);
		Long invalidReceiverId = Long.valueOf(999);
		Long receivingBankAccountId = Long.valueOf(6);
		BigDecimal amount = BigDecimal.valueOf(20);

		TransactionRequest request = new TransactionRequest(sendingBankAccountId, senderId,
				receivingBankAccountId, invalidReceiverId, amount);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<TransactionRequest> requestEntity = new HttpEntity<>(request,
				headers);

		// restTemplate.put returns void so exchange must be used instead to check
		// status
		ResponseEntity<String> transferResponse = restTemplate.exchange("/api/transfer",
				HttpMethod.PUT, requestEntity,
				String.class);

		assertEquals("[\"Invalid receiver id\"]", transferResponse.getBody().toString());
	}

	// confirms that transfer denied for a sender without a matching account number
	@Test
	public void testTransferToInvalidSenderBankAccountId() {
		// make sure the repos loaded.
		assertNotNull(customerRepository);
		assertNotNull(bankAccountRepository);
		assertNotNull(transactionRepository);

		// Judah Parham receives 20 from Georgina
		Long senderId = Long.valueOf(4);
		Long invalidSendingBankAccountId = Long.valueOf(999);// 5
		Long receiverId = Long.valueOf(5);
		Long receivingBankAccountId = Long.valueOf(6);
		BigDecimal amount = BigDecimal.valueOf(20);

		TransactionRequest request = new TransactionRequest(invalidSendingBankAccountId, senderId,
				receivingBankAccountId, receiverId, amount);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<TransactionRequest> requestEntity = new HttpEntity<>(request,
				headers);

		// restTemplate.put returns void so exchange must be used instead to check
		// status
		ResponseEntity<String> transferResponse = restTemplate.exchange("/api/transfer",
				HttpMethod.PUT, requestEntity,
				String.class);

		assertEquals("[\"Sender does not have an account with that id\"]", transferResponse.getBody().toString());
	}

	// confirms that transfer denied for a sender without a matching account
	// number
	@Test
	public void testTransferToInvalidReceiverBankAccountId() {
		// make sure the repos loaded.
		assertNotNull(customerRepository);
		assertNotNull(bankAccountRepository);
		assertNotNull(transactionRepository);

		// Judah Parham receives 20 from Georgina
		Long senderId = Long.valueOf(4);
		Long sendingBankAccountId = Long.valueOf(5);
		Long receiverId = Long.valueOf(5);
		Long invalidReceivingBankAccountId = Long.valueOf(999);
		BigDecimal amount = BigDecimal.valueOf(20);

		TransactionRequest request = new TransactionRequest(sendingBankAccountId, senderId,
				invalidReceivingBankAccountId, receiverId, amount);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<TransactionRequest> requestEntity = new HttpEntity<>(request,
				headers);

		// restTemplate.put returns void so exchange must be used instead to check
		// status
		ResponseEntity<String> transferResponse = restTemplate.exchange("/api/transfer",
				HttpMethod.PUT, requestEntity,
				String.class);

		assertEquals("[\"Receiver does not have an account with that id\"]", transferResponse.getBody().toString());
	}

	// confirms that transfer denied for a receiver without a matching account
	// number
	@Test
	public void testTransferFromAccountNotOwnedByReceiver() {
		// make sure the repos loaded.
		assertNotNull(customerRepository);
		assertNotNull(bankAccountRepository);
		assertNotNull(transactionRepository);

		// Judah Parham receives 20 from Georgina
		Long senderId = Long.valueOf(4);
		Long sendingBankAccountId = Long.valueOf(5);
		Long receiverId = Long.valueOf(5);
		Long invalidReceivingBankAccountId = Long.valueOf(1);
		BigDecimal amount = BigDecimal.valueOf(20);

		TransactionRequest request = new TransactionRequest(sendingBankAccountId, senderId,
				invalidReceivingBankAccountId, receiverId, amount);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<TransactionRequest> requestEntity = new HttpEntity<>(request,
				headers);

		// restTemplate.put returns void so exchange must be used instead to check
		// status
		ResponseEntity<String> transferResponse = restTemplate.exchange("/api/transfer",
				HttpMethod.PUT, requestEntity,
				String.class);

		assertEquals("[\"Receiving bank account does not belong to receiving customer\"]",
				transferResponse.getBody().toString());
	}

	// confirms that transfer denied for a receiver without a matching account
	// number
	@Test
	public void testTransferFromAccountNotOwnedBySender() {
		// make sure the repos loaded.
		assertNotNull(customerRepository);
		assertNotNull(bankAccountRepository);
		assertNotNull(transactionRepository);

		// Judah Parham receives 20 from Georgina
		Long senderId = Long.valueOf(4);
		Long invalidSendingBankAccountId = Long.valueOf(1);
		Long receiverId = Long.valueOf(5);
		Long receivingBankAccountId = Long.valueOf(6);
		BigDecimal amount = BigDecimal.valueOf(20);

		TransactionRequest request = new TransactionRequest(invalidSendingBankAccountId, senderId,
				receivingBankAccountId, receiverId, amount);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<TransactionRequest> requestEntity = new HttpEntity<>(request,
				headers);

		// restTemplate.put returns void so exchange must be used instead to check
		// status
		ResponseEntity<String> transferResponse = restTemplate.exchange("/api/transfer",
				HttpMethod.PUT, requestEntity,
				String.class);

		assertEquals("[\"Sending bank account does not belong to sending customer\"]",
				transferResponse.getBody().toString());
	}

	// This checks the expected loaded transaction list for a customer
	@Test
	public void testGetTransactionDetails() {
		// make sure the repos loaded.
		assertNotNull(customerRepository);
		assertNotNull(bankAccountRepository);
		assertNotNull(transactionRepository);

		// this is the ccount that belongs to 'Arisha Barron'
		Long customerId = Long.valueOf(1);

		ResponseEntity<String> response = restTemplate
				.getForEntity("/api/getCustomerTransactionDetails/" + customerId, String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		assertEquals("[\"FROM Georgina Hazel 20 TO Arisha Barron\"]", response.getBody().toString());
	}

	/*
	 * This test is for an exchange of 20 between Judah and Georgina.
	 * The expected output is that both Judah and Georgina will have
	 * a balance at the end of the transaction that reflects the
	 * amount moved between them. There should also be a record
	 * of the transaction between the two of them in their
	 * transaction details list.
	 */
	@Test
	public void testTansferBetweenAccountsAndTransactionRecord() {
		// make sure the repos loaded.
		assertNotNull(customerRepository);
		assertNotNull(bankAccountRepository);
		assertNotNull(transactionRepository);

		// Judah Parham receives 20 from Georgina
		Long senderId = Long.valueOf(4);
		Long sendingBankAccountId = Long.valueOf(5);
		Long receiverId = Long.valueOf(5);
		Long receivingBankAccountId = Long.valueOf(6);
		BigDecimal amount = BigDecimal.valueOf(20);
		Optional<BankAccount> startingBalanceSender = bankAccountRepository.findById(sendingBankAccountId);
		Optional<BankAccount> startingBalanceReceiver = bankAccountRepository.findById(receivingBankAccountId);

		TransactionRequest request = new TransactionRequest(sendingBankAccountId, senderId,
				receivingBankAccountId, receiverId, amount);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<TransactionRequest> requestEntity = new HttpEntity<>(request,
				headers);

		// restTemplate.put returns void so exchange must be used instead to check
		// status
		ResponseEntity<String> transferResponse = restTemplate.exchange("/api/transfer",
				HttpMethod.PUT, requestEntity,
				String.class);

		assertEquals(HttpStatus.OK, transferResponse.getStatusCode());

		// confirm transaction detail was created for Judah
		ResponseEntity<String> transactionDetailsReceiverResponse = restTemplate
				.getForEntity("/api/getCustomerTransactionDetails/" + receiverId, String.class);

		assertEquals(HttpStatus.OK, transferResponse.getStatusCode());
		assertNotNull(transactionDetailsReceiverResponse.getBody());
		String receiverTransactionDetails = transactionDetailsReceiverResponse.getBody().toString();

		assertEquals("[\"FROM Georgina Hazel 20 TO Judah Parham\"]", receiverTransactionDetails);

		// confirm transaction detail was created for Georgina
		ResponseEntity<String> transactionDetailsSenderResponse = restTemplate
				.getForEntity("/api/getCustomerTransactionDetails/" + receiverId, String.class);

		assertEquals(HttpStatus.OK, transferResponse.getStatusCode());
		assertNotNull(transactionDetailsSenderResponse.getBody());
		String senderTransactionDetails = transactionDetailsSenderResponse.getBody().toString();

		assertTrue(senderTransactionDetails.indexOf("FROM Georgina Hazel 20 TO Judah Parham") > 0);

		// confirm the end balance for Judah
		String expectedEndreceiverBalance = (amount.add(startingBalanceReceiver.get().getBalance())).toString();

		String actualEndingReceiverBalance = bankAccountRepository.findById(receivingBankAccountId).get().getBalance()
				.toString();
		assertEquals(expectedEndreceiverBalance, actualEndingReceiverBalance);

		// confirm the end balance for Georgina
		String expectedEndSenderBalance = (startingBalanceSender.get().getBalance().subtract(amount)).toString();

		String actualEndingSenderBalance = bankAccountRepository.findById(sendingBankAccountId).get().getBalance()
				.toString();
		assertEquals(expectedEndSenderBalance, actualEndingSenderBalance);

	}

}
