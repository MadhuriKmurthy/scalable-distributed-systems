package project2;

import static org.junit.jupiter.api.Assertions.*;

import java.rmi.Naming;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class RMITest {

	@Test
	void testThreadPutJoinDelete() throws InterruptedException {
		int numThreads = 100;

		Thread putThread[] = new Thread[numThreads];
		Thread deleteThread[] = new Thread[numThreads];

		AtomicInteger adds = new AtomicInteger(0);
		AtomicInteger deletes = new AtomicInteger(0);

		Runnable addTask = () -> {
			try {
				RMIInterface rmiInterface = (RMIInterface) Naming.lookup("rmi://127.0.0.1:5800/MyKeyValueMap");
				rmiInterface.put("key" + adds.incrementAndGet(), "value");
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		};
		Runnable deleteTask = () -> {
			try {

				RMIInterface rmiInterface = (RMIInterface) Naming.lookup("rmi://127.0.0.1:5800/MyKeyValueMap");
				rmiInterface.delete("key" + deletes.incrementAndGet());

			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		};

		for (int i = 0; i < numThreads; i++) {
			putThread[i] = new Thread(addTask);
			putThread[i].start();
		}

		for (int i = 0; i < numThreads; i++) {
			putThread[i].join();
		}

		for (int i = 0; i < numThreads; i++) {
			deleteThread[i] = new Thread(deleteTask);
			deleteThread[i].start();
		}

		for (int i = 0; i < numThreads; i++) {
			deleteThread[i].join();
		}

		try {

			RMIInterface rmiInterface = (RMIInterface) Naming.lookup("rmi://127.0.0.1:5800/MyKeyValueMap");
			int expectedSize = adds.get() - deletes.get();
			int actualSize = rmiInterface.getMapSize();
			assertEquals(expectedSize, actualSize);

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	
	@Test
	void testThreadPutDelete() throws InterruptedException {
		int numThreads = 100;

		Thread putThread[] = new Thread[numThreads];
		Thread deleteThread[] = new Thread[numThreads];

		AtomicInteger adds = new AtomicInteger(0);
		AtomicInteger deletes = new AtomicInteger(0);
		AtomicInteger deleteCtr = new AtomicInteger(0);

		Runnable addTask = () -> {
			try {
				RMIInterface rmiInterface = (RMIInterface) Naming.lookup("rmi://127.0.0.1:5800/MyKeyValueMap");
				rmiInterface.put("key" + adds.incrementAndGet(), "value");
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		};
		Runnable deleteTask = () -> {
			try {

				RMIInterface rmiInterface = (RMIInterface) Naming.lookup("rmi://127.0.0.1:5800/MyKeyValueMap");
				String msg = rmiInterface.delete("key" + deletes.incrementAndGet());
				if("Deleted Key:".contains(msg)) {
					deleteCtr.incrementAndGet();
				}

			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		};

		try {

			RMIInterface rmiInterface = (RMIInterface) Naming.lookup("rmi://127.0.0.1:5800/MyKeyValueMap");
			int expectedSize = adds.get() - deleteCtr.get();
			int actualSize = rmiInterface.getMapSize();
			assertEquals(expectedSize, actualSize);

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

}
