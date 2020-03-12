package cn.xpbootcamp.legacy_code;

import cn.xpbootcamp.legacy_code.enums.STATUS;
import cn.xpbootcamp.legacy_code.service.WalletService;
import cn.xpbootcamp.legacy_code.service.WalletServiceImpl;
import cn.xpbootcamp.legacy_code.utils.RedisDistributedLock;
import org.junit.jupiter.api.Test;

import javax.transaction.InvalidTransactionException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class WalletTransactionTest {

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

		RedisDistributedLock redisDistributedLock = mock(RedisDistributedLock.class);
		when(redisDistributedLock.lock("t_h1")).thenReturn(true);

		WalletService walletService = mock(WalletServiceImpl.class);
		when(walletService.moveMoney("t_h1", 1L, 1L, 1d)).thenReturn(null);


		WalletTransaction walletTransaction = spy(new WalletTransaction("t_h1", 1L, 1L, 1L, "aa"));
		doReturn(redisDistributedLock).when(walletTransaction).getRedisDistributedLockInstance();
		doReturn(walletService).when(walletTransaction).getWalletService();
		walletTransaction.setAmount(10D);

		assertFalse(walletTransaction.execute());
		assertEquals(STATUS.FAILED, walletTransaction.getStatus());
	}

	@Test
	void should_return_true_when_moveMoney_successful() throws InvalidTransactionException {

		RedisDistributedLock redisDistributedLock = mock(RedisDistributedLock.class);
		when(redisDistributedLock.lock("t_h1")).thenReturn(true);

		WalletService walletService = mock(WalletServiceImpl.class);
		when(walletService.moveMoney("t_h1", 1L, 1L, 1d)).thenReturn(UUID.randomUUID().toString() + "aaa");


		WalletTransaction walletTransaction = spy(new WalletTransaction("t_h1", 1L, 1L, 1L, "aa"));
		doReturn(redisDistributedLock).when(walletTransaction).getRedisDistributedLockInstance();
		doReturn(walletService).when(walletTransaction).getWalletService();
		walletTransaction.setAmount(1L);

		assertTrue(walletTransaction.execute());
		assertEquals(STATUS.EXECUTED, walletTransaction.getStatus());
	}

	@Test
	void should_return_true_when_locked_failed() throws InvalidTransactionException {
		RedisDistributedLock redisDistributedLock = mock(RedisDistributedLock.class);
		when(redisDistributedLock.lock("t_h1")).thenReturn(false);

		WalletService walletService = mock(WalletServiceImpl.class);
		when(walletService.moveMoney("t_h1", 1L, 1L, 1d)).thenReturn(UUID.randomUUID().toString() + "t_h1");

		WalletTransaction walletTransaction = spy(new WalletTransaction("t_h1", 1L, 1L, 1L, "aa"));
		doReturn(redisDistributedLock).when(walletTransaction).getRedisDistributedLockInstance();
		doReturn(walletService).when(walletTransaction).getWalletService();
		walletTransaction.setAmount(1L);
		assertFalse(walletTransaction.execute());
	}

	@Test
	void executeTestReturnTrueWhenExpired() throws InvalidTransactionException {
		RedisDistributedLock redisDistributedLock = mock(RedisDistributedLock.class);
		when(redisDistributedLock.lock("t_h1")).thenReturn(true);

		WalletService walletService = mock(WalletServiceImpl.class);
		when(walletService.moveMoney("t_h1", 1L, 1L, 1d)).thenReturn(UUID.randomUUID().toString() + "aaa");

		WalletTransaction walletTransaction = spy(new WalletTransaction("t_h1", 1L, 1L, 1L, "aa"));
		doReturn(redisDistributedLock).when(walletTransaction).getRedisDistributedLockInstance();
		doReturn(walletService).when(walletTransaction).getWalletService();
		doReturn(true).when(walletTransaction).isExpired();
		walletTransaction.setAmount(1L);

		assertFalse(walletTransaction.execute());
	}
}
