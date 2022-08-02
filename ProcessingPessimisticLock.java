import java.math.BigDecimal;
import java.util.concurrent.locks.ReentrantLock;


public class ProcessingPessimisticLock {

    public static void transferMoney(Account account1, Account account2, BigDecimal money) {

        //before Lock validation
        boolean isEnoughMoney = account1.getMoney().compareTo(money) >= 0;
        if (!isEnoughMoney)
            throw new IllegalStateException("Not enough money on account: " + account1.getName());

        //acquire the lock
        account1.getLock().lock();
        account2.getLock().lock();
        try {
            unsafeTransferMoney(account1, account2, money);
        } finally {
            //anyway release the lock
            account1.getLock().unlock();
            account2.getLock().unlock();
        }
    }

    private static void unsafeTransferMoney(Account account1, Account account2, BigDecimal money) {
        BigDecimal acc1ResMoney = account1.getMoney().subtract(money);
        if (acc1ResMoney.signum() < 0)
            throw new IllegalStateException("Not enough money on account: " + account1.getName());
        BigDecimal acc2ResMoney = account2.getMoney().add(money);

        account1.setMoney(acc1ResMoney);
        account2.setMoney(acc2ResMoney);
    }


    private static class Account {
        private String name;
        private volatile BigDecimal money;
        private ReentrantLock lock = new ReentrantLock();

        public Account(String name, BigDecimal money) {
            this.name = name;
            this.money = money;
        }

        public String getName() {
            return name;
        }

        public BigDecimal getMoney() {
            return money;
        }

        public void setMoney(BigDecimal money) {
            this.money = money;
        }

        public ReentrantLock getLock() {
            return lock;
        }
    }
}
