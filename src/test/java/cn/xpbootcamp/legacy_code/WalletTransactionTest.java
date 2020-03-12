package cn.xpbootcamp.legacy_code;

import cn.xpbootcamp.legacy_code.enums.STATUS;
import cn.xpbootcamp.legacy_code.service.WalletServiceImpl;
import cn.xpbootcamp.legacy_code.utils.RedisDistributedLock;
import org.junit.jupiter.api.Test;

import javax.transaction.InvalidTransactionException;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WalletTransactionTest {
	private WalletServiceImpl service = mock(WalletServiceImpl.class);
	private RedisDistributedLock distributedLock = mock(RedisDistributedLock.class);


	@Test
	void should_throw_exception_when_buyerId_null() {
		WalletTransaction walletTransaction = new WalletTransaction("t_buyerId_null", null, 1L, 1L, "aa");
		assertThrows(InvalidTransactionException.class, walletTransaction::execute);
	}

	@Test
	void should_throw_exception_when_sellerId_null() {
		WalletTransaction walletTransaction = new WalletTransaction("t_sellerId_null", 1L, null, 1L, "aa");
		assertThrows(InvalidTransactionException.class, walletTransaction::execute);
	}

	@Test
	void should_throw_exception_when_amount_less_than_0() {
		WalletTransaction walletTransaction = new WalletTransaction("t_amount_less_0", 1L, 1L, 1L, "aa");
		walletTransaction.setAmount(-1D);
		assertThrows(InvalidTransactionException.class, walletTransaction::execute);
	}

	@Test
	void should_return_true_when_status_is_executed() throws InvalidTransactionException {
		WalletTransaction walletTransaction = new WalletTransaction("t_h1", 1L, 1L, 1L, "aa");
		walletTransaction.setStatus(STATUS.EXECUTED);
		walletTransaction.setAmount(1D);
		assertTrue(walletTransaction.execute());
	}

	@Test
	void should_return_false_when_moveMoney_return_null() throws InvalidTransactionException {
		when(service.moveMoney("t_h1", 1L, 2L, 10D)).thenReturn(null);
		when(distributedLock.lock("t_h1")).thenReturn(true);

		WalletTransaction walletTransaction = new WalletTransaction("t_h1", 1L, 2L, 1L, "aa");
		walletTransaction.setAmount(10D);
		walletTransaction.setInstance(distributedLock);
		walletTransaction.setWalletService(service);

		assertFalse(walletTransaction.execute());
		assertEquals(STATUS.FAILED, walletTransaction.getStatus());
	}

	@Test
	void should_return_true_when_moveMoney_successful() throws InvalidTransactionException {
		when(service.moveMoney("t_h1", 1L, 2L, 10D)).thenReturn("aaa");
		when(distributedLock.lock("t_h1")).thenReturn(true);

		WalletTransaction walletTransaction = new WalletTransaction("t_h1", 1L, 2L, 1L, "aa");
		walletTransaction.setAmount(10D);
		walletTransaction.setInstance(distributedLock);
		walletTransaction.setWalletService(service);

		assertTrue(walletTransaction.execute());
		assertEquals(STATUS.EXECUTED, walletTransaction.getStatus());
	}
}
