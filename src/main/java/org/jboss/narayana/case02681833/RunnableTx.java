package org.jboss.narayana.case02681833;

import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

public class RunnableTx implements Runnable {

    private final int numberOfTransactions;

    public RunnableTx(int numberOfTransactions) {
        this.numberOfTransactions = numberOfTransactions;
    }

    @Override
    public void run() {
        TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
        XAResource resource1 = new XAResourceImpl();
        XAResource resource2 = new XAResourceImpl();

        for (int i = 0; i < numberOfTransactions; i++) {
            try {

                tm.begin();

                tm.getTransaction().enlistResource(resource1);
                tm.getTransaction().enlistResource(resource2);

                tm.commit();

            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
