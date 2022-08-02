import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;


public class ProcessingOptimisticLock {

    public static void transferMoney(Account account1, Account account2, BigDecimal money) {

        boolean repeat = true;

        while (repeat) {
            repeat = !unsafeTransferMoney(account1, account2, money);
        }
    }

    private static boolean unsafeTransferMoney(Account account1, Account account2, BigDecimal money) {
        BigDecimal acc1Money = account1.getMoney();
        BigDecimal acc2Money = account2.getMoney();

        BigDecimal acc1ResMoney = account1.getMoney().subtract(money);
        if (acc1ResMoney.signum() < 0)
            throw new IllegalStateException("Not enough money on account: " + account1.getName());
        BigDecimal acc2ResMoney = account2.getMoney().add(money);

        boolean successSettingAcc1 = account1.setMoney(acc1Money, acc1ResMoney);
        boolean successSettingAcc2 = account2.setMoney(acc2Money, acc2ResMoney);

        return successSettingAcc1 && successSettingAcc2;
    }


    private static class Account {
        private final String name;
        private final AtomicReference<BigDecimal> atomicMoney;

        public Account(String name, BigDecimal money) {
            this.name = name;
            this.atomicMoney = new AtomicReference<>(money);
        }

        public String getName() {
            return name;
        }

        public BigDecimal getMoney() {
            return atomicMoney.get();
        }

        public boolean setMoney(BigDecimal expectedOldMoney, BigDecimal money) {
            return atomicMoney.compareAndSet(expectedOldMoney, money);
        }
    }
}
