package org.jboss.narayana.case02681833;

import com.arjuna.ats.arjuna.common.CoreEnvironmentBean;
import com.arjuna.ats.internal.arjuna.objectstore.hornetq.HornetqJournalEnvironmentBean;
import com.arjuna.ats.internal.arjuna.objectstore.hornetq.HornetqObjectStoreAdaptor;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;

import java.io.File;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class Runner {

    public static void main(String[] args) throws Exception {

        HornetqJournalEnvironmentBean hornetqJournalEnvironmentBean = BeanPopulator.getDefaultInstance(HornetqJournalEnvironmentBean.class);

        // given: each tx writes a transaction log record, which is a journal append of 660 payload bytes, followed immediately by a delete.
        // at any given time the number of live tx in the journal is at most the number of concurrent threads.
        // thus a single 32k file will hold enough live tx for a reasonable sized desktop machine.

        hornetqJournalEnvironmentBean.setFileSize(1024*32);

        // with sync on, it needs lots of threads to saturate the system. With sync off, one will do.
        hornetqJournalEnvironmentBean.setSyncDeletes(false);
        hornetqJournalEnvironmentBean.setSyncWrites(false);

        // required: that at at no time does the number of journal files exceed 4.

        hornetqJournalEnvironmentBean.setCompactMinFiles(2);
        hornetqJournalEnvironmentBean.setPoolSize(4);

        BeanPopulator.getDefaultInstance(CoreEnvironmentBean.class).setNodeIdentifier("1");
        BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).setObjectStoreType(HornetqObjectStoreAdaptor.class.getName());

        int numProcs = 1; // Runtime.getRuntime().availableProcessors();

        ThreadPoolExecutor executor = new ThreadPoolExecutor(numProcs, numProcs, 1, TimeUnit.HOURS, new LinkedBlockingDeque<>(numProcs));

        for(int i = 0; i < numProcs; i++) {
            executor.submit(new RunnableTx(10000));
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        System.out.println("number of files expected: "+hornetqJournalEnvironmentBean.getPoolSize());
        System.out.println("number of files present: "+new File("HornetqJournalStore").listFiles().length);
        System.exit(0);
    }
}
