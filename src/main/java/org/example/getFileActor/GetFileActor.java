package org.example.getFileActor;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import akka.persistence.typed.*;
import akka.persistence.typed.javadsl.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetFileActor extends EventSourcedBehavior<String, String, List<String>> {

    private final ActorRef<String> filterActor;
    private static final int MAX_BATCH_SIZE = 1024;

    private GetFileActor( ActorRef<String> filterActor, PersistenceId persistenceId) {
        super(persistenceId);
        this.filterActor = filterActor;
    }

    public static Behavior<String> create(ActorRef<String> filterActor, PersistenceId persistenceId) {
        return Behaviors.setup(context -> new GetFileActor(filterActor, persistenceId));
    }

    @Override
    public List<String> emptyState() {
        return new ArrayList<String>();
    }

    @Override
    public CommandHandler<String, String, List<String>> commandHandler() {
        return newCommandHandlerBuilder()
                .forAnyState()
                .onCommand(String.class, this::onHandleDirectory)
                .build();
    }

    private Effect<String, List<String>> onHandleDirectory(List<String> state, String path) {
        File directory = new File(path);                                                //path to directory

        File[] files = directory.listFiles();

        assert files != null;
        for (File file : files) {

            try {

                long fileSize = file.length();
                StringBuilder data= new StringBuilder();
                if (fileSize > MAX_BATCH_SIZE) {

                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

                        int numBatches = (int) Math.ceil((double) fileSize / MAX_BATCH_SIZE);   // Calculate the number of batches

                        for (int i = 0; i < numBatches; i++) {
                            // Read the next batch
                            char[] batch = new char[MAX_BATCH_SIZE];
                            int read = reader.read(batch, 0, MAX_BATCH_SIZE);


                            String batchString = new String(batch, 0, read);              // Process the batch
                            data.append(batchString);

                        }
                    }
                } else {

                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {      // File size is less than or equal to the maximum batch size
                        // Read the entire file in one go
                        StringBuilder entireFile = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            entireFile.append(line).append("\n");
                        }
                        data = new StringBuilder(entireFile.toString());
                    }
                }
                filterActor.tell(data.toString());
            }catch (IOException e) {
                e.printStackTrace();
            }
            file.delete();
        }
        return Effect().none();
    }


    @Override
    public EventHandler<List<String>, String> eventHandler() {
        return newEventHandlerBuilder()
                .forAnyState()
                .build();
    }
}
