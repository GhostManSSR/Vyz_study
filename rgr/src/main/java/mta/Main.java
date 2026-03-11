package mta;

import mta.db.Database;
import mta.queue.RetryWorker;
import mta.smtp.SMTPServer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws Exception {

        Database.init();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        RetryWorker retryWorker = new RetryWorker();
        executor.submit(retryWorker);

        SMTPServer server = new SMTPServer(2525);
        executor.submit(() -> {
            try {
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        System.out.println("MiniMTA running on port 2525...");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down MiniMTA...");

            server.stop();
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS))
                    executor.shutdownNow();
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }

            System.out.println("MiniMTA stopped");
        }));
    }
}
