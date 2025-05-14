package com.example.app;

import com.example.app.bridge.EmailNotificationSender;
import com.example.app.bridge.NotificationSender;
import com.example.app.bridge.SMSNotificationSender;
import com.example.app.decorator.InsuranceDecorator;
import com.example.app.decorator.PremiumBenefitsDecorator;
import com.example.app.exception.HighRiskTransactionException;
import com.example.app.interfaces.NotificationService;
import com.example.app.model.*;
import com.example.app.service.AccountServiceSecurityProxy;
import com.example.app.service.*;
import com.example.app.interfaces.AccountAbstractFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Qualifier;
import java.math.BigDecimal;

@SpringBootApplication
public class AppApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppApplication.class, args);
	}

	@Bean
	public CommandLineRunner demo(
			@Qualifier("standardAccountFactory") AccountAbstractFactory standardFactory,
			@Qualifier("premiumAccountFactory") AccountAbstractFactory premiumFactory,
			@Qualifier("accountService") AccountService accountService,
			@Qualifier("smsNotificationService") NotificationService smsService,
			BankingFacade bankingFacade,
			@Qualifier("emailNotificationService") NotificationService emailService) {

		return args -> {
			testBuilderPattern();
			testPrototypePattern(accountService);
			testFlyweightPattern(accountService);
			testAdapterPattern(accountService, smsService);
			testCompositePattern(accountService);
			testFacadePattern(accountService, bankingFacade);
			testDecoratorPattern(accountService);
			testBridgePattern(accountService, emailService, smsService);
			testProxyPattern(accountService);
		};
	}

	private void testBuilderPattern() {
		System.out.println("\n=== TEST BUILDER PATTERN ===");
		BankCustomer customer = new BankCustomer.Builder()
				.name("Dorian")
				.address("Str. Studentilor 7/1")
				.email("dorian@example.com")
				.phone("+37360471665")
				.build();

		System.out.println("Client creat cu succes:");
		System.out.println(customer);
	}

	private void testPrototypePattern(AccountService accountService) {
		System.out.println("\n=== TEST PROTOTYPE PATTERN ===");
		Account original = accountService.createCheckingAccount("ORIG123");
		original.deposit(BigDecimal.valueOf(1500));

		Account clone = accountService.cloneAccount("ORIG123", "CLONE456");
		System.out.println("Sold original: " + original.getBalance());
		System.out.println("Sold clonă: " + clone.getBalance());
	}

	private void testFlyweightPattern(AccountService accountService) {
		System.out.println("\n=== TEST FLYWEIGHT PATTERN ===");

		Account flySav1 = accountService.createSavingsAccount("FLY-SAV1");
		Account flySav2 = accountService.createSavingsAccount("FLY-SAV2");
		BigDecimal rate1 = ((StandardSavingsAccount) flySav1).getInterestRate();
		BigDecimal rate2 = ((StandardSavingsAccount) flySav2).getInterestRate();
		System.out.println("[Savings] Same interest rate instance? " + (rate1 == rate2));

		Account flyChk1 = accountService.createCheckingAccount("FLY-CHK1");
		Account flyChk2 = accountService.createCheckingAccount("FLY-CHK2");
		BigDecimal overdraft1 = ((StandardCheckingAccount) flyChk1).getOverdraftLimit();
		BigDecimal overdraft2 = ((StandardCheckingAccount) flyChk2).getOverdraftLimit();
		System.out.println("[Checking] Same overdraft instance? " + (overdraft1 == overdraft2));
	}

	private void testAdapterPattern(AccountService accountService, NotificationService smsService) {
		System.out.println("\n=== TEST ADAPTER PATTERN ===");
		Account adapterAcc1 = accountService.createCheckingAccount("ADAPT1");
		Account adapterAcc2 = accountService.createSavingsAccount("ADAPT2");

		adapterAcc1.deposit(BigDecimal.valueOf(2000));
		smsService.sendTransferNotification(adapterAcc1, adapterAcc2, BigDecimal.valueOf(500));
		System.out.println("Notificare SMS trimisă cu succes!");
	}

	private void testCompositePattern(AccountService accountService) {
		System.out.println("\n=== TEST COMPOSITE PATTERN ===");
		Portfolio portfolio = new Portfolio();

		Account compAcc1 = accountService.createCheckingAccount("COMP1");
		compAcc1.deposit(BigDecimal.valueOf(1000));

		Account compAcc2 = accountService.createPremiumSavingsAccount("COMP2");
		compAcc2.deposit(BigDecimal.valueOf(2500));

		portfolio.add(compAcc1);
		portfolio.add(compAcc2);
		System.out.println("Sold total portofoliu: " + portfolio.getBalance() + " LEI");
	}

	private void testFacadePattern(AccountService accountService, BankingFacade bankingFacade) {
		System.out.println("\n=== TEST FAÇADE PATTERN ===");

		BankCustomer customer = bankingFacade.createFullCustomer(
				"Ana",
				"ana@example.com",
				"+37360000000",
				"FACADE-001",
				"FACADE-002"
		);

		Portfolio source = new Portfolio();
		source.add(accountService.getAccountByNumber("FACADE-001"));
		source.add(accountService.getAccountByNumber("FACADE-002"));

		Portfolio destination = new Portfolio();
		destination.add(accountService.createPremiumCheckingAccount("FACADE-003"));

		accountService.getAccountByNumber("FACADE-001").deposit(BigDecimal.valueOf(5000));
		accountService.getAccountByNumber("FACADE-002").deposit(BigDecimal.valueOf(3000));

		bankingFacade.transferBetweenPortfolios(source, destination, BigDecimal.valueOf(2000));
		System.out.println("Transfer realizat cu succes prin Façade!");
	}

	private void testBridgePattern(AccountService accountService,
								   NotificationService emailService,
								   NotificationService smsService) {
		System.out.println("\n=== TEST BRIDGE PATTERN ===");

		Account bridgeAcc1 = accountService.createCheckingAccount("BRIDGE1");
		Account bridgeAcc2 = accountService.createSavingsAccount("BRIDGE2");
		bridgeAcc1.deposit(BigDecimal.valueOf(5000));

		System.out.println("\n[Test 1] Notificare via Email:");
		emailService.sendTransferNotification(bridgeAcc1, bridgeAcc2, BigDecimal.valueOf(1500));

		System.out.println("\n[Test 2] Notificare via SMS:");
		smsService.sendTransferNotification(bridgeAcc1, bridgeAcc2, BigDecimal.valueOf(2500));

		System.out.println("\n[Test 3] Notificare custom:");
		NotificationService customService = new EmailNotificationService(new SMSNotificationSender());
		customService.sendTransferNotification(bridgeAcc1, bridgeAcc2, BigDecimal.valueOf(1000));
	}

	private void testDecoratorPattern(AccountService accountService) {
		System.out.println("\n=== TEST DECORATOR PATTERN ===");

		Account baseAccount = accountService.createSavingsAccount("DECOR1");
		baseAccount.deposit(BigDecimal.valueOf(1000));

		Account insuredAccount = accountService.addInsurance(baseAccount, BigDecimal.valueOf(50));
		Account premiumAccount = accountService.addPremiumBenefits(insuredAccount);

		premiumAccount.deposit(BigDecimal.valueOf(200));

		if(premiumAccount instanceof SavingsAccount) {
			((SavingsAccount) premiumAccount).applyInterest();
		}

		System.out.println("Sold final: " + premiumAccount.getBalance() + " LEI");
		System.out.println("Istoric tranzacții:");
		premiumAccount.getTransactionHistory().forEach(t ->
				System.out.printf("-> %-20s: %8.2f LEI\n", t.getDescription(), t.getAmount())
		);
	}

	private void testProxyPattern(AccountService accountService) {
		System.out.println("\n=== TEST PROXY PATTERN ===");

		Account proxyAcc1 = accountService.createCheckingAccount("PROXY1");
		Account proxyAcc2 = accountService.createSavingsAccount("PROXY2");
		proxyAcc1.deposit(BigDecimal.valueOf(15000));

		// Test 1: Tranzacție validă
		try {
			accountService.transfer(proxyAcc1, proxyAcc2, BigDecimal.valueOf(9000));
			System.out.println("Transfer reușit: 9000 LEI");
		} catch (HighRiskTransactionException e) {
			System.out.println("Eroare neașteptată: " + e.getMessage());
		}

		// Test 2: Tranzacție blocată
		try {
			accountService.transfer(proxyAcc1, proxyAcc2, BigDecimal.valueOf(11000));
			System.out.println("Eroare: Tranzacția ar fi trebuit blocată!");
		} catch (HighRiskTransactionException e) {
			System.out.println("Operație blocată cu succes: " + e.getMessage());
		}
	}

	@Bean
	public ThirdPartySMS thirdPartySMS() {
		return new ThirdPartySMS();
	}

	@Bean
	public NotificationSender emailSender() {
		return new EmailNotificationSender();
	}

	@Bean
	public NotificationSender smsSender() {
		return new SMSNotificationSender();
	}
}